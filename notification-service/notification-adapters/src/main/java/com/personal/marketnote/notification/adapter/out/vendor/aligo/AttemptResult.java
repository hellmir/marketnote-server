package com.personal.marketnote.notification.adapter.out.vendor.aligo;

/**
 * Aligo HTTP 호출의 시도 1회 결과 메타.
 *
 * <p>retry 정책 적용 시 시도별 outcome을 누적하여 통신 이력 payload JSON에 함께 저장한다.</p>
 *
 * @param number     1-based 시도 번호
 * @param status     시도 결과 분류
 * @param httpStatus HTTP_FAILURE 시 statusCode, 그 외 {@code null}
 * @param exception  예외 식별자 — HTTP_FAILURE/PARSE_FAILURE 시 예외 클래스 SimpleName,
 *                   SUCCESS 시 resultCode 비정상이면 {@code RESULT_CODE_X}, 정상이면 {@code null}
 */
public record AttemptResult(
        int number,
        AttemptStatus status,
        Integer httpStatus,
        String exception
) {
    public static AttemptResult success(int number, String exception) {
        return new AttemptResult(number, AttemptStatus.SUCCESS, null, exception);
    }

    public static AttemptResult httpFailure(int number, int httpStatus, String exception) {
        return new AttemptResult(number, AttemptStatus.HTTP_FAILURE, httpStatus, exception);
    }

    public static AttemptResult parseFailure(int number, String exception) {
        return new AttemptResult(number, AttemptStatus.PARSE_FAILURE, null, exception);
    }
}
