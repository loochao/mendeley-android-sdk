package com.mendeley.api.request.provider;

import static com.mendeley.api.request.Request.API_URL;

public class TrashNetworkProvider {
    public static String BASE_URL = API_URL + "trash";




    /* URLS */

    public static String getRecoverUrl(String documentId) {
        return BASE_URL + "/" + documentId + "/restore";
    }

    public static String getDeleteUrl(String documentId) {
        return BASE_URL + "/" + documentId;
    }


}
