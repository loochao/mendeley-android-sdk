package com.mendeley.api.request.provider;

import android.net.Uri;

import static com.mendeley.api.request.Request.API_URL;

public class TrashNetworkProvider {
    public static String BASE_URL = API_URL + "trash";




    /* URLS */

    public static Uri getRecoverUrl(String documentId) {
        return Uri.parse(BASE_URL + "/" + documentId + "/restore");
    }

    public static Uri getDeleteUrl(String documentId) {
        return Uri.parse( BASE_URL + "/" + documentId);
    }


}
