# Fassto 벤더 통신 이력 검색 가이드

## 배경

`#5040` 머지 후 Fassto Goods/Stock/Warehousing/Delivery/ReturnDelivery 매퍼는
`productKey`(UUID 36자)를 `cstGodCd` 필드로 송신할 때 Base62 22자로 인코딩한다.
(Fassto 벤더 측 `cstGodCd` 25자 한도 회피 목적, `FasstoCustomerGoodsCodeEncoder` 참조.)

이로 인해 `fulfillment_vendor_communication_history.payload`/`payload_json`
컬럼에 저장된 `cstGodCd` 값은 **22자 Base62 문자열**(예: `1A2B3c4D5e6F7g8H9i0J1K`)이다.
운영자가 productKey UUID 36자 (예: `123e4567-e89b-12d3-a456-426614174000`)
원문으로 grep/SQL 검색하면 매칭되지 않는다.

## productKey 검색 가이드

### 1. `target_id` 컬럼 우선 사용 (권장)

`fulfillment_vendor_communication_history.target_id` 컬럼은
productKey UUID 36자 원문을 보존한다.

- `Goods` 클라이언트(상품 등록/수정/조회): `target_id` = productKey UUID
- `Stock` 클라이언트(상품 재고 상세 조회):
  - `forProductKey` 경유 호출 시 (`SyncFulfillmentStockService` 등): `target_id` = productKey UUID 36자
    (`cstGodCd` Base62 22자를 `decodeCustomerProductCode`로 디코딩하여 저장)
  - `of` 경유 호출(`customerProductCode` 사용자 지정): `target_id` = 해당 코드 원문
    (22자 Base62 형식이면 디코딩된 UUID, 그 외에는 입력 그대로)
- `Warehousing`/`Delivery`/`ReturnDelivery` 클라이언트의 주문/입고/반품 단위 호출은
  productKey 식별 불가 (전체 조회/주문 단위 호출). `target_id`에 productKey 없음.

```sql
-- 운영자: productKey UUID로 이력 조회
SELECT *
FROM fulfillment_vendor_communication_history
WHERE target_id = '123e4567-e89b-12d3-a456-426614174000'
ORDER BY created_at DESC;
```

### 2. payload 안의 `cstGodCd`(Base62)로 검색해야 하는 경우

`target_id`가 비어 있거나 payload 내부 검색이 필요한 경우, productKey UUID를
Base62 22자로 **인코딩**한 후 검색한다.

#### 인코딩/디코딩 변환 (Java)

```java
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder.FasstoCustomerGoodsCodeEncoder;
import java.util.UUID;

// productKey UUID → cstGodCd Base62 22자
UUID productKey = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
String cstGodCd = FasstoCustomerGoodsCodeEncoder.encode(productKey);
// cstGodCd: 예) "1A2B3c4D5e6F7g8H9i0J1K"

// cstGodCd Base62 22자 → productKey UUID
// `decode(...)`는 22자 Base62 형식이 아니면 FasstoCustomerGoodsCodeLength/CharsetDecodingFailedException을 throw한다.
// 운영자가 임의 입력에 대해 안전하게 변환하려면 `decodeCustomerProductCode(...)`(형식 불일치 시 입력 그대로 반환)을 사용한다.
String cstGodCdFromHistory = "1A2B3c4D5e6F7g8H9i0J1K";
String decodedProductKey = FasstoCustomerGoodsCodeEncoder.decodeCustomerProductCode(cstGodCdFromHistory);
```

#### DataGrip / SQL 예시

```sql
-- 인코딩된 cstGodCd 값(Base62 22자)을 알고 있을 때
SELECT *
FROM fulfillment_vendor_communication_history
WHERE payload LIKE '%"cstGodCd":"1A2B3c4D5e6F7g8H9i0J1K"%'
ORDER BY created_at DESC;

-- payload_json (JSONB) 사용 시
SELECT *
FROM fulfillment_vendor_communication_history
WHERE payload_json @> '{"body": [{"cstGodCd": "1A2B3c4D5e6F7g8H9i0J1K"}]}'::jsonb;
```

## 문제 해결

| 증상 | 원인 | 해결 |
|------|------|------|
| productKey UUID로 검색해도 0건 | `cstGodCd` payload 안에는 Base62 인코딩값만 존재 | `target_id` 컬럼으로 검색하거나 productKey를 인코딩하여 payload 검색 |
| `target_id`도 비어 있음 | 전체 조회/주문 단위 API 호출(productKey 식별 불가) | 다른 식별자(예: `ordNo`)로 payload 검색하거나 시각·벤더명으로 필터 |

## 관련

- 인코더 구현: `FasstoCustomerGoodsCodeEncoder` (Base62 22자, 영숫자만)
- 이력 기록기: `FulfillmentVendorCommunicationRecorder`
- 관련 이슈: `#475`, `#4077`, `#5040`, `#5046`
