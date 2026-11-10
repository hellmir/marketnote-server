package com.personal.marketnote.common.utility;

public class RegularExpressionConstant {
    public static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9]{2,10}$";
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String FULL_NAME_PATTERN = "^[가-힣]{2,10}$";
    // 허용 prefix 그룹 — PHONE_NUMBER_PATTERN / PHONE_NUMBER_INPUT_PATTERN 공유.
    //   - 모바일: 01[016789]
    //   - 서울 유선: 02
    //   - 광역/도 유선: 0(3[1-3]|4[1-4]|5[1-5]|6[1-4]) (KISA 정식 지역번호)
    //   - 인터넷 전화: 070
    private static final String PHONE_PREFIX_GROUP = "(01[016789]|02|0(3[1-3]|4[1-4]|5[1-5]|6[1-4])|070)";
    // Canonical 단일 패턴: 하이픈 없는 9~11자리(prefix + 7~8자리).
    // 하이픈 제거는 PhoneNumber VO 내부에서 수행된다.
    // Request DTO 의 경계 검증에는 하이픈 허용 PHONE_NUMBER_INPUT_PATTERN 을 사용한다.
    public static final String PHONE_NUMBER_PATTERN = "^" + PHONE_PREFIX_GROUP + "\\d{7,8}$";
    // DTO 경계 검증용. canonical 또는 하이픈 포함 입력 둘 다 수용.
    // VO 생성 시 canonical 로 정규화됨.
    public static final String PHONE_NUMBER_INPUT_PATTERN = "^" + PHONE_PREFIX_GROUP + "-?\\d{3,4}-?\\d{4}$";
    public static final String POSITIVE_INTEGER_PATTERN = "^([1-9]\\d*)$";
    public static final String ZERO_OR_POSITIVE_INTEGER_PATTERN = "^(0|[1-9]\\d*)$";
    public static final String RECIPIENT_NAME_PATTERN = "^[가-힣a-zA-Z\\s]{1,50}$";
    // RandomCodeGenerator.generateReferenceCode()는 보다 좁은 알파벳(A-HJ-NPQR-Y + 3-4,6-9)으로 6자리를 생성하지만,
    // 기존 데이터 호환과 외부 입력 유연성을 위해 VO 포맷 검증은 [A-Z0-9] superset으로 완화했다.
    public static final String REFERENCE_CODE_PATTERN = "^[A-Z0-9]{6}$";
    public static final String ZIP_CODE_PATTERN = "^\\d{5}$";
    public static final String NO_HTML_TAG_PATTERN = "^[^<>]*$";
    private static final String IPV4_OCTET = "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])";
    public static final String IPV4_PATTERN = "^" + IPV4_OCTET + "(\\." + IPV4_OCTET + "){3}$";
    public static final String IPV6_PATTERN =
            "^(" +
                    "([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +
                    "([0-9a-fA-F]{1,4}:){1,7}:|" +
                    "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
                    "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
                    "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
                    "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
                    "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
                    "[0-9a-fA-F]{1,4}:(:[0-9a-fA-F]{1,4}){1,6}|" +
                    ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +
                    "::(ffff(:0{1,4})?:)?" + IPV4_OCTET + "(\\." + IPV4_OCTET + "){3}" +
                    ")$";
}
