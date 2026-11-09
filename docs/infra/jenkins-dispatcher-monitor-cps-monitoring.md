# Jenkins Dispatcher-Monitor CPS 직렬화 모니터링 Runbook

## 배경과 목적

#4815 (Jenkins dispatcher 모드 자식 빌드 결과 집계 monitor job 도입) 의 핵심 함수
`pollDispatcherChildren` (`jenkins/resolve-and-register.groovy`) 은 본 프로젝트의
Jenkins shared helper 에서 **처음으로 등장하는 패턴 조합**이다.

- `for (int i = 0; i < maxIterations; i++)` 명시 for-loop
- 다중 closure: `services.each`, `services.findAll`, `builds.each`, `services.any`
- closure 내부에서 Pipeline step 호출: `sh`, `parentScript.readJSON`, `sleep`, `echo`
- Groovy 정규식 Matcher (`desc =~ /dispatcher: #${dispatcherNum} SERVICE=(\S+)/`)

위 조합은 Jenkins Pipeline CPS(Continuation Passing Style) 변환과 충돌할 위험이
구조적으로 존재한다. #4815 PR 리뷰 단계에서 정적 분석으로 CPS-safe 판정되었으나,
**런타임 직렬화 동작은 운영 환경에서 실측해야만 최종 검증된다.**

이 Runbook 은 monitor job 첫 운영 투입 시점에 다음을 수행한다.

- 첫 실행 console log 에서 직렬화 경고 부재 확인
- Jenkins agent 재기동/durability 시점 빌드 복원 동작 검증
- `NotSerializableException` / `NotCpsSerializable` 발생 시 대응 절차

## CPS 직렬화 메커니즘 (간단 요약)

Jenkins Pipeline 은 매 Pipeline step (`sh`, `sleep`, `readJSON`, `withCredentials`,
`withEnv`, `parallel` 등) 진입 직전에 현재 **Groovy 호출 스택과 외부에서 캡처된
변수**를 직렬화하여 디스크에 저장한다.

이를 통해 다음이 가능해진다.

- Jenkins controller 재기동 시 빌드 재개 (durability)
- agent 일시 단절 시 step 결과를 받아 진행
- Pipeline 진행 상황 history/replay

직렬화 시점에 **`Serializable` 을 구현하지 않은 객체가 호출 스택/closure 캡처
변수에 남아 있으면** `java.io.NotSerializableException` 이 발생하여 빌드가 실패한다.

대표적 비-Serializable 객체:

- `java.util.regex.Matcher` (정규식 매칭 결과 — 본 함수에서 사용)
- `java.util.regex.Pattern` (일부 JDK 구현)
- `java.io.InputStream`, `java.io.Reader`
- 익명 클래스 인스턴스 (캡처 변수에 비-Serializable 객체 포함 시 전파)

본 함수는 closure 내부에서 Matcher 를 사용하므로 **Pipeline step (`sleep`,
`sh`) 진입 전 Matcher 가 반드시 해제되어야 한다.**

## 현재 pollDispatcherChildren 직렬화 위험 분석

`jenkins/resolve-and-register.groovy` L827~907 기준.

### closure 캡처 변수 인벤토리

| 변수 | 타입 | Serializable 여부 | 비고 |
| --- | --- | --- | --- |
| `envPrefix` | String | OK | params 값 |
| `parentJobName` | String | OK | params 값 |
| `dispatcherNum` | String | OK | params 값 |
| `services` | ArrayList\<String\> | OK | split + collect 결과 |
| `intervalSec`, `timeoutMin`, `maxIterations` | int | OK | primitive |
| `results` | LinkedHashMap\<String, LinkedHashMap\<String, ?\>\> | OK | 내부 값 모두 String/Long/null |
| `startTime` | long | OK | primitive |
| `timedOut` | boolean | OK | primitive |
| `jobPath`, `baseUrl`, `listUrl` | String | OK | URL 문자열 |
| `remainingServices` | ArrayList\<String\> | OK | findAll 결과 |
| `raw` | String | OK | sh stdout |
| `json`, `builds` | net.sf.json.JSONObject / JSONArray | OK | readJSON 반환 (Serializable) |
| `b` | net.sf.json.JSONObject | OK | builds 반복 변수 |
| `desc` | String | OK | toString 결과 |
| `matcher` | java.util.regex.Matcher | **NOT Serializable** | **반드시 step 진입 전 해제 필요** |
| `svc` | String | OK | matcher.group 결과 |

### Matcher 해제 처리

L871~882 (Fix 적용본):

```groovy
def matcher = (desc =~ /dispatcher: #${dispatcherNum} SERVICE=(\S+)/)
if (matcher.find()) {
    def svc = matcher.group(1)
    // ... results 갱신 ...
}
matcher = null   // ← Matcher 변수 명시 해제 (CPS-safe 보장)
```

`matcher = null` 이 **`builds.each` closure body 마지막에 위치**하여, 다음 반복
또는 closure 종료 후 `sleep` step 진입 시점에 호출 스택/캡처 변수에 Matcher
참조가 남지 않는다.

### Pipeline step 진입 지점 매핑

`pollDispatcherChildren` 본문에서 직렬화 발생 가능 지점:

| 위치 | step | 진입 직전 호출 스택 |
| --- | --- | --- |
| L854 | `parentScript.withEnv` | for-loop 외부 — Matcher 미존재 |
| L863 | `sh(curl ...)` | for-loop iteration 시작 직후 — Matcher 미존재 |
| L867 | `parentScript.readJSON` | sh 직후 — Matcher 미존재 |
| L869~883 | `builds.each` 내부 | step 호출 없음 (echo 만 존재, echo 는 step 이지만 직렬화 트리거 아님) |
| L888 | `sleep` | iteration 종료 직전 — **Matcher 가 null 처리되어야 함** |

L869~883 `builds.each` 내부는 Pipeline step 을 호출하지 않으므로 Matcher 가
closure 내부에 임시 존재해도 직렬화 위험이 없다. **직렬화는 L888 `sleep`
진입 시점**에 발생하며, 이 시점에는 `matcher = null` 처리되어 closure 캡처 변수
스코프에서 제거된 상태다.

### 잠재 위험 (운영 검증 항목)

정적 분석상 안전하나 다음은 런타임 검증이 필요하다.

1. **Groovy CPS 변환기가 closure 내부 `def matcher = ...` 를 외부 캡처 변수로
   취급하는지** — closure body 종료 후에도 참조가 남는 구현이 있다면 `matcher = null`
   할당 후에도 직렬화 시 NotSerializableException 발생 가능
2. **`for (int i ...)` + closure 조합에서 CPS 변환기가 i 를 closure 외부 변수로
   끌어올리는지** — for-loop 내부에서 정의된 `remainingServices`, `raw`, `json`,
   `builds` 등이 다음 iteration 까지 잔존하는지 (sleep step 직렬화 시점 영향)
3. **Jenkins agent 재기동 후 durability 복원 시 closure 호출 스택 복원이 정확한지**

이 3개 항목은 **첫 monitor 빌드 console log 와 agent 재기동 시 빌드 동작**으로만
검증 가능하다.

## 첫 실행 후 console log 점검 체크리스트

monitor 빌드 1회 실행 후 console output 을 다운로드하여 다음 키워드를 검색한다.

```bash
# Jenkins UI 또는 CLI 로 console log 다운로드
curl -sf -u "$J_USER:$J_TOKEN" \
  "${JENKINS_URL}/job/<project>-dispatcher-monitor/job/<branch>/<buildNum>/consoleText" \
  > /tmp/monitor-console.log

# 직렬화 관련 키워드 grep (모두 0건이어야 함)
grep -nE 'NotSerializableException|NotCpsSerializable|java\.io\.NotSerializable' /tmp/monitor-console.log
grep -nE 'java\.util\.regex\.Matcher.*serial|Pattern\.serial' /tmp/monitor-console.log

# CPS 변환 경고 (0건 권장)
grep -nE 'CpsCallableInvocation|MissingMethodException.*cps|RejectedAccessException' /tmp/monitor-console.log

# durability/재개 관련 키워드 (정상 동작 확인용)
grep -nE 'Resuming build|Pipeline step.*durability|FlowExecutionList' /tmp/monitor-console.log

# 정상 진행 키워드 (모니터 진입/완료 로그)
grep -nE '\[MONITOR\] poll [0-9]+/[0-9]+' /tmp/monitor-console.log | head -5
grep -nE '\[MONITOR\] all [0-9]+ services completed' /tmp/monitor-console.log
grep -nE '\[MONITOR\] timeout after' /tmp/monitor-console.log
```

### 판정 기준

| 항목 | 기대 결과 | 위반 시 대응 |
| --- | --- | --- |
| `NotSerializableException` 키워드 | **0건** | "위반 발생 시 대응" 섹션 즉시 적용 |
| `NotCpsSerializable` 키워드 | **0건** | 동일 |
| `RejectedAccessException` | **0건** | sandbox 정책 검토 (별도 이슈) |
| `[MONITOR] poll N/M` 로그 | **N=1..M 순차 출력** | 누락 시 for-loop CPS 변환 이슈 가능성 |
| `[MONITOR] all N services completed` 또는 `timeout after Mmin` | **둘 중 하나 출력** | 둘 다 없으면 monitor 가 비정상 종료 (예외 stacktrace 분석) |

### 운영 이력 기록

첫 monitor 빌드 후 본 Runbook 끝의 "수행 이력" 표에 다음을 기록한다.

- 빌드 번호, 환경(QA/STAGE/PROD)
- 직렬화 경고 발생 여부 (0건 / N건)
- monitor 결과 (all-success / partial-failed / timeout)
- 특이사항

## Jenkins agent 재기동 시 복원 검증

monitor 빌드는 최대 60분 (`POLL_TIMEOUT_MINUTES=60`) 폴링한다. 이 동안 Jenkins
controller / agent 재기동이 발생할 수 있으며, durability 가 정상 동작해야
빌드가 재개된다.

### 의도적 재기동 검증 (운영 적용 전 1회 권장)

QA 환경에서 monitor 빌드가 진행 중일 때 의도적으로 agent restart 를 발생시켜
durability 동작을 확인한다.

```bash
# 1) QA dispatcher 트리거 → monitor 빌드 실행 시작 (별도 터미널에서 console 모니터링)

# 2) monitor 빌드가 [MONITOR] poll 3/120 정도 진행된 시점에 agent 재기동
#    (controller 재기동은 영향 범위가 크므로 agent 만 재기동)
ssh personal-jenkins
sudo systemctl restart jenkins-agent   # 또는 해당 환경의 agent 서비스명

# 3) monitor 빌드 console 로그에서 다음 시퀀스 확인
#    - "Cannot contact <agent>: ..." 또는 "channel stopped" 로그
#    - "Resuming build" / "Ready to run" 복원 로그
#    - [MONITOR] poll N/M 로그가 끊김 없이 이어지는지
```

### 판정 기준

- agent 재기동 후 monitor 빌드가 **재개되어 다음 polling iteration 으로 진행**
- console log 에 `NotSerializableException` 미출현
- 최종적으로 monitor 가 SUCCESS 또는 timeout 으로 정상 종료

### 위반 시

- agent 재기동 직후 monitor 빌드가 ABORTED/FAILURE 로 종료되며 stacktrace 에
  `NotSerializableException` 출현 → "위반 발생 시 대응" 적용

### 의도적 재기동 검증 미수행 시

QA 운영 중 자연 발생하는 agent 재기동/네트워크 단절 이벤트로 첫 60분 windowed
검증을 대체할 수 있다. 다음 4주간 monitor 빌드 console log 에 위 키워드가 0건
유지되면 durability 안정성을 충분히 확보한 것으로 간주한다.

## 위반 발생 시 대응

`NotSerializableException` 또는 `NotCpsSerializable` 키워드가 console log 에서
1건이라도 발견되면 즉시 다음 절차를 적용한다.

### 1. 즉시 영향 평가

- 해당 빌드의 자식 빌드 결과 집계 누락 여부 확인 (`MONITOR_RESULTS_JSON` 환경변수)
- Slack `[DISPATCHER-MONITOR]` 알림 발송 여부 (실패 시 운영자 직접 확인 필요)
- monitor SUCCESS 유지 여부 (fail-open 정책상 모니터 자체는 성공 종료)

### 2. closure → 명시 for-loop 변환 패턴

`builds.each { b -> ... }` 를 명시 for-loop 으로 전환하면 closure 캡처 스코프
자체가 사라져 직렬화 위험이 제거된다.

**현재 (closure 사용):**

```groovy
builds.each { b ->
    def desc = (b.description ?: '').toString()
    def matcher = (desc =~ /dispatcher: #${dispatcherNum} SERVICE=(\S+)/)
    if (matcher.find()) {
        def svc = matcher.group(1)
        if (results.containsKey(svc) && results[svc].result == null && b.result != null) {
            results[svc].result = b.result
            results[svc].url = b.url
            results[svc].number = b.number
            results[svc].duration = b.duration
            echo "[MONITOR] ${svc} completed: ${b.result} #${b.number}"
        }
    }
    matcher = null
}
```

**전환 후 (명시 for-loop):**

```groovy
for (int j = 0; j < builds.size(); j++) {
    def b = builds[j]
    def desc = (b.description ?: '').toString()
    def parsed = extractServiceFromDescription(desc, dispatcherNum)
    if (parsed != null && results.containsKey(parsed) && results[parsed].result == null && b.result != null) {
        results[parsed].result = b.result
        results[parsed].url = b.url
        results[parsed].number = b.number
        results[parsed].duration = b.duration
        echo "[MONITOR] ${parsed} completed: ${b.result} #${b.number}"
    }
}
```

추가로 Matcher 처리를 `@NonCPS` helper 로 분리한다.

```groovy
@NonCPS
def extractServiceFromDescription(String desc, String dispatcherNum) {
    def matcher = (desc =~ /dispatcher: #${dispatcherNum} SERVICE=(\S+)/)
    return matcher.find() ? matcher.group(1) : null
}
```

`@NonCPS` 메서드 내부는 일반 Groovy 실행으로 처리되어 Matcher 가 메서드 종료
시점에 스택 프레임과 함께 사라진다. 호출자 측에는 `String` 만 반환되므로
직렬화 안전성이 구조적으로 보장된다.

### 3. 동일 패턴 다른 closure 점검

`pollDispatcherChildren` 외에도 closure + Pipeline step + Matcher 조합이 있는지
재점검한다.

```bash
# 같은 helper 파일 내 closure + step 조합 grep
grep -nE '\.each\s*\{|\.findAll\s*\{|\.collect\s*\{|\.any\s*\{' jenkins/resolve-and-register.groovy
grep -nE '=~|Pattern\.compile|Matcher' jenkins/resolve-and-register.groovy

# Jenkinsfile 전체에서도 동일 점검
grep -rnE '\.each\s*\{[^}]*\b(sh|sleep|readJSON|writeJSON|withEnv|withCredentials)\b' Jenkinsfile* jenkins/
```

발견된 closure 가 내부에서 Pipeline step 을 호출하면서 Matcher/Pattern 등
비-Serializable 객체를 캡처하면 동일 패턴으로 전환한다.

### 4. 회귀 방지

- 위반 발생 빌드 console log 발췌를 GitHub 이슈에 첨부
- `@NonCPS` helper 분리 PR 발행
- 이 Runbook "수행 이력" 표에 violation 사례 기록

## 관련 파일

- `jenkins/resolve-and-register.groovy` (L827~907) — `pollDispatcherChildren` 본문
- `Jenkinsfile.dispatcher-monitor` — monitor job 정의 (helper.pollDispatcherChildren 호출 지점)
- `Jenkinsfile` — dispatcher 모드 (monitor 트리거 지점)

## 관련 이슈

- #4815 dispatcher 모드 자식 빌드 결과 집계 monitor job 도입
- #4816 dispatcher monitor child build number 추적
- #4821 Jenkinsfile CPS WorkflowScript method too large 한계 초과 (helper 이관 선례)
- #4829 dispatcher-monitor `pollDispatcherChildren` CPS 직렬화 운영 모니터링 (본 Runbook 발행 이슈)

## 수행 이력

| 날짜 | 환경 | 빌드 # | 직렬화 경고 | monitor 결과 | 담당 | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| | | | | | | |
