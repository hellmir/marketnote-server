# Jenkins Markup Formatter Runbook

## 배경과 목적

Jenkins 의 빌드 description 필드는 `Manage Jenkins → Configure Global Security → Markup Formatter` 설정에 따라 다음 중 하나의 방식으로 렌더링된다.

- **Plain text**: HTML 메타문자 escape, 태그 무력화 (안전)
- **Safe HTML** (OWASP AntiSamy 기반): 화이트리스트 태그만 허용, `<script>`/이벤트 핸들러 차단 (안전)
- **Raw HTML**: 입력 그대로 렌더링, `<script>` / `onerror=` / `<iframe>` 등 모두 허용 (Stored XSS 표면)

본 레포는 `currentBuild.description` 을 다수 위치에서 설정하며, 그 데이터 출처에 사용자/외부 제어 입력 (예: dispatcher 빌드 파라미터, SERVICE 이름, git commit hash) 이 합성된다. Markup Formatter 가 `Raw HTML` 로 설정되어 있으면, dispatcher 빌드를 트리거할 권한을 가진 누구든 description sink 를 통해 Stored XSS 를 트리거할 수 있다.

이 Runbook 은 다음을 명문화한다.

- 운영 콘솔에서 현재 Markup Formatter 설정을 확인하는 절차
- 권장 설정 (`Plain text` 우선, 필요 시 `Safe HTML`) 과 그 근거
- `Raw HTML` 로 설정된 경우 변경 절차와 영향 평가
- 변경 후 검증 절차

### 관련 이슈 / 변경 맥락

- #4816 자식 빌드 description 에 dispatcher BUILD_NUMBER prefix 합성 — 자식 → 부모 역기록 sink 추가
- #4815 dispatcher-monitor job 에서 monitor URL 을 dispatcher description 끝에 append — 외부 monitor sink 추가
- #4817 Slack 메시지 sink (별도 조치, 본 Runbook 범위 외)
- #4827 (본 이슈) Markup Formatter 설정 가시성 확보

description sink 가 다중 경로로 확장되면서 단일 화이트리스트로 출력 sanitize 를 보장하기 어려운 구조가 되었다. 따라서 입력 단 sanitize 가 아닌 **렌더링 단에서 markup formatter 로 무력화**하는 것이 회귀 안전한 방어 라인이다.

## 현재 description sink 인벤토리

`currentBuild.description` 이 설정되는 위치는 아래와 같다. 모두 본 레포 내 코드이며 외부 plugin/job 설정은 별도 점검 대상이다.

### dispatcher 빌드 (parent)

`jenkins/resolve-and-register.groovy`

| 라인 | 설정 값 | 데이터 출처 |
| --- | --- | --- |
| L558 | `'[DISPATCHER] no deploy target'` | 정적 문자열 |
| L579 | `"[DISPATCHER] re-triggered ${candidates.size()}: ${candidates.join(',')}"` | candidates = `env.CANDIDATE_SERVICES` 파싱 결과 (서비스명 화이트리스트 문자열) |

### 자식 빌드 (per-service)

`jenkins/resolve-and-register.groovy` — `composeChildDescription()` (L672) 경유

| 라인 | suffix 입력 | 결과 description |
| --- | --- | --- |
| L644 | `"SERVICE=${env.SERVICE_NAME}"` | `dispatcher: #N SERVICE=<svc>` 또는 suffix 만 (수동/cron 트리거) |
| L687 | `"SERVICE=${env.SERVICE_NAME} COMMIT=${env.GIT_COMMIT}"` (post.success) | 동일 prefix + COMMIT |
| L714 | failed suffix (post.failure) | 동일 prefix + 실패 표시 |

`composeChildDescription` 은 `parentScript.params.DISPATCHER_BUILD_NUMBER` 를 prefix 로 합성한다. 이 파라미터 자체는 dispatcher 빌드가 자식 트리거 시 `env.BUILD_NUMBER` 를 전달하므로 통상 정수 문자열이지만, **사용자가 자식 job 을 수동 트리거할 때 임의 문자열을 입력 가능**하다.

### dispatcher-monitor job

`Jenkinsfile.dispatcher-monitor`

| 라인 | 설정 값 | 데이터 출처 |
| --- | --- | --- |
| L48 | `"MONITOR for dispatcher: #${params.DISPATCHER_BUILD_NUMBER} SERVICES=${params.SERVICES}"` | 빌드 파라미터 (사용자 입력) |

또한 `jenkins/resolve-and-register.groovy` L780 부근의 `appendMonitorLinkToDispatcherDescription` 은 dispatcher 빌드의 description 끝에 monitor URL 을 REST POST `submitDescription` 으로 append 한다. 이 sink 는 dispatcher description 에 외부 호출 경로로 데이터를 주입한다.

### 입력 출처 요약

description 합성에 들어가는 사용자/외부 제어 입력:

- `params.DISPATCHER_BUILD_NUMBER` — 정수 기대, 자식 job 수동 트리거 시 임의 문자열 가능
- `params.SERVICES` — 콤마 구분 서비스명, 문자열 검증 없음
- `env.SERVICE_NAME` — Jenkins job 이름 기반, 잡 생성 권한 보유자 제어 가능
- `env.GIT_COMMIT` — Jenkins 가 채우는 commit SHA (영숫자 40자), 통상 안전
- monitor URL (REST submitDescription) — `${baseUrl}/${jobPath}/${dispatcherNum}/...` 형태, dispatcherNum 외 정적

위 입력 중 일부라도 화면 렌더링 시 Raw HTML 로 처리되면 `<script>` / `<img onerror=...>` / `<iframe>` 주입이 가능하다.

## 운영 콘솔 확인 절차

1. Jenkins 관리자 계정으로 로그인
2. 좌측 메뉴 `Manage Jenkins` 클릭
3. `Security` 섹션의 `Configure Global Security` 클릭 (구버전: `Configure Global Security` 단독 메뉴)
4. 페이지 내 `Markup Formatter` 섹션 확인
   - 드롭다운 현재 값 기록
5. 값이 `Plain text` 또는 `Safe HTML` 이면 정상, `Raw HTML` 이면 본 Runbook 의 변경 절차 수행

### CLI 로 확인 (선택)

Jenkins CLI 또는 Script Console (`Manage Jenkins → Script Console`) 에서 다음 Groovy 로 즉시 확인 가능하다.

```groovy
def fmt = jenkins.model.Jenkins.instance.markupFormatter
println "class: ${fmt.class.name}"
println "descriptor: ${fmt.descriptor.displayName}"
```

기대 출력 매핑:

- `hudson.markup.EscapedMarkupFormatter` → **Plain text**
- `hudson.markup.RawHtmlMarkupFormatter` (또는 OWASP AntiSamy 기반 Safe HTML) → **Safe HTML**
- `hudson.markup.RawHtmlMarkupFormatter` 가 raw 모드로 설정 → **Raw HTML** (변경 필요)

> 주의: 클래스명만으로 Plain text / Safe HTML / Raw HTML 을 한 번에 구분하기 어려운 케이스가 있다. UI 의 드롭다운 값을 최종 판단 근거로 한다.

## 권장 설정

### 권장: `Plain text`

근거:

- 본 레포 description 본문은 단순 키=값 문자열 (`SERVICE=<svc> COMMIT=<sha>`, `[DISPATCHER] re-triggered ...`) 만 사용한다. HTML 마크업이 필요 없다.
- monitor URL append (#4815) 에서 URL 은 평문으로 노출되어도 충분하다 (화면에서 자동 링크화는 부수 기능, 본질 아님).
- Plain text 는 모든 description sink 의 모든 입력을 균일하게 escape 한다 — 입력 출처가 늘어나도 회귀 안전.

### 차선: `Safe HTML`

근거:

- 일부 운영자가 description 에 `<a>` 태그로 외부 링크를 직접 삽입하는 운영 관행이 있다면 Safe HTML 이 필요하다.
- OWASP AntiSamy 화이트리스트로 `<script>` / 이벤트 핸들러는 차단된다.
- 단점: AntiSamy 정책 파일이 변경되면 화이트리스트 변동 가능성. Plain text 보다 회귀 표면이 넓다.

### 금지: `Raw HTML`

- description 에 들어가는 모든 입력이 escape 없이 렌더링된다.
- 빌드 파라미터를 입력할 권한을 가진 사용자가 임의 JavaScript 를 다른 사용자의 브라우저에서 실행시킬 수 있다 (Stored XSS).
- Jenkins 공식 문서도 Raw HTML 을 명시적으로 비권장한다.

## 변경 절차

### 1. 사전 영향 평가

변경 전에 현재 시점에 존재하는 빌드 description 들을 점검한다.

- 최근 30일 dispatcher 빌드 / 자식 빌드 / dispatcher-monitor 빌드의 description 중 HTML 태그 사용 사례가 있는지 sampling
- 운영자가 수동으로 `<a href=...>` 등을 description 에 넣은 빌드가 있다면 Plain text 전환 시 해당 빌드 화면에서 태그가 평문으로 표시됨 (보안상 정상 동작이나 운영자에게 사전 공지 필요)

Script Console 로 sampling 가능:

```groovy
import jenkins.model.Jenkins
Jenkins.instance.allItems(hudson.model.Job).each { job ->
    job.builds.limit(5).each { b ->
        def d = b.description
        if (d && (d.contains('<') || d.contains('&'))) {
            println "${job.fullName} #${b.number}: ${d}"
        }
    }
}
```

출력이 비어 있으면 description 에 HTML 마크업을 사용하는 운영 관행이 없으므로 Plain text 로 안전하게 전환 가능.

### 2. 백업 (선택)

설정 변경은 `$JENKINS_HOME/config.xml` 의 `<markupFormatter>` 엘리먼트에 반영된다. 변경 전 해당 파일 백업.

```bash
sudo cp /var/lib/jenkins/config.xml /var/lib/jenkins/config.xml.bak.$(date +%Y%m%d-%H%M%S)
```

### 3. UI 설정 변경

1. `Manage Jenkins → Configure Global Security` 진입
2. `Markup Formatter` 드롭다운에서 `Plain text` 선택 (또는 차선 `Safe HTML`)
3. 페이지 하단 `Save` 클릭

### 4. 사후 영향 평가

- 기존 빌드 description 화면을 sampling 하여 평문/HTML 렌더링이 의도대로 변경되었는지 확인
- description 에 HTML 마크업이 들어 있던 과거 빌드는 Plain text 전환 후 `<a href="...">link</a>` 가 평문으로 표시된다 — 운영자에게 사전 공지된 정상 동작
- `composeChildDescription` 결과 (`dispatcher: #N SERVICE=<svc> COMMIT=<sha>`) 는 HTML 메타문자가 없어 시각적 변화 없음

## 검증 절차

### 1. 설정 반영 확인

`Manage Jenkins → Configure Global Security` 재진입하여 `Markup Formatter` 가 변경된 값으로 표시되는지 확인.

Script Console 로 추가 검증:

```groovy
def fmt = jenkins.model.Jenkins.instance.markupFormatter
println fmt.class.name
// 기대: hudson.markup.EscapedMarkupFormatter (Plain text)
```

### 2. XSS 무력화 확인 (Plain text 전환 후)

다음을 dispatcher 빌드 파라미터로 입력하여 자식 description 에 합성되는 경로를 점검한다.

- 임의 dispatcher 빌드를 수동 트리거하면서 `SERVICES` 파라미터에 다음 페이로드 삽입 (테스트 권한 보유자만 수행)

```
<img src=x onerror=alert(1)>
```

기대 동작:

- 자식 빌드 description 화면에서 위 문자열이 **평문으로 그대로 표시**되어야 한다 (`<` 가 `&lt;` 로 escape)
- 브라우저에서 alert 가 발생하면 **변경이 반영되지 않은 것** — Script Console 결과 / 페이지 캐시 / 다른 형식 설정 재확인

### 3. 정상 description 렌더링 확인

다음 경로의 description 이 평문으로 정상 표시되는지 확인:

- dispatcher 빌드: `[DISPATCHER] re-triggered N: svc1,svc2`
- 자식 빌드 success: `dispatcher: #N SERVICE=<svc> COMMIT=<sha>`
- dispatcher-monitor: `MONITOR for dispatcher: #N SERVICES=<svc1,svc2>`
- monitor URL append 후 dispatcher: `... MONITOR=<url>`

모든 description 이 시각적으로 동일하면 (HTML escape 가 시각 변화를 만들지 않으면) 회귀 없음.

### 4. 검증 실패 시

- Script Console 결과가 `RawHtmlMarkupFormatter` 로 남아 있으면 `Save` 버튼 누락 여부 / 권한 부족 여부 확인
- description 에 alert 가 발생하면 즉시 `Plain text` 로 재변경 후 영향 받은 빌드 description 수동 정리 (`submitDescription` REST 또는 UI 로 description 비움)
- `config.xml` 백업본으로 롤백:

  ```bash
  sudo systemctl stop jenkins
  sudo cp /var/lib/jenkins/config.xml.bak.<timestamp> /var/lib/jenkins/config.xml
  sudo systemctl start jenkins
  ```

## 후속 작업

- 본 Runbook 적용 후에도 Jenkins plugin 업데이트 / 인스턴스 교체 시 Markup Formatter 가 기본값으로 회귀할 가능성이 있다. 정기 점검 항목으로 등록 권고
- IaC (Configuration as Code Plugin / JCasC) 도입 시 `jenkins.security.markupFormatter` 를 명시적으로 코드화하면 재발 방지 가능. IaC 도입은 본 Runbook 범위 외로 별도 이슈 트래킹
- description 입력 단 sanitize (#4817 Slack 메시지 sink 와 유사) 는 별도 이슈로 분리. 본 Runbook 은 렌더링 단 무력화에만 집중

## 수행 이력

| 날짜 | 환경 | 담당 | 비고 |
| --- | --- | --- | --- |
| | | | |
