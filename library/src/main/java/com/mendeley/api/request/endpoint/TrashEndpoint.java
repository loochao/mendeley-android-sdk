package com.mendeley.api.request.endpoint;

import android.net.Uri;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class TrashEndpoint {
    public static String BASE_URL = MENDELEY_API_BASE_URL + "trash";

    /* URLS */

    public static Uri getRecoverUrl(String documentId) {
        return Uri.parse(BASE_URL + "/" + documentId + "/restore");
    }

    public static Uri getDeleteUrl(String documentId) {
        return Uri.parse( BASE_URL + "/" + documentId);
    }


}
