package org.example.couponconsumer.repository;

import org.example.couponcore.model.entity.base.CouponIssue;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CouponIssueR2DBRepository extends ReactiveCrudRepository<CouponIssue, Long> {

}
