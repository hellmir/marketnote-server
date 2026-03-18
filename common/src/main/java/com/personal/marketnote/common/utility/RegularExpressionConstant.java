package com.personal.marketnote.common.utility;

public class RegularExpressionConstant {
    public static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9]{2,10}$";
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String FULL_NAME_PATTERN = "^[가-힣]{2,10}$";
    public static final String PHONE_NUMBER_PATTERN = "^01[016789]-\\d{3,4}-\\d{4}$";
    public static final String POSITIVE_INTEGER_PATTERN = "^([1-9]\\d*)$";
    public static final String ZERO_OR_POSITIVE_INTEGER_PATTERN = "^(0|[1-9]\\d*)$";
}
