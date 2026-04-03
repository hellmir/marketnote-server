package com.personal.marketnote.community.adapter.out.web.product;

import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.community.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.community.utility.ServiceCommunicationRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

/**
 * 상품 서비스 HTTP 클라이언트
 *
 * <p>FindProductByPricePolicyPort 구현을 ProductReadModelPersistenceAdapter로 이관하여
 * findByPricePolicyIds() 메서드를 제거했습니다.
 * 향후 다른 상품 서비스 API 호출이 필요하면 이 클래스에 추가합니다.</p>
 */
@ServiceAdapter
@Slf4j
public class ProductServiceClient {
    private final RestClient restClient;
    private final String productServiceBaseUrl;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    public ProductServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${product-service.base-url:http://localhost:8081}") String productServiceBaseUrl,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder,
            ServiceCommunicationRecorder serviceCommunicationRecorder,
            ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator
    ) {
        this.restClient = restClientBuilder.build();
        this.productServiceBaseUrl = productServiceBaseUrl;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
        this.serviceCommunicationRecorder = serviceCommunicationRecorder;
        this.serviceCommunicationPayloadGenerator = serviceCommunicationPayloadGenerator;
    }
}
