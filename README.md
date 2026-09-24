# ![marketnote](https://github.com/user-attachments/assets/409f0f06-e841-428a-b39c-eb0cdb4520e3) 마켓플레이스 서비스 마켓노트 Server <img src="https://img.shields.io/badge/v1.0-6DB33F?style=flat-square&logo=Adobe&logoColor=white"><br>

## 📋 프로젝트 설명

- 마켓플레이스 서비스 API를 제공하는 서버
  <br><br>

## 📅 프로젝트 기간
<b>2025. 12. 24 ~ 2026. 06. 20</b>
<br>
<b>2026. 09. 01 ~ </b>
<br><br>

## 👫 구성원

### 성효빈
- 서버 개발, 배포 및 관리
  <br>

## 📚 관련 URL
- [CI/CD 파이프라인](https://hyobin-jenkins.duckdns.org/job/marketnote)
- [회원 서비스](https://users.marketnote.store/swagger-ui/index.html)
- [상품 서비스](https://products.marketnote.store/swagger-ui/index.html)
- [커머스 서비스](https://commerce.marketnote.store/swagger-ui/index.html)
- [풀필먼트 서비스](https://fulfillment.marketnote.store/swagger-ui/index.html)
- [커뮤니티 서비스](https://community.marketnote.store/swagger-ui/index.html)
- [리워드 서비스](https://rewards.marketnote.store/swagger-ui/index.html)
- [알림 서비스](https://notifications.marketnote.store/swagger-ui/index.html)
- [파일 서비스](https://files.marketnote.store/swagger-ui/index.html)
  <br><br>

## 🛠️ Skills

## Language
- Java 21

## Frameworks
- Spring Boot 3.5.6
- Spring Security
- JPA(Hibernate)
  <br>

## Structure
- MSA
- Hexagonal Architecture
- Multi-Module(Adapters/application/domain)

## Event Streaming
- Apache Kafka Cluster

## RDBMS
- PostgreSQL

## NoSQL
- Redis(Cache/Session/Redisson Distribution Lock)

## Infrastructure

### CI/CD
- Jenkins

### Container Registry
- AWS ECR
  <br>

### Container Orchestration
- AWS ECS Fargate
  <br>

### Network Routing
- AWS Route 53
- AWS NAT Gateway
  <br>

### Load Balancing
- AWS ELB
  <br>

### Object Storage
- AWS S3
  <br>

### Monitoring
- Prometheus
- Grafana
  <br>

### Performance Testing
- K6
  <br>

## Vendors

### OAuth2
- Google(Web/Mobile SDK)
- Kakao(Web/Mobile SDK)
- Apple(Mobile SDK)

### Email Verification
- AWS SES

### PSP
- NHN KCP

### Fulfillment
- Fassto

### Offerwall
- Adpopcorn
- TNK
- Adiscope

### Coupon
- Giftishow

### Notifications

#### Push Notification
- Firebase Cloud Messaging

#### AlimTalk
- Aligo

#### System Notification
- Slack(Jenkins/Grafana/Kafka)

## Tools

### IDE
- Intellij IDEA
  <br>

### Test Automation
- JUnit
- Mockito
- AssertJ

### Configuration Management
- Flyway
- Git

### Issue Tracking
- [Github Issues](https://github.com/hellmir/marketnote-server/issues)
  <br>

## Kafka 토픽

```
# User
  user.user.signup-completed
  user.user.guest-signup-completed
  user.user.referral-completed
  user.user.withdrawn
  user.user.status-changed
  user.user.marketing-consent-changed
  user.shipping-address.changed

# Product
  product.product.registered
  product.product.updated
  product.product.deleted
  product.product.tag-changed
  product.product.status-changed
  product.price-policy.created
  product.price-policy.deleted
  product.shipping-policy.changed
  product.seller.changed

# Commerce
  commerce.order.payment-completed
  commerce.order.cancelled
  commerce.order.cancel-failed
  commerce.order.auto-confirm-failed
  commerce.order.purchase-confirmed
  commerce.order.returned
  commerce.order.return-requested
  commerce.order.repurchase-nudged
  commerce.return-inspection.completed
  commerce.payment.approved
  commerce.payment.failed
  commerce.payment.cancelled
  commerce.settlement.executed
  commerce.inventory.changed

# Fulfillment
  fulfillment.shipping.status-changed
  fulfillment.inventory.synced
  fulfillment.goods.synced
  fulfillment.delivery.work-status-changed

# Community
  community.review.registered
  community.review.updated
  community.review.deleted
  community.notice.registered
  community.event.registered
  community.inquiry.answered

# Reward
  reward.booster.applied
  reward.booster.revoke-requested
  reward.payback-policy.changed
  reward.attendance.reminder-consecutive
  reward.attendance.reminder-non-consecutive
  reward.coupon.expiry-reminder-24h
  reward.coupon.expiry-reminder-3h
  reward.coupon.welcome-expiry-reminder-24h
  reward.coupon.welcome-expiry-reminder-3h
  reward.purchase.point-confirmed
  reward.shared-purchase.point-confirmed
  reward.shared-purchase.point-pending

# File
  file.image.changed

# SAGA (인프라)
  saga.response
  saga.order-payment.inventory
  saga.order-payment.ledger
  saga.order-payment.completed
  saga.order-cancel.fulfillment
  saga.order-cancel.refund
  saga.order-cancel.point-refund
  saga.order-cancel.coupon-restore
  saga.order-cancel.completed
  saga.order-return.refund
  saga.order-return.point-refund
  saga.order-return.coupon-restore
  saga.order-return.completed
  saga.order-payment-downstream.fulfillment
  saga.order-payment-downstream.order-point
  saga.order-payment-downstream.coupon
  saga.order-payment-downstream.shared-point
  saga.order-payment-downstream.product-point
  saga.order-payment-downstream.cart

# 각 토픽별 DLT
  {토픽명}.dlt
```

## 인프라 구성

**로그:** CloudWatch (awslogs)

**이미지:** AWS ECR (서비스별 레포지토리)

**서버 사양 (AWS ECS Fargate):**

| Service              | Node Machine    | Tier   | CPU / Memory | desired | Auto Scaling (min~max, CPU 70% 타깃)| Capacity Provider              |
|----------------------|-----------------|--------|--------------|---------|------------------------------------|--------------------------------|
| commerce-service     | AWS ECS Fargate | Heavy  | 1024 / 2048  | 2       | 2 ~ 4                              | FARGATE(base=1) + FARGATE_SPOT |
| notification-service | AWS ECS Fargate | Heavy  | 1024 / 2048  | 3       | 3 ~ 5                              | FARGATE(base=1) + FARGATE_SPOT |
| product-service      | AWS ECS Fargate | Heavy  | 1024 / 2048  | 2       | 2 ~ 5                              | FARGATE(base=1) + FARGATE_SPOT |
| reward-service       | AWS ECS Fargate | Medium | 512 / 2048   | 2       | 2 ~ 3                              | FARGATE(base=1) + FARGATE_SPOT |
| fulfillment-service  | AWS ECS Fargate | Medium | 512 / 2048   | 2       | 2 ~ 3                              | FARGATE(base=1) + FARGATE_SPOT |
| user-service         | AWS ECS Fargate | Medium | 512 / 2048   | 2       | 2 ~ 4                              | FARGATE(base=1) + FARGATE_SPOT |
| community-service    | AWS ECS Fargate | Light  | 512 / 1024   | 2       | 2 ~ 3                              | FARGATE only                   |
| file-service         | AWS ECS Fargate | Light  | 512 / 1024   | 1       | 미적용 (단일 인스턴스 운영)              | FARGATE only                   |

**Kafka Cluster:**

| Broker  | Host                         | Node Machine  | CPU / Memory | Port |
|---------|------------------------------|---------------|--------------|------|
| kafka-1 | marketnote-qa_kafka-broker-1 | AWS Lightsail | 2048 / 4096  | 9092 |
| kafka-2 | marketnote-qa_kafka-broker-2 | AWS Lightsail | 2048 / 4096  | 9092 |
| kafka-3 | marketnote-qa_kafka-broker-3 | AWS Lightsail | 2048 / 4096  | 9092 |