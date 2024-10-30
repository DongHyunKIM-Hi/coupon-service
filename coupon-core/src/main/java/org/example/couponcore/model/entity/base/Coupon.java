package org.example.couponcore.model.entity.base;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.enums.CouponType;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "coupons")
public class Coupon extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime issueStartDateTime;

    @Column(nullable = false)
    private LocalDateTime issueEndDateTime;


    public void issue() {
       validateIssue();
       issuedQuantity ++;
    }

    public void validateIssue() {

        if(!availableIssueQuantity()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY,"수량 검증 실패");
        }

        if(!availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATETIME,"유효 기간 검증 실패");
        }

    }

    public boolean availableIssueQuantity() {
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return issueStartDateTime.isBefore(now) && issueEndDateTime.isAfter(now);
    }

    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        return issueEndDateTime.isBefore(now) || !availableIssueQuantity();
    }
}
