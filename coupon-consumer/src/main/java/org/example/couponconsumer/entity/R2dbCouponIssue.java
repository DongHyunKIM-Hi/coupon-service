package org.example.couponconsumer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "coupon_issues")
public class R2dbCouponIssue extends R2dbBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime issuedDateTime;

    private LocalDateTime usedDateTime;

    public static R2dbCouponIssue build(long couponId, long userId) {
        return R2dbCouponIssue.builder()
            .couponId(couponId)
            .userId(userId)
            .build();
    }

}
