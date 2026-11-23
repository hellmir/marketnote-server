package com.personal.marketnote.notification.adapter.out.vendor.aligo;

/**
 * Aligo HTTP 호출 1회의 결과 분류.
 *
 * <ul>
 *   <li>{@link #SUCCESS}: HTTP 200 응답 + 응답 파싱 성공. resultCode 비정상도 SUCCESS로 분류하고
 *       exception 필드에 {@code RESULT_CODE_X}를 표기한다(retry 비대상).</li>
 *   <li>{@link #HTTP_FAILURE}: HTTP 4xx/5xx 응답.</li>
 *   <li>{@link #PARSE_FAILURE}: HTTP 200이지만 응답 파싱 실패.</li>
 * </ul>
 */
public enum AttemptStatus {
    SUCCESS,
    HTTP_FAILURE,
    PARSE_FAILURE
}
