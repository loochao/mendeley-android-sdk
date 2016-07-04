package com.mendeley.sdk.util;


public interface MssoCookieManager {

    void saveMSSOCookieValue(String mssoCookie);

    String getMssoCookieValue();
}
