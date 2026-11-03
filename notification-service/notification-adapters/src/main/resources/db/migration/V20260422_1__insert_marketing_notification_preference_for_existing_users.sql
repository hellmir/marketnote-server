-- [#2090][#4606] 기존 회원 대상 MARKETING 알림 수신 설정 백필 (옵트인 원칙: enabled=false)
-- 정보통신망법 제50조에 따라 광고성 정보 수신은 명시적 사전 동의가 필요하므로 초기값은 미동의(false)로 적재한다.
-- 멱등성: 이미 MARKETING 레코드가 존재하는 사용자는 제외 (WHERE NOT EXISTS), 유니크 제약(uk_notification_preference_user_type)과도 정합.

INSERT INTO notification_preference (user_id, notification_type, enabled, status, created_at)
SELECT DISTINCT p.user_id,
                'MARKETING',
                FALSE,
                'ACTIVE',
                NOW()
FROM notification_preference p
WHERE NOT EXISTS (SELECT 1
                  FROM notification_preference m
                  WHERE m.user_id = p.user_id
                    AND m.notification_type = 'MARKETING');
