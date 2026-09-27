package com.personal.marketnote.common.utility;

public class RegularExpressionConstant {
    public static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9]{2,10}$";
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String FULL_NAME_PATTERN = "^[가-힣]{2,10}$";
    public static final String PHONE_NUMBER_PATTERN = "^01[016789]-\\d{3,4}-\\d{4}$";
    public static final String POSITIVE_INTEGER_PATTERN = "^([1-9]\\d*)$";
    public static final String ZERO_OR_POSITIVE_INTEGER_PATTERN = "^(0|[1-9]\\d*)$";
    public static final String RECIPIENT_NAME_PATTERN = "^[가-힣a-zA-Z\\s]{1,50}$";
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
