package com.personal.marketnote.commerce.adapter.out.persistence.payment.entity;

import com.personal.marketnote.commerce.domain.payment.PaymentEventStatus;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "psp_payment_event")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PspPaymentEventJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_key", nullable = false, length = 63)
    private String orderKey;

    @Column(name = "pg_company_key", nullable = false)
    private String pgCompanyKey;

    @Column(name = "pg_shop_key")
    private String pgShopKey;

    @Column(name = "pg_payment_key")
    private String pgPaymentKey;

    @Column(name = "pg_approval_result", length = 1023)
    private String pgApprovalResult;

    @Column(name = "pg_cancel_approval_result", length = 1023)
    private String pgCancelApprovalResult;

    @Column(name = "shop_transaction_id")
    private String shopTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "po_status", nullable = false, length = 15)
    private PaymentEventStatus poStatus;

    @Column(name = "method", nullable = false, length = 15)
    private String method;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "vat_amount")
    private Long vatAmount;

    @Column(name = "nat_amount")
    private Long natAmount;

    @Column(name = "card_number", length = 511)
    private String cardNumber;

    @Column(name = "approval_number", length = 511)
    private String approvalNumber;

    @Column(name = "installment")
    private Short installment;

    @Column(name = "issue_company_code")
    private String issueCompanyCode;

    @Column(name = "issue_company_name")
    private String issueCompanyName;

    @Column(name = "purchase_company_code")
    private String purchaseCompanyCode;

    @Column(name = "purchase_company_name")
    private String purchaseCompanyName;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bill_number")
    private String billNumber;

    @Column(name = "bill_sequence_number")
    private String billSequenceNumber;

    @Column(name = "user_ip", length = 31)
    private String userIp;

    @Column(name = "payment_info", length = 2047)
    private String paymentInfo;

    @Column(name = "payment_source", length = 127)
    private String paymentSource;

    @Column(name = "result_code")
    private String resultCode;

    @Column(name = "result_message", length = 511)
    private String resultMessage;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long version;

    public static PspPaymentEventJpaEntity from(PspPaymentEvent event) {
        return PspPaymentEventJpaEntity.builder()
                .orderId(event.getOrderId())
                .orderKey(event.getOrderKey())
                .pgCompanyKey(event.getPgCompanyKey())
                .pgShopKey(event.getPgShopKey())
                .pgPaymentKey(event.getPgPaymentKey())
                .pgApprovalResult(event.getPgApprovalResult())
                .pgCancelApprovalResult(event.getPgCancelApprovalResult())
                .shopTransactionId(event.getShopTransactionId())
                .poStatus(event.getPoStatus())
                .method(event.getMethod())
                .amount(event.getAmount())
                .vatAmount(event.getVatAmount())
                .natAmount(event.getNatAmount())
                .cardNumber(event.getCardNumber())
                .approvalNumber(event.getApprovalNumber())
                .installment(event.getInstallment())
                .issueCompanyCode(event.getIssueCompanyCode())
                .issueCompanyName(event.getIssueCompanyName())
                .purchaseCompanyCode(event.getPurchaseCompanyCode())
                .purchaseCompanyName(event.getPurchaseCompanyName())
                .bankCode(event.getBankCode())
                .bankName(event.getBankName())
                .billNumber(event.getBillNumber())
                .billSequenceNumber(event.getBillSequenceNumber())
                .userIp(event.getUserIp())
                .paymentInfo(event.getPaymentInfo())
                .paymentSource(event.getPaymentSource())
                .resultCode(event.getResultCode())
                .resultMessage(event.getResultMessage())
                .paidAt(event.getPaidAt())
                .build();
    }

    public void updateFrom(PspPaymentEvent event) {
        this.pgPaymentKey = event.getPgPaymentKey();
        this.pgApprovalResult = event.getPgApprovalResult();
        this.pgCancelApprovalResult = event.getPgCancelApprovalResult();
        this.poStatus = event.getPoStatus();
        this.method = FormatValidator.hasValue(event.getMethod()) ? event.getMethod() : this.method;
        this.cardNumber = event.getCardNumber();
        this.approvalNumber = event.getApprovalNumber();
        this.installment = event.getInstallment();
        this.issueCompanyCode = event.getIssueCompanyCode();
        this.issueCompanyName = event.getIssueCompanyName();
        this.purchaseCompanyCode = event.getPurchaseCompanyCode();
        this.purchaseCompanyName = event.getPurchaseCompanyName();
        this.resultCode = event.getResultCode();
        this.resultMessage = event.getResultMessage();
        this.paidAt = event.getPaidAt();
    }

    @PostLoad
    private void initVersionAfterLoad() {
        if (FormatValidator.hasNoValue(version)) {
            version = 0L;
        }
    }

    @PrePersist
    private void initVersionBeforePersist() {
        if (FormatValidator.hasNoValue(version)) {
            version = 0L;
        }
    }
}
