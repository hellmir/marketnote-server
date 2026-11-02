package com.personal.marketnote.common.utility;

public class ApiConstant {
    public static final String USER_ID_KEY = "userId";
    public static final String DEFAULT_PAGE_NUMBER = "1";
    public static final String ADMIN_POINTCUT = "isAuthenticated() and hasRole('ROLE_ADMIN')";
    public static final String ADMIN_OR_SELLER_POINTCUT = "isAuthenticated() and (hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER'))";
    public static final String ADMIN_OR_SELLER_PRINCIPAL_POINTCUT
            = "isAuthenticated() and (" +
            "hasAuthority('ROLE_ADMIN') or " +
            "(hasAuthority('ROLE_SELLER') and " +
            " #request.sellerId.toString() == authentication.name)" +
            ")";
    public static final byte INTER_SERVER_MAX_REQUEST_COUNT = 5;
    public static final long INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND = 1000L;
    public static final long INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE = 2;
    public static final long FIRST_PAGE_CURSOR_VALUE = -1L;
}
