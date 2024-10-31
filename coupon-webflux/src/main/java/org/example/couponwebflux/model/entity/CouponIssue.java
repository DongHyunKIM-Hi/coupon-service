package org.example.couponwebflux.model.entity;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "coupon_issues")
public class CouponIssue extends BaseEntity {

    @Id
    private Long id;

    @Column
    private Long couponId;

    @Column
    private Long userId;

    @Column
    @CreatedDate
    private LocalDateTime issuedDateTime;

    private LocalDateTime usedDateTime;

    public static CouponIssue build(long couponId, long userId) {
        return CouponIssue.builder()
            .couponId(couponId)
            .userId(userId)
            .issuedDateTime(LocalDateTime.now())
            .build();
    }

}
