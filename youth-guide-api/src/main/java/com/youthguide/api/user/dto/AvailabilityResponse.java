package com.youthguide.api.user.dto;

/**
 * GAP-03 중복확인 응답.
 * field: loginName | nickname | email, available: 지금 사용 가능하면 true.
 * 사전 확인일 뿐이며, 가입 시점에 다시 검사한다.
 */
public record AvailabilityResponse(String field, boolean available) {
}
