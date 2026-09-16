package com.personal.marketnote.product.adapter.out.persistence.fulfillment;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.fulfillment.entity.FulfillmentGoodsElementReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.fulfillment.entity.FulfillmentGoodsReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.fulfillment.repository.FulfillmentGoodsReadModelJpaRepository;
import com.personal.marketnote.product.port.in.result.fulfillment.*;
import com.personal.marketnote.product.port.out.fulfillment.GetFulfillmentVendorGoodsElementsPort;
import com.personal.marketnote.product.port.out.fulfillment.GetFulfillmentVendorGoodsPort;
import com.personal.marketnote.product.port.out.fulfillment.SaveFulfillmentGoodsReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class FulfillmentGoodsReadModelPersistenceAdapter implements
        SaveFulfillmentGoodsReadModelPort,
        GetFulfillmentVendorGoodsPort,
        GetFulfillmentVendorGoodsElementsPort {

    private final FulfillmentGoodsReadModelJpaRepository repository;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(FulfillmentGoodsSyncedEvent event) {
        Optional<FulfillmentGoodsReadModelJpaEntity> existing =
                repository.findByCustomerGoodsCode(event.customerGoodsCode());

        if (existing.isPresent()) {
            updateEntity(existing.get(), event);
            return;
        }

        try {
            FulfillmentGoodsReadModelJpaEntity entity = FulfillmentGoodsReadModelJpaEntity.of(event.customerGoodsCode());
            updateEntity(entity, event);
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("풀필먼트 상품 Read Model 중복 저장 (멱등 처리). customerGoodsCode={}", event.customerGoodsCode());
            repository.findByCustomerGoodsCode(event.customerGoodsCode())
                    .ifPresent(entity -> updateEntity(entity, event));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetFulfillmentVendorGoodsResult getFulfillmentVendorGoods(String godNm) {
        Optional<FulfillmentGoodsReadModelJpaEntity> entity =
                repository.findByCustomerGoodsCode(godNm);

        if (entity.isEmpty()) {
            return GetFulfillmentVendorGoodsResult.of(0, List.of());
        }

        FulfillmentGoodsReadModelJpaEntity goods = entity.get();
        if (!goods.getStatus().isActive()) {
            return GetFulfillmentVendorGoodsResult.of(0, List.of());
        }

        FulfillmentVendorGoodsInfoResult info = mapToGoodsInfoResult(goods);
        return GetFulfillmentVendorGoodsResult.of(1, List.of(info));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetFulfillmentVendorGoodsElementsResult getFulfillmentVendorGoodsElements() {
        List<FulfillmentGoodsReadModelJpaEntity> allGoods =
                repository.findAll().stream()
                        .filter(entity -> entity.getStatus().isActive())
                        .toList();

        List<FulfillmentVendorGoodsElementInfoResult> elements = allGoods.stream()
                .map(this::mapToElementInfoResult)
                .toList();

        return GetFulfillmentVendorGoodsElementsResult.of(elements.size(), elements);
    }

    private void updateEntity(FulfillmentGoodsReadModelJpaEntity entity, FulfillmentGoodsSyncedEvent event) {
        entity.updateFrom(
                event.goodsCode(), event.goodsName(), event.goodsType(), event.goodsTypeName(),
                event.invoiceGoodsNameEnabled(), event.goodsOptionCode1(), event.goodsOptionCode2(),
                event.customerCode(), event.customerName(), event.supplierCode(), event.supplierName(),
                event.categoryCode(), event.categoryName(), event.seasonCode(), event.genderCode(),
                event.unitPrice(), event.supplyPrice(), event.salePrice(), event.handlingTemperature(),
                event.pickingFacility(), event.giftDivision(), event.giftDivisionName(),
                event.goodsWidth(), event.goodsLength(), event.goodsHeight(), event.manufactureYear(),
                event.goodsBulk(), event.goodsWeight(), event.goodsSideSum(), event.goodsVolume(),
                event.goodsBarcode(), event.boxWidth(), event.boxLength(), event.boxHeight(),
                event.boxBulk(), event.boxWeight(), event.innerBoxBarcode(), event.innerBoxLength(),
                event.innerBoxHeight(), event.innerBoxBulk(), event.innerBoxWidth(), event.innerBoxWeight(),
                event.innerBoxSideSum(), event.boxInnerCount(), event.innerBoxInnerCount(),
                event.palletInnerCount(), event.origin(), event.expirationDateManagementEnabled(),
                event.shelfLifeDays(), event.outboundAvailableDays(), event.inboundAvailableDays(),
                event.outboundBoxType(), event.cushioningEnabled(), event.loadingDirection(),
                event.firstInboundDate(), event.enabled(), event.feeApplied(), event.saleUnitQuantity(),
                event.oneDayDeliveryEnabled(), event.safetyStock()
        );

        if (FormatValidator.hasValue(event.elements())) {
            List<FulfillmentGoodsElementReadModelJpaEntity> elementEntities = event.elements().stream()
                    .map(elem -> FulfillmentGoodsElementReadModelJpaEntity.of(
                            elem.goodsCode(), elem.customerGoodsCode(), elem.goodsBarcode(),
                            elem.goodsName(), elem.goodsType(), elem.goodsTypeName(), elem.quantity()
                    ))
                    .toList();
            entity.replaceElements(elementEntities);
        }
    }

    private FulfillmentVendorGoodsInfoResult mapToGoodsInfoResult(FulfillmentGoodsReadModelJpaEntity entity) {
        return FulfillmentVendorGoodsInfoResult.builder()
                .goodsCode(entity.getGoodsCode())
                .goodsType(entity.getGoodsType())
                .goodsName(entity.getGoodsName())
                .goodsTypeName(entity.getGoodsTypeName())
                .invoiceGoodsNameEnabled(entity.getInvoiceGoodsNameEnabled())
                .customerGoodsCode(entity.getCustomerGoodsCode())
                .goodsOptionCode1(entity.getGoodsOptionCode1())
                .goodsOptionCode2(entity.getGoodsOptionCode2())
                .customerCode(entity.getCustomerCode())
                .customerName(entity.getCustomerName())
                .supplierCode(entity.getSupplierCode())
                .supplierName(entity.getSupplierName())
                .categoryCode(entity.getCategoryCode())
                .categoryName(entity.getCategoryName())
                .seasonCode(entity.getSeasonCode())
                .genderCode(entity.getGenderCode())
                .unitPrice(entity.getUnitPrice())
                .supplyPrice(entity.getSupplyPrice())
                .salePrice(entity.getSalePrice())
                .handlingTemperature(entity.getHandlingTemperature())
                .pickingFacility(entity.getPickingFacility())
                .giftDivision(entity.getGiftDivision())
                .giftDivisionName(entity.getGiftDivisionName())
                .goodsWidth(entity.getGoodsWidth())
                .goodsLength(entity.getGoodsLength())
                .goodsHeight(entity.getGoodsHeight())
                .manufactureYear(entity.getManufactureYear())
                .goodsBulk(entity.getGoodsBulk())
                .goodsWeight(entity.getGoodsWeight())
                .goodsSideSum(entity.getGoodsSideSum())
                .goodsVolume(entity.getGoodsVolume())
                .goodsBarcode(entity.getGoodsBarcode())
                .boxWidth(entity.getBoxWidth())
                .boxLength(entity.getBoxLength())
                .boxHeight(entity.getBoxHeight())
                .boxBulk(entity.getBoxBulk())
                .boxWeight(entity.getBoxWeight())
                .innerBoxBarcode(entity.getInnerBoxBarcode())
                .innerBoxLength(entity.getInnerBoxLength())
                .innerBoxHeight(entity.getInnerBoxHeight())
                .innerBoxBulk(entity.getInnerBoxBulk())
                .innerBoxWidth(entity.getInnerBoxWidth())
                .innerBoxWeight(entity.getInnerBoxWeight())
                .innerBoxSideSum(entity.getInnerBoxSideSum())
                .boxInnerCount(entity.getBoxInnerCount())
                .innerBoxInnerCount(entity.getInnerBoxInnerCount())
                .palletInnerCount(entity.getPalletInnerCount())
                .origin(entity.getOrigin())
                .expirationDateManagementEnabled(entity.getExpirationDateManagementEnabled())
                .shelfLifeDays(entity.getShelfLifeDays())
                .outboundAvailableDays(entity.getOutboundAvailableDays())
                .inboundAvailableDays(entity.getInboundAvailableDays())
                .outboundBoxType(entity.getOutboundBoxType())
                .cushioningEnabled(entity.getCushioningEnabled())
                .loadingDirection(entity.getLoadingDirection())
                .firstInboundDate(entity.getFirstInboundDate())
                .enabled(entity.getEnabled())
                .feeApplied(entity.getFeeApplied())
                .saleUnitQuantity(entity.getSaleUnitQuantity())
                .oneDayDeliveryEnabled(entity.getOneDayDeliveryEnabled())
                .safetyStock(entity.getSafetyStock())
                .build();
    }

    private FulfillmentVendorGoodsElementInfoResult mapToElementInfoResult(FulfillmentGoodsReadModelJpaEntity entity) {
        List<FulfillmentVendorGoodsElementItemResult> elementItems = entity.getElements().stream()
                .map(elem -> FulfillmentVendorGoodsElementItemResult.builder()
                        .goodsCode(elem.getGoodsCode())
                        .customerGoodsCode(elem.getCustomerGoodsCode())
                        .goodsBarcode(elem.getGoodsBarcode())
                        .goodsName(elem.getGoodsName())
                        .goodsType(elem.getGoodsType())
                        .goodsTypeName(elem.getGoodsTypeName())
                        .quantity(elem.getQuantity())
                        .build())
                .toList();

        return FulfillmentVendorGoodsElementInfoResult.builder()
                .goodsCode(entity.getGoodsCode())
                .customerGoodsCode(entity.getCustomerGoodsCode())
                .goodsName(entity.getGoodsName())
                .enabled(entity.getEnabled())
                .elementList(elementItems)
                .build();
    }
}
