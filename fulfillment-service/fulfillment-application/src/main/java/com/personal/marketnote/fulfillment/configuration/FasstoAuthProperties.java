package com.personal.marketnote.fulfillment.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vendor.fassto.auth")
public class FasstoAuthProperties {
    private String baseUrl;
    private String customerCode;
    private String apiCd;
    private String apiKey;
    private String connectPath = "/api/v1/auth/connect";
    private String disconnectPath = "/api/v1/auth/disconnect";
    private String shopPath = "/api/v1/marketnote/{customerCode}";
    private String supplierPath = "/api/v1/supplier/{customerCode}";
    private String goodsPath = "/api/v1/goods/{customerCode}";
    private String goodsElementPath = "/api/v1/goods/element/{customerCode}";
    private String warehousingPath = "/api/v1/warehousing/{customerCode}";
    private String warehousingListPath = "/api/v1/warehousing/{customerCode}/{startDate}/{endDate}";
    private String warehousingDetailPath = "/api/v1/warehousing/detail/{customerCode}/{slipNo}";
    private String warehousingInspecDetailPath = "/api/v1/warehousing/inspec/{customerCode}/{slipNo}/{whCd}";
    private String warehousingAbnormalPath = "/api/v1/warehousing/abnormal/{customerCode}/{whCd}/{slipNo}";
    private String warehousingAbnormalImagePath = "/api/v1/warehousing/{slipNo}/{godCd}/{goodsSerialNo}/{fileSeq}/{imgNo}";
    private String deliveryPath = "/api/v1/delivery/parcel/{customerCode}";
    private String deliveryCarPath = "/api/v1/delivery/car/{customerCode}";
    private String deliveryIcsPath = "/api/v1/delivery/ics/{customerCode}";
    private String deliveryListPath = "/api/v1/delivery/{customerCode}/{startDate}/{endDate}/{status}/{outDiv}";
    private String deliveryStatusPath = "/api/v1/delivery/parcel/{customerCode}/{startDate}/{endDate}/{outDiv}";
    private String deliveryDetailPath = "/api/v1/delivery/detail/{customerCode}/{slipNo}";
    private String deliveryCancelPath = "/api/v1/delivery/cancel/{customerCode}";
    private String deliveryOutOrdGoodsDetailPath = "/api/v1/delivery/outOrd/goodsDetail/{customerCode}";
    private String deliveryOutOrdGoodsByOrdNoPath = "/api/v1/delivery/outOrd/ordNo/{customerCode}/{startDate}/{endDate}";
    private String deliveryGoodDetailPath = "/api/v1/delivery/goodDetail/{customerCode}/{startDate}/{endDate}";
    private String deliveryIcsCompletedPath = "/api/v1/delivery/ics/completed/{customerCode}";
    private String stockListPath = "/api/v1/stock/list/{customerCode}";
    private String returnDeliveryPath = "/api/v1/return/parcel/{customerCode}";
    private String settlementDailyCostPath = "/api/v1/settlement/{yearMonth}/{whCd}/{customerCode}";
}
