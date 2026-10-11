-- [#2090][#2109][#3789] 알림 템플릿 20종 시드 (Consumer 실제 사용 template_code 기준)
-- 운영 DB 수동 등록 누락으로 인한 silent drop 재발 방지 목적.
-- 멱등성: INSERT ... SELECT ... WHERE NOT EXISTS 패턴으로 이미 등록된 row(관리자 API 등록 또는 재실행)는 보존한다.
--   (H2/PostgreSQL 양쪽 호환을 위해 ON CONFLICT 대신 WHERE NOT EXISTS 사용)
-- NotificationType(18) vs template_code(20) 관계는 1:N: PURCHASE_CONFIRMATION_COMPLETED는 AUTO/MANUAL/(Push)로 3분할.
-- NOTICE_REGISTERED/EVENT_REGISTERED는 Consumer 구현(NoticeRegisteredNotificationConsumer/EventRegisteredNotificationConsumer)의 하드코딩 template_code 기준.
-- body_template/url_template의 placeholder는 Consumer가 실제로 제공하는 variables 키만 사용한다 (미제공 변수 참조 금지).
-- 기획 미확정 항목은 합리적 기본값으로 시드하며, 기획 확정 후 관리자 API로 수정한다.

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'ORDER_PAYMENT_COMPLETED', 'ORDER_PAYMENT_COMPLETED', 'MANDATORY', '[결제 완료]', '주문 결제가 정상 완료되었습니다.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'ORDER_PAYMENT_COMPLETED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'SHIPPING_STARTED', 'SHIPPING_STARTED', 'MANDATORY', '[배송 시작]', '요청하신 상품이 배송 시작되었습니다.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'SHIPPING_STARTED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'SHIPPING_COMPLETED', 'SHIPPING_COMPLETED', 'MANDATORY', '[배송 완료]', '배송이 완료되었습니다.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'SHIPPING_COMPLETED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'SHIPPING_DELAYED', 'SHIPPING_DELAYED', 'MANDATORY', '[배송 지연]', '주문 폭주로 인해 배송이 지연되고 있습니다.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'SHIPPING_DELAYED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'ORDER_CANCELLED', 'ORDER_CANCELLED', 'MANDATORY', '[주문 취소 완료]', '취소 및 환불이 정상 처리되었습니다.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'ORDER_CANCELLED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'ORDER_CANCEL_FAILED', 'ORDER_CANCEL_FAILED', 'MANDATORY', '[주문 취소 불가]', '배송이 시작되어 주문 취소가 불가능합니다. 상품 수령 후 반품 신청을 이용해 주세요.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'ORDER_CANCEL_FAILED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'RETURN_REQUESTED', 'RETURN_REQUESTED', 'MANDATORY', '[반품 신청]', '반품 신청이 접수되었습니다. 수거 기사님이 방문할 예정입니다.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'RETURN_REQUESTED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'RETURN_COMPLETED', 'RETURN_COMPLETED', 'MANDATORY', '[반품 완료]', '반품 및 환불이 정상 처리되었습니다.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'RETURN_COMPLETED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'RETURN_REJECTED', 'RETURN_REJECTED', 'MANDATORY', '[반품 불가]', '반품 신청이 반려되었습니다. 자세한 사유는 고객센터로 문의해 주세요.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'RETURN_REJECTED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'ONE_ON_ONE_INQUIRY_REPLY', 'ONE_ON_ONE_INQUIRY_REPLY', 'MANDATORY', '[1:1 문의]', '고객님께서 문의하신 ''{inquiry_title}'' 건에 답변이 등록되었습니다.', '/inquiry/{post_id}?board=ONE_ON_ONE_INQUERY'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'ONE_ON_ONE_INQUIRY_REPLY');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'PRODUCT_INQUIRY_REPLY', 'PRODUCT_INQUIRY_REPLY', 'MANDATORY', '[상품 문의]', '{inquiry_title} 상품에 남기신 문의에 판매자가 답변을 남겼습니다.', '/inquiry/{post_id}?board=PRODUCT_INQUERY'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'PRODUCT_INQUIRY_REPLY');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'NOTICE_REGISTERED', 'NOTICE', 'MANDATORY', '[공지사항]', '{notice_title}', '/notice/{post_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'NOTICE_REGISTERED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'CONSENT_REMINDER', 'CONSENT_REMINDER', 'MANDATORY', '[수신 동의 재고지]', '마케팅 정보 수신 동의 유효기간 갱신 안내입니다.', '/mypage/notification/settings'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'CONSENT_REMINDER');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'PURCHASE_CONFIRMATION_REQUEST', 'PURCHASE_CONFIRMATION_REQUEST', 'INFORMATIONAL', '[구매 확정 요청]', '배송 받으신 상품이 마음에 드셨나요? 구매 확정하고 캐시, 부스터를 받으세요.', '/order/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'PURCHASE_CONFIRMATION_REQUEST');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'PURCHASE_CONFIRMATION_COMPLETED', 'PURCHASE_CONFIRMATION_COMPLETED', 'INFORMATIONAL', '[구매 확정 완료]', '구매 확정이 완료되어 캐시와 부스터가 적립되었습니다.', '/mypage/cash'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'PURCHASE_CONFIRMATION_COMPLETED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'PURCHASE_CONFIRMATION_COMPLETED_AUTO', 'PURCHASE_CONFIRMATION_COMPLETED', 'INFORMATIONAL', '[구매 확정 완료]', '구매 확정 기간 종료로 자동 구매 확정되어 캐시와 부스터가 적립되었습니다.', '/mypage/cash'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'PURCHASE_CONFIRMATION_COMPLETED_AUTO');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'PURCHASE_CONFIRMATION_COMPLETED_MANUAL', 'PURCHASE_CONFIRMATION_COMPLETED', 'INFORMATIONAL', '[구매 확정 완료]', '구매 확정이 완료되어 캐시와 부스터가 적립되었습니다.', '/mypage/cash'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'PURCHASE_CONFIRMATION_COMPLETED_MANUAL');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'REVIEW_REPLY', 'REVIEW_REPLY', 'INFORMATIONAL', '[리뷰 답글]', '{product_name} 상품의 리뷰에 판매자가 답글을 남겼습니다.', '/mypage/review'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'REVIEW_REPLY');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'POINT_ACCRUAL', 'POINT_ACCRUAL', 'INFORMATIONAL', '[포인트 적립]', '{amount} 포인트가 적립되었습니다.', '/mypage/point'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'POINT_ACCRUAL');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'EVENT_REGISTERED', 'EVENT', 'PROMOTIONAL', '[이벤트]', '{event_title}', '/notice/{post_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'EVENT_REGISTERED');

INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
SELECT 'ACTIVE', NOW(), 'REVIEW_WRITE_REQUEST', 'REVIEW_WRITE_REQUEST', 'PROMOTIONAL', '[리뷰 작성]', '최근에 구매하신 상품에 대한 리뷰를 작성해 보세요.', '/review/create/{order_id}'
WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'REVIEW_WRITE_REQUEST');
