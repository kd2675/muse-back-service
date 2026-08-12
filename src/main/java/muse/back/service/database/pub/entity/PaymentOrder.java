package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse.back.service.common.jpa.CommonDateEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOrder extends CommonDateEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_order_id")
    private Long paymentOrderId;
    @Column(name = "order_id", length = 64, nullable = false)
    private String orderId;
    @Column(name = "artist_id", nullable = false)
    private Long artistId;
    @Column(name = "contest_id", nullable = false)
    private Long contestId;
    @Column(name = "provider", length = 20, nullable = false)
    private String provider;
    @Column(name = "order_name", length = 120, nullable = false)
    private String orderName;
    @Column(name = "amount", nullable = false)
    private int amount;
    @Column(name = "status", length = 30, nullable = false)
    private String status;
    @Column(name = "payment_key", length = 200)
    private String paymentKey;
    @Column(name = "payment_secret", length = 200)
    private String paymentSecret;
    @Column(name = "idempotency_key", length = 64, nullable = false)
    private String idempotencyKey;
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;
    @Column(name = "failure_code", length = 80)
    private String failureCode;
    @Column(name = "failure_message", length = 500)
    private String failureMessage;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    @Version @Column(name = "version", nullable = false)
    private Long version;

    public PaymentOrder(String orderId, Long artistId, Long contestId, String orderName, int amount, String idempotencyKey) {
        this.orderId = orderId;
        this.artistId = artistId;
        this.contestId = contestId;
        this.provider = "TOSS";
        this.orderName = orderName;
        this.amount = amount;
        this.status = "READY";
        this.idempotencyKey = idempotencyKey;
    }

    public void markPaid(String paymentKey, String paymentSecret, String receiptUrl, LocalDateTime paidAt) {
        this.status = "DONE";
        this.paymentKey = paymentKey;
        this.paymentSecret = paymentSecret;
        this.receiptUrl = receiptUrl;
        this.paidAt = paidAt;
        this.failureCode = null;
        this.failureMessage = null;
    }

    public void markFailed(String code, String message) {
        this.status = "FAILED";
        this.failureCode = code;
        this.failureMessage = message;
    }

    public void markCanceled(LocalDateTime value) {
        this.status = "CANCELED";
        this.canceledAt = value;
    }
}
