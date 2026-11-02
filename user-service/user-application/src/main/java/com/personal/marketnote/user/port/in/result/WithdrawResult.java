package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;

import java.util.Map;

public record WithdrawResult(
        Map<AuthVendor, Boolean> disconnectResults
) {
}
