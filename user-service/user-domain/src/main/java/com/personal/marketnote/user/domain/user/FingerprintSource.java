package com.personal.marketnote.user.domain.user;

public enum FingerprintSource {
    SIGNUP,
    LOGIN;

    public boolean isSignup() {
        return this == SIGNUP;
    }

    public boolean isLogin() {
        return this == LOGIN;
    }
}
