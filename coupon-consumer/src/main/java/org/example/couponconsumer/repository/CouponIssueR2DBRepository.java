package org.example.couponconsumer.repository;

import org.example.couponconsumer.entity.R2dbCouponIssue;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CouponIssueR2DBRepository extends ReactiveCrudRepository<R2dbCouponIssue, Long> {

}
