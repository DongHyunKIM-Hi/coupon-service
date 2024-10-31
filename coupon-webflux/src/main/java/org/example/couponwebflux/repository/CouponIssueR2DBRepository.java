package org.example.couponwebflux.repository;


import org.example.couponwebflux.model.entity.CouponIssue;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CouponIssueR2DBRepository extends ReactiveCrudRepository<CouponIssue, Long> {

}
