# Jenkins Sysstat Sampling Runbook

## 배경과 목적

#4551 (2GB Jenkins 인스턴스 Gradle JVM 메모리 포화로 인한 hang 방지) 분석 과정에서 장애 시점의 메모리/swap 급변 구간을 특정하기 어려웠다.

sysstat(`sar`)의 기본 샘플 주기가 **10분**이라 다음과 같이 한 샘플 차이로 상태가 급변했다.

- `%memused`가 한 샘플 사이에 `48.75% → 88.32%`로 상승
- `swpused`가 한 샘플 사이에 `25% → 99.99%`로 상승

이 때문에 OOM 진입 직전의 페이지 스캔/swap 진행 추이를 분 단위로 재구성할 수 없었다.

샘플 주기를 **1분**으로 단축하면 다음 가시성을 확보한다.

- 메모리 사용률 / 커밋 메모리 / available 메모리 분 단위 추이
- swap-in / swap-out 활성화 시점 분 단위 특정
- 페이지 스캔(`pgscank`, `pgscand`, `pgsteal`) 활성 구간 분 단위 특정
- load average / 러너블 태스크 수 분 단위 추이

데이터 증가량은 기존 대비 약 10배이나, `/var/log/sysstat`은 현재 수 MB 수준이어서 용량 한계 영향은 무시 가능하다.

## 대상 파일

**주 수집 주체: systemd timer `sysstat-collect.timer`** (Ubuntu 24.04+ sysstat 12.x / RHEL 계열 sysstat >= 11 공통).

- Timer 정의 (패키지 제공, **수정 금지**): `/usr/lib/systemd/system/sysstat-collect.timer`
- Service 정의 (패키지 제공, **수정 금지**): `/usr/lib/systemd/system/sysstat-collect.service`
- **변경 대상 (신규 drop-in)**: `/etc/systemd/system/sysstat-collect.timer.d/override.conf`
- 기본 설정: `OnCalendar=*:00/10` (10분 주기)
- HISTORY 설정 (보존 일수):
  - Debian/Ubuntu: `/etc/sysstat/sysstat`
  - RHEL 계열: `/etc/sysconfig/sysstat`

### `/etc/cron.d/sysstat` 은 수집 경로가 아니다

Ubuntu 24.04 sysstat 12.6.1 환경에서 실측한 결과, cron 라인은 동작하여 `debian-sa1` 호출 로그가 분 단위로 남지만 **`/var/log/sysstat/sa*` 파일의 mtime 은 `sysstat-collect.timer` 실행 시각(10분 주기)에만 갱신된다.** 즉 실제 샘플링/저장 주체는 systemd timer 다.

이 Runbook 은 `/etc/cron.d/sysstat` 을 **건드리지 않는다.** cron 수정만으로는 `sar -r` 데이터가 1분 주기로 쌓이지 않는다.

### 배포판/버전 확인

환경마다 수집 경로가 다를 수 있으므로 작업 전에 반드시 timer 유무를 확인한다.

```bash
# timer 가 존재하면 이 Runbook 절차 적용
systemctl cat sysstat-collect.timer

# 현재 OnCalendar 값과 마지막/다음 실행 시각
systemctl list-timers 'sysstat*' --no-pager
```

`sysstat-collect.timer` 가 존재하지 않는 구버전 sysstat(<=10) 환경이면 별도 경로(cron 중심) 가 필요하므로 이 Runbook 적용 대상이 아니다.

## 기본 구성

패키지 제공 정의 (수정 금지):

```ini
# /usr/lib/systemd/system/sysstat-collect.timer
[Unit]
Description=Run system activity accounting tool every 10 minutes

[Timer]
OnCalendar=*:00/10

[Install]
WantedBy=sysstat.service
```

```ini
# /usr/lib/systemd/system/sysstat-collect.service
[Service]
Type=oneshot
User=root
ExecStart=/usr/lib/sysstat/sa1 1 1
```

변경 후 drop-in (신규):

```ini
# /etc/systemd/system/sysstat-collect.timer.d/override.conf
[Timer]
OnCalendar=
OnCalendar=*:*:00
```

- 빈 `OnCalendar=` 한 줄로 패키지 기본값(`*:00/10`) 을 **리셋**한 뒤 새 값(`*:*:00`, 매 분 0초) 을 선언하는 systemd drop-in 관용 패턴
- 빈 줄을 생략하면 기본값과 새 값이 **누적**되어 10분 + 1분 양쪽으로 triggered 된다. 반드시 `OnCalendar=` 빈 줄 먼저 선언

## 적용 절차 (SSH 수동)

레포에 IaC(Ansible/Terraform) 가 없으므로 담당자가 Jenkins 인스턴스에 SSH 로 접속하여 직접 수행한다.

```bash
# 1) Jenkins 인스턴스 SSH 접속
ssh personal-jenkins

# 2) 기존 timer 설정 확인 (OnCalendar 값 기록)
systemctl cat sysstat-collect.timer
systemctl list-timers 'sysstat*' --no-pager

# 3) 적용 전 용량 측정 (순증 계산 근거)
du -sh /var/log/sysstat

# 4) drop-in 디렉터리 생성
sudo mkdir -p /etc/systemd/system/sysstat-collect.timer.d

# 5) override 파일 생성
#    빈 OnCalendar= 로 기존 값 리셋 후 새 값 선언 (systemd drop-in 관용)
sudo tee /etc/systemd/system/sysstat-collect.timer.d/override.conf > /dev/null <<'EOF'
[Timer]
OnCalendar=
OnCalendar=*:*:00
EOF

# 6) systemd reload 후 timer 재시작
sudo systemctl daemon-reload
sudo systemctl restart sysstat-collect.timer

# 7) 변경 확인
#    - [Timer] 섹션에 OnCalendar=*:*:00 (override) 가 최종 값으로 반영되어야 함
#    - list-timers 의 다음 실행 시각이 분 단위(다음 분:00)로 나와야 함
systemctl cat sysstat-collect.timer
systemctl list-timers 'sysstat*' --no-pager
```

## 검증

변경 직후 시각을 기록하고, 1분 주기 샘플 2개 이상 확보를 위해 최소 3분 대기 후 해당 시각 이후 레코드만 조회한다.

```bash
# 적용 직후 시각 기억
START=$(date +%H:%M:%S)

# 3분 대기 (1분 주기 샘플 2개 이상 확보)
sleep 180

# START 이후 레코드만 조회
sar -r -s "$START" | head -10   # 메모리 샘플
sar -q -s "$START" | head -10   # 러너블/블록 태스크 샘플
sar -S -s "$START" | head -10   # swap 샘플
                                #   -S 실패 시 /etc/sysstat/sysstat 의 SADC_OPTIONS 에 '-S SWAP' 추가 필요

# sa 파일 mtime 이 1분 이내여야 한다 (실제 수집이 일어나는지 최종 판단 근거)
ls -la --time-style=full-iso /var/log/sysstat/sa$(date +%d)

# timer 실행 이력
sudo journalctl -u sysstat-collect.service --since "-3 min" | tail -10
```

START 이후 `sar -r` 레코드가 1분 간격으로 이어지고, sa 파일 mtime 이 분 단위로 갱신되면 적용 성공이다.

### 검증 실패 시

1분 간격 레코드가 쌓이지 않으면 아래 순서로 디버그한다.

```bash
# 1) timer 활성화 상태
systemctl status sysstat-collect.timer --no-pager

# 2) timer 실행 이력 (ExecStart 호출 로그)
sudo journalctl -u sysstat-collect.service --since "-10 min"

# 3) drop-in 이 올바르게 로드되었는지 확인
#    Drop-In: /etc/systemd/system/sysstat-collect.timer.d/override.conf 가 표시되고
#    [Timer] 섹션 OnCalendar=*:*:00 이 최종 값이어야 한다
systemctl cat sysstat-collect.timer

# 4) sa 파일 mtime 실시간 모니터링 (Ctrl+C 로 종료)
watch -n 5 'ls -la --time-style=full-iso /var/log/sysstat/sa$(date +%d)'

# 5) daemon-reload 누락 여부 재확인
#    (override 파일만 만들고 reload 안 했을 때 흔한 증상)
sudo systemctl daemon-reload
sudo systemctl restart sysstat-collect.timer
```

확인 포인트:

- timer 가 `active (waiting)` 상태여야 한다
- `systemctl cat` 출력의 Drop-In 섹션에 override.conf 경로가 보여야 하고, `OnCalendar=*:*:00` 이 최종 값이어야 한다
- `OnCalendar=` 빈 줄이 누락되면 기본값(`*:00/10`) 과 새 값(`*:*:00`) 이 누적 triggered 되어 혼란을 줄 수 있으므로 override 파일 내용 재확인
- journalctl 에 분 단위로 `sysstat-collect.service` 실행 기록이 남아야 한다
- sa 파일 mtime 이 분 단위로 갱신되어야 한다 (이것이 최종 판단 근거)

## 영향 / 용량

- `sa*` 바이너리 데이터(`/var/log/sysstat/`) 저장량이 기존 대비 약 10배 증가
- 1레코드당 크기: sysstat 12.x 기준 약 680 bytes/record 수준 (커널 스탯 필드 수에 따라 변동. sysstat 11 이하는 더 작음)
  - 10분 주기: 하루 144 레코드 × 680B ≈ 약 0.1 MB/day
  - 1분 주기: 하루 1440 레코드 × 680B ≈ 약 1 MB/day
  - 7일 보존(`HISTORY=7`) 기준: 기존 약 0.7 MB → 변경 후 약 7 MB (순증 약 +6 MB)
- 적용 전후 실측은 `du -sh /var/log/sysstat` 로 수집 후 수행 이력에 기록한다
- HISTORY 설정 위치:
  - Debian/Ubuntu: `/etc/sysstat/sysstat`
  - RHEL 계열: `/etc/sysconfig/sysstat`
- `SA_DIR` 은 기본 `/var/log/sysstat`
- timer 실행 빈도가 분당 1회로 늘지만 `sa1 1 1` 은 CPU/IO 부하가 무시 가능한 수준

## 원상복구

drop-in 파일을 제거하고 timer 를 재시작하면 패키지 기본값(`OnCalendar=*:00/10`) 으로 복구된다.

```bash
sudo rm /etc/systemd/system/sysstat-collect.timer.d/override.conf
sudo rmdir /etc/systemd/system/sysstat-collect.timer.d 2>/dev/null || true
sudo systemctl daemon-reload
sudo systemctl restart sysstat-collect.timer

# 복구 확인 — OnCalendar 가 기본값으로 돌아왔는지
systemctl cat sysstat-collect.timer | grep OnCalendar
# 기대 출력: OnCalendar=*:00/10
```

## 잘못된 시도 기록 (중요 교훈)

`/etc/cron.d/sysstat` 의 cron 표현식(`5-55/10 * * * *` → `* * * * *`) 변경은 **Ubuntu 24.04 sysstat 12.6.1 환경에서 효과 없음**이 실측으로 확인됐다.

- cron 은 정상 동작하여 `debian-sa1` 호출 로그가 분 단위로 남았다
- 그러나 `/var/log/sysstat/sa*` 파일 mtime 은 여전히 10분 주기로만 갱신됐다
- 실제 샘플링/저장 주체는 `sysstat-collect.timer` → `sysstat-collect.service` → `/usr/lib/sysstat/sa1 1 1` 경로다
- cron 라인의 `debian-sa1` 호출은 무해하게 존재하지만 sa 파일에 기록을 남기지 못했다 (패키지 구성상 timer 경로와 lock/실행 조건 차이로 추정)

교훈: sysstat 샘플 주기 변경은 **배포판/버전에 따라 수집 경로가 다르다.** 반드시 `systemctl cat sysstat-collect.timer` 로 timer 유무를 먼저 확인하고, timer 가 있으면 drop-in override 방식을 사용한다.

## 후속 작업

- 현재 Jenkins 인스턴스 OS 는 IaC 로 관리되지 않는다. IaC 도입 전까지 본 변경은 인스턴스 교체/재배포 시 **수동 재적용이 필요하다.**
- IaC 도입은 본 Runbook 범위 외. 별도 이슈로 트래킹한다.

## 수행 이력

| 날짜 | 환경 | 담당 | 비고 |
| --- | --- | --- | --- |
| 2026-10-31 | Jenkins (Lightsail 2GB, Ubuntu 24.04) | @sunghyobin | #4554 적용. systemd timer drop-in `sysstat-collect.timer.d/override.conf` 생성, `OnCalendar=*:*:00` 로 1분 주기 전환. `cron.d/sysstat` 은 원본 유지. |
