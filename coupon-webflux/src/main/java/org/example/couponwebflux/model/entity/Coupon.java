package org.example.couponwebflux.model.entity;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.couponwebflux.exception.CouponIssueException;
import org.example.couponwebflux.exception.ErrorCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coupons")
public class Coupon extends BaseEntity {

    @Id
    private Long id;

    @Column
    private String title;

    @Column
    private String couponType;

    private Integer totalQuantity;

    @Column
    private int issuedQuantity;

    @Column
    private int discountAmount;

    @Column
    private int minAvailableAmount;

    @Column
    private LocalDateTime issueStartDateTime;

    @Column
    private LocalDateTime issueEndDateTime;


    public void issue() {
        validateIssue();
        issuedQuantity++;
    }

    public void validateIssue() {

        if (!availableIssueQuantity()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "수량 검증 실패");
        }

        if (!availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATETIME, "유효 기간 검증 실패");
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
