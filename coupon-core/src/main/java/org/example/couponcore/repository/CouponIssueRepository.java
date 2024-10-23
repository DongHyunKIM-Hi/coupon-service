package org.example.couponcore.repository;

import org.example.couponcore.model.entity.base.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueRepository extends JpaRepository<Long, CouponIssue> {

}
