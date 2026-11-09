# Jenkins Dispatcher Monitor 운영 활성화 Runbook

## 배경과 목적

#4815 (#4820 머지 완료) 에서 dispatcher(SERVICE=auto) 가 트리거한 자식 빌드 N건의 결과를 별도 executor 에서 폴링/집계 후 통합 Slack 알림을 보내는 `dispatcher-monitor` 잡을 도입했다. 이는 dispatcher 잡이 `disableConcurrentBuilds()` 와 `build(wait: true)` 의 교착(동일 잡 self-trigger 시 자식이 큐에서 대기)을 회피하기 위한 분리 구조다.

`Jenkinsfile.dispatcher-monitor` 와 `jenkins/resolve-and-register.groovy` 의 헬퍼(`triggerDispatcherMonitor`, `appendDispatcherMonitorLink`, `pollDispatcherChildren`) 코드는 머지 완료되었으나, 실제 동작에는 Jenkins 운영 측에서 아래 3개 항목을 등록해야 한다.

- Jobs 3개 (환경별 dispatcher-monitor Pipeline 잡)
- Credentials 3개 (Jenkins REST API 호출용 username + API token)
- pipeline-utility-steps 플러그인 (`readJSON` / `writeJSON` 사용)

이 Runbook 은 운영자가 Jenkins 콘솔에서 위 3개 단계를 수행하기 위한 체크리스트와 검증 절차를 명문화한다.

### Fail-open 안전장치

`triggerDispatcherMonitor` / `appendDispatcherMonitorLink` / `pollDispatcherChildren` 는 모두 fail-open 으로 설계되어 있다. monitor 잡 미존재, credential 미등록, REST API 호출 실패 시 `echo` 만 남기고 dispatcher 본 흐름(자식 빌드 트리거)은 계속 진행한다. 따라서 본 Runbook 미수행 상태에서도 dispatcher 자체는 정상 동작하고, 단지 **통합 Slack 알림과 description 역기록만 누락**된다.

## 사전 요구사항

- Jenkins 환경: MARKETNOTE_QA / MARKETNOTE_STAGE / MARKETNOTE_PROD 3개 인스턴스 (또는 동일 인스턴스 내 환경별 폴더)
- Jenkins 버전: 2.176.2 이상 (CSRF crumb API token 면제 정책 의존, #4832 참조)
- 기존 dispatcher 잡(`shop-qa`, `shop-stage`, `shop-prod`) 정상 동작 중
- 운영자 권한: Jenkins Job 생성 / Credentials 등록 / Plugin 설치 권한
- Jenkins 서비스 계정: REST API 호출용 별도 계정 (Build read + Description 수정 권한 보유)

## Step 1 — Jobs 3개 등록

환경별로 dispatcher-monitor Pipeline 잡을 1개씩 총 3개 생성한다. 잡 이름은 `triggerDispatcherMonitor` 헬퍼가 `${dispatcher_project}-dispatcher-monitor` 규칙으로 추론하므로 정확히 일치해야 한다 (`jenkins/resolve-and-register.groovy:757`).

### 등록 잡 목록

| 환경 | dispatcher 잡 이름 | monitor 잡 이름 (필수) |
|------|---------------------|--------------------------|
| QA | `shop-qa` | `shop-qa-dispatcher-monitor` |
| Stage | `shop-stage` | `shop-stage-dispatcher-monitor` |
| Prod | `shop-prod` | `shop-prod-dispatcher-monitor` |

dispatcher 잡이 멀티브랜치 구조(`shop-qa/main` 등)인 경우 monitor 잡도 동일 멀티브랜치 구조(`shop-qa-dispatcher-monitor/main`)를 유지한다. 헬퍼는 dispatcher 의 `JOB_NAME` 에서 첫 segment 를 project, 나머지를 branchPath 로 분리해 `${project}-dispatcher-monitor/${branchPath}` 를 monitor job name 으로 구성한다.

### 잡 설정

각 잡에 대해 다음 설정을 적용한다.

- **타입**: Pipeline (또는 dispatcher 와 동일한 Multibranch Pipeline)
- **Definition**: Pipeline script from SCM
- **SCM**: Git (dispatcher 와 동일 저장소)
- **Branches to build**: dispatcher 와 동일 (`*/main` 또는 환경 매핑 브랜치)
- **Script Path**: `Jenkinsfile.dispatcher-monitor`
- **Lightweight checkout**: 활성화 권장

### 체크리스트

- [ ] `shop-qa-dispatcher-monitor` 잡 생성, Script Path = `Jenkinsfile.dispatcher-monitor`
- [ ] `shop-stage-dispatcher-monitor` 잡 생성, Script Path = `Jenkinsfile.dispatcher-monitor`
- [ ] `shop-prod-dispatcher-monitor` 잡 생성, Script Path = `Jenkinsfile.dispatcher-monitor`
- [ ] 각 잡의 SCM/branch 설정이 동명 dispatcher 잡과 일치
- [ ] 각 잡이 한 번 이상 Pipeline 정의 로딩에 성공 (수동 빌드 필요는 없음 — Branch Indexing 만으로 충분)

## Step 2 — Credentials 3개 추가

`appendDispatcherMonitorLink` 와 `pollDispatcherChildren` 는 `withCredentials([usernamePassword(credentialsId: "${envPrefix}_JENKINS_API", ...)])` 로 Jenkins REST API 를 호출한다 (`jenkins/resolve-and-register.groovy:792, 846`). 환경별 credential ID 는 dispatcher 가 SERVICE 매핑 시 결정한 `ENV_PREFIX` 를 그대로 사용한다.

### 등록 Credential 목록

| 환경 | Credential ID (필수) | 타입 |
|------|----------------------|-----|
| QA | `MARKETNOTE_QA_JENKINS_API` | Username with password |
| Stage | `MARKETNOTE_STAGE_JENKINS_API` | Username with password |
| Prod | `MARKETNOTE_PROD_JENKINS_API` | Username with password |

### 필드 값

- **Kind**: Username with password
- **Scope**: Global (모든 잡에서 사용)
- **Username**: Jenkins 서비스 계정 사용자명
- **Password**: 사용자 패스워드가 아닌 **Jenkins API token** (서비스 계정의 Settings > API Token > Add new Token 에서 발급한 값)
- **ID**: 위 표의 정확한 문자열 (오타 시 fail-open 으로 모든 SERVICE PENDING 처리 + monitor SUCCESS 유지, 알림 누락)
- **Description**: `${envPrefix} Jenkins REST API token (description append + child build polling)` 권장

### Password 가 API token 이어야 하는 이유 (#4832)

Jenkins 2.176.2 이후 `hudson.security.csrf.CrumbFilter` 는 **API token 의 Basic Auth 헤더에만 crumb 면제**를 적용한다. 사용자 패스워드를 password 필드에 넣으면 `appendDispatcherMonitorLink` 의 `submitDescription` POST 가 401/403 으로 실패한다. fail-open 정책상 monitor 잡은 SUCCESS 로 종료되지만 dispatcher 빌드 description 에 `MONITOR=<url>` 역기록이 누락된다.

상세는 [#4832 운영 docs 이슈](https://github.com/personal-service/marketnote/issues/4832) 와 본 Runbook 의 Step 4 검증 절차 참조.

### 서비스 계정 권한

해당 Jenkins 서비스 계정은 다음 권한이 필요하다.

- **Overall / Read** — REST API 진입
- **Job / Read** — dispatcher 잡 description 조회 (`api/json?tree=description`) 및 자식 빌드 목록 폴링 (`api/json?tree=builds[...]`)
- **Job / Configure 또는 Run / Update** — dispatcher 빌드 description 변경 (`submitDescription` POST)

가능하면 Matrix-based 권한 전략에서 dispatcher 잡 + monitor 잡 두 개로만 권한을 한정한 별도 서비스 계정을 사용한다.

### 체크리스트

- [ ] `MARKETNOTE_QA_JENKINS_API` 등록, Password = QA Jenkins 서비스 계정 API token
- [ ] `MARKETNOTE_STAGE_JENKINS_API` 등록, Password = Stage Jenkins 서비스 계정 API token
- [ ] `MARKETNOTE_PROD_JENKINS_API` 등록, Password = Prod Jenkins 서비스 계정 API token
- [ ] 각 credential ID 가 정확히 대문자 + 언더스코어 형식 (`MARKETNOTE_QA_JENKINS_API` 등) 일치
- [ ] 서비스 계정이 dispatcher 잡에 대해 Read + Description 수정 권한 보유

## Step 3 — pipeline-utility-steps 플러그인 설치 검증

`Jenkinsfile.dispatcher-monitor` 와 `jenkins/resolve-and-register.groovy` 는 `readJSON(text: ...)` 호출로 Jenkins REST API 응답을 파싱한다 (`jenkins/resolve-and-register.groovy:803, 867`). 이 step 은 [pipeline-utility-steps](https://plugins.jenkins.io/pipeline-utility-steps/) 플러그인이 제공한다. 본 프로젝트에서 처음 도입되는 step 이므로 운영 Jenkins 인스턴스에 설치 여부를 확인한다.

### 확인 절차

1. **Manage Jenkins > Plugins > Installed plugins** 진입
2. 검색창에 `pipeline-utility-steps` 입력
3. 설치되어 있고 활성화 상태이면 OK
4. 미설치 시 **Available plugins** 탭에서 동명 플러그인 설치 (재시작 불필요, hot-load 가능)

### 동작 검증 (선택)

스크립트 콘솔(`/script`)에서 다음을 실행하여 step 가용성을 확인할 수 있다.

```groovy
println(Jenkins.instance.pluginManager.getPlugin('pipeline-utility-steps')?.version)
```

`null` 이 출력되면 미설치 상태다.

### 체크리스트

- [ ] QA Jenkins 인스턴스에 `pipeline-utility-steps` 설치/활성화
- [ ] Stage Jenkins 인스턴스에 `pipeline-utility-steps` 설치/활성화
- [ ] Prod Jenkins 인스턴스에 `pipeline-utility-steps` 설치/활성화

## Step 4 — 활성화 검증 절차

3개 단계 등록 완료 후, 환경 1개(권장: QA) 에서 SERVICE=auto 빌드를 실행하여 monitor 통합이 동작하는지 검증한다.

### 검증 시나리오

1. **dispatcher 트리거**: QA Jenkins 에서 `shop-qa` 잡을 `SERVICE=auto` 로 수동 빌드 실행 (또는 PR merge 등 자동 트리거 사용)
2. **자식 빌드 생성 확인**: dispatcher 가 자식 빌드 N개를 트리거하고, 각 자식 description 에 `dispatcher: #N SERVICE=<svc>` prefix 가 기록 (#4816 join key)
3. **monitor 잡 트리거 확인**: dispatcher 빌드 로그에서 다음 줄 확인
   ```
   [DISPATCHER] monitor job triggered: shop-qa-dispatcher-monitor/<branch>
   ```
   `monitor trigger failed (fail-open)` 이 보이면 Step 1 잡 등록 누락 또는 잡 이름 불일치.
4. **monitor 빌드 실행 확인**: `shop-qa-dispatcher-monitor` 잡에 신규 빌드가 즉시 시작
5. **dispatcher description 역기록 확인**: dispatcher 빌드 페이지의 description 끝에 ` MONITOR=<monitor build url>` 이 추가됨
   - 누락 시: monitor 빌드 로그에서 `[MONITOR] dispatcher reverse link failed (fail-open)` 메시지 확인 → Step 2 credential 미등록 또는 API token 미사용 (#4832) 가능성
6. **자식 빌드 결과 폴링 확인**: monitor 빌드 로그에 다음 패턴 출현
   ```
   [MONITOR] poll 1/120, pending=[user-service, product-service, ...]
   [MONITOR] all N services completed at iteration M
   ```
7. **통합 Slack 알림 수신 확인**: 자식 빌드 N개 종결 후 monitor 잡이 통합 결과를 한 번의 Slack 메시지로 발송. 환경별 채널에 다음 정보 포함:
   - dispatcher 빌드 번호 + URL
   - 서비스별 결과 (SUCCESS / FAILURE / UNSTABLE / PENDING)
   - 각 자식 빌드 URL

### 체크리스트

- [ ] QA SERVICE=auto 빌드 1회 실행
- [ ] dispatcher 로그에 `monitor job triggered:` 메시지 확인
- [ ] `shop-qa-dispatcher-monitor` 잡에 신규 빌드 생성 확인
- [ ] dispatcher 빌드 description 끝에 ` MONITOR=<url>` append 확인
- [ ] monitor 빌드 로그에 polling iteration 메시지 정상 출력
- [ ] Slack 채널에 통합 알림 1건 수신 확인 (자식 빌드 N개에 대한 N개 알림이 아님)
- [ ] Stage 환경에서 동일 검증
- [ ] Prod 환경에서 동일 검증

### Stage / Prod 활성화

QA 에서 위 7단계가 모두 통과하면 Stage 와 Prod 도 동일 절차로 검증한다. Prod 는 트래픽 영향이 크므로 자연 발생하는 SERVICE=auto 트리거(예: dev → main merge) 시점에 Slack 알림 수신만 모니터링하는 비파괴 검증을 권장한다.

## 롤백 절차

활성화 후 monitor 잡이 비정상 동작(예: 무한 폴링, 잘못된 Slack 알림 발송)이 확인되면 다음 순서로 비활성화한다.

### 옵션 A — 잡 단위 비활성화 (권장)

monitor 잡만 일시 비활성화. dispatcher 는 fail-open 으로 정상 동작 유지.

1. Jenkins 콘솔에서 `shop-{env}-dispatcher-monitor` 잡 진입
2. **Disable Project** 클릭 (또는 Multibranch 의 경우 해당 브랜치 disable)
3. dispatcher 의 `triggerDispatcherMonitor` 가 `build(...)` 호출 시 fail-open 처리 → echo 만 남고 dispatcher 본 흐름 정상 진행

### 옵션 B — Credential 제거

credential 만 제거하면 monitor 잡은 트리거되지만 폴링/역기록이 fail-open 으로 SUCCESS 종료되며 Slack 알림은 PENDING 으로 발송된다. 알림 자체를 차단하려면 옵션 A 를 사용한다.

### 옵션 C — 코드 레벨 롤백

monitor 트리거 자체를 비활성화하려면 `jenkins/resolve-and-register.groovy` 의 `triggerDispatcherMonitor` 진입부에 `return` 을 임시 추가한 PR 을 머지한다. 운영 활성화/비활성화는 코드 변경 없이 옵션 A 로 처리하는 것이 원칙이다.

### 롤백 검증

- [ ] 옵션 A 적용 후 dispatcher 빌드 로그에 `monitor trigger failed (fail-open)` 메시지 출현 (또는 build skip)
- [ ] dispatcher 본 흐름(자식 빌드 트리거)은 정상 진행 확인
- [ ] Slack 통합 알림이 더 이상 발송되지 않음 확인

## 관련 이슈

- [#4815](https://github.com/personal-service/marketnote/issues/4815) dispatcher-monitor 도입 기능 이슈 (#4820 머지 완료)
- [#4816](https://github.com/personal-service/marketnote/issues/4816) 자식 빌드 description prefix join key 도입
- [#4832](https://github.com/personal-service/marketnote/issues/4832) JENKINS_API credential 은 API token 사용 명시 (본 Runbook Step 2 참조)
- [#4833](https://github.com/personal-service/marketnote/issues/4833) 본 Runbook 발행 이슈 (운영 활성화)
- [#4713](https://github.com/personal-service/marketnote/issues/4713) Jenkins dispatcher 모드 통합 상위 이슈
