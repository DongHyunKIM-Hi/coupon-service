package org.example.couponapi.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public record CouponIssueResponseDto(
    boolean isIssued,
    String message
) {

}
