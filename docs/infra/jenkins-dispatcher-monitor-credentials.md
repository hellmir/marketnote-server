# Jenkins Dispatcher Monitor Credentials Runbook

## 배경

`Jenkinsfile.dispatcher-monitor` 는 dispatcher 빌드의 자식 빌드 결과를 집계하면서 두 가지 Jenkins REST 호출을 수행한다.

- `GET  {baseUrl}/job/.../{dispatcherNum}/api/json?tree=description` — 기존 description 조회
- `POST {baseUrl}/job/.../{dispatcherNum}/submitDescription` — `MONITOR=<url>` append

두 호출 모두 `${ENV_PREFIX}_JENKINS_API` credential 의 `usernamePassword` 바인딩(`J_USER:J_TOKEN`)을 Basic Auth 로 사용한다 (`jenkins/resolve-and-register.groovy` L791-L818, L845-L). password 필드에는 **반드시 Jenkins API token 을 등록**해야 한다. 사용자 로그인 패스워드를 등록하면 `submitDescription` POST 가 401/403 으로 실패한다.

### 왜 API token 이어야 하는가 — CSRF crumb 면제 정책

Jenkins 2.176.2 / 2.222 이후 `hudson.security.csrf.CrumbFilter` 는 **API token 으로 인증된 Basic Auth 요청에 한해 crumb 검증을 면제**한다. 사용자 패스워드를 사용한 Basic Auth 는 면제 대상이 아니므로 POST 요청 시 `403 No valid crumb was included in the request` 가 반환된다.

`appendDispatcherMonitorLink` 의 `submitDescription` POST 는 crumb 을 별도로 fetch 하지 않고 Authorization 헤더 단독으로 호출하므로 **API token 사용이 강제 요건**이다. GET (`/api/json`) 만 사용한다면 패스워드도 통과하지만, 본 파이프라인은 같은 credential 로 GET + POST 를 모두 수행하므로 단일 token 정책으로 통일한다.

> 참고: 이 정책은 GitHub `personal-service/marketnote` 인프라 컨벤션이며, 모든 `${ENV_PREFIX}_JENKINS_API` 사용처(`appendDispatcherMonitorLink`, `pollDispatcherChildren` 등)가 동일하게 API token 을 가정한다.

## Credential 사양

| 항목         | 값                                                          |
| ------------ | ----------------------------------------------------------- |
| 타입         | Username with password                                      |
| Scope        | Global (Jenkins, nodes, items, all child items, etc)        |
| Username     | API token 을 발급한 Jenkins 계정의 username                 |
| Password     | **해당 계정에서 발급한 Jenkins API token** (사용자 패스워드 금지) |
| ID           | `${ENV_PREFIX}_JENKINS_API` (아래 환경별 매핑 참고)         |
| Description  | `Jenkins API token for dispatcher monitor (env={qa|stage|prod})` 권장 |

### 환경별 credential ID 매핑

| ENV_PREFIX     | Credential ID            | 용도                                   |
| -------------- | ------------------------ | -------------------------------------- |
| `MARKETNOTE_QA`      | `MARKETNOTE_QA_JENKINS_API`    | qa 환경 dispatcher monitor             |
| `MARKETNOTE_STAGE`   | `MARKETNOTE_STAGE_JENKINS_API` | stage 환경 dispatcher monitor          |
| `MARKETNOTE_PROD`    | `MARKETNOTE_PROD_JENKINS_API`  | prod 환경 dispatcher monitor           |

`Jenkinsfile.dispatcher-monitor` 의 `ENV_PREFIX` 파라미터 값에 `_JENKINS_API` 접미사가 붙어 credential 이 lookup 된다.

## API token 발급 절차

1. Jenkins UI 우상단 사용자 메뉴 → **Configure** (또는 `${JENKINS_URL}/me/configure`)
2. **API Token** 섹션 → **Add new Token** 클릭
3. Token name 입력 (예: `dispatcher-monitor-qa`, `dispatcher-monitor-prod`) → **Generate**
4. 생성된 토큰 문자열을 즉시 복사 (재표시 불가, 분실 시 재발급 필요)
5. Jenkins **Manage Jenkins** → **Credentials** → **System** → **Global credentials (unrestricted)** → **Add Credentials**
6. 위 [Credential 사양](#credential-사양) 표대로 입력하여 저장
7. 동일 절차를 환경별(qa/stage/prod)로 별도 token + 별도 credential ID 로 반복

> 운영 보안: 환경별로 토큰을 분리해 prod 토큰이 qa/stage 에 노출되지 않도록 한다. 토큰 회전(rotation) 시에도 환경별 독립 회전.

## 권한 요구사항

API token 을 발급한 계정은 dispatcher 잡에 대해 다음 권한이 필요하다.

- **Job/Read** — `api/json` 으로 description/builds 조회
- **Job/Configure** 또는 동급의 description 수정 권한 — `submitDescription` POST
- **Job/Build** (선택) — 본 파이프라인은 monitor 가 dispatcher 를 트리거하지 않으므로 불필요

dispatcher 잡이 폴더에 속해 있으면 해당 폴더 레벨에서 권한이 inherit 되어야 한다. Role-based Authorization Strategy 사용 시 `dispatcher-monitor-bot` 등 전용 role 분리 권장.

## 검증 절차

credential 등록 후 다음 순서로 검증한다.

1. dispatcher 빌드를 1회 실행하여 monitor 빌드가 자동 트리거되는지 확인
2. monitor 빌드 콘솔 로그에서 다음 라인 확인
   - `[MONITOR] dispatcher #N description updated with monitor link` — submitDescription POST 성공
   - `[MONITOR] dispatcher reverse link failed (fail-open): ...` — 실패 (패스워드 사용 등 가능성, 하단 [증상별 트러블슈팅](#증상별-트러블슈팅) 참조)
3. dispatcher 빌드의 description 끝에 ` MONITOR=<monitor-url>` 이 append 되어 있는지 UI 에서 확인
4. monitor 빌드의 자식 폴링 결과(`MONITOR_RESULTS_JSON`)가 정상 채워지는지 확인

> `appendDispatcherMonitorLink` 는 fail-open 으로 실패해도 monitor 폴링은 계속되지만, 운영 추적성 확보를 위해 첫 빌드에서 반드시 응답 코드/로그 확인한다.

## 증상별 트러블슈팅

### `[MONITOR] dispatcher reverse link failed (fail-open): ...`

| 증상                                                 | 원인                                              | 조치                                          |
| ---------------------------------------------------- | ------------------------------------------------- | --------------------------------------------- |
| `403 No valid crumb was included in the request`     | password 필드에 사용자 패스워드 등록 (CSRF 면제 미적용) | API token 으로 교체                           |
| `401 Unauthorized`                                   | username/token 불일치 또는 token 만료/revoke      | 토큰 재발급 후 credential password 갱신       |
| `403 Forbidden` (crumb 메시지 아님)                  | 계정에 Job/Configure 권한 없음                    | dispatcher 잡 권한 부여                       |
| `CredentialNotFoundException`                        | `${ENV_PREFIX}_JENKINS_API` credential 미등록     | 위 [발급 절차](#api-token-발급-절차) 수행      |

### dispatcher description 에 `MONITOR=...` 이 append 되지 않음

- monitor 콘솔 로그의 `[MONITOR] ...` 라인을 먼저 확인
- 이미 `MONITOR=` 가 포함되어 있으면 skip 됨 (재실행 멱등성 — `resolve-and-register.groovy` L805-L808)

## 변경 이력

- 2026-11-09 (#4832) — 운영 docs 초안 작성. dispatcher-monitor 파이프라인의 API token 사용 정책 명문화.
