package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.request.Request;
import com.mendeley.api.request.SignedInTest;
import com.mendeley.api.util.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class TrashEndpointTest extends SignedInTest {

    // TODO create tests for GET, DELETE and RESTORE

    @SmallTest
    public void test_getTrashDocuments_useTheRightUrl_noParams() {
        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("trash").build();
        Uri actual = getRequestFactory().getTrashedDocuments().getUrl();

        assertEquals("Request url is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_getTrashDocuments_useTheRightUrl_withParams() throws ParseException {
        DocumentEndpoint.DocumentRequestParameters.View view = DocumentEndpoint.DocumentRequestParameters.View.ALL;
        String groupId = "test-group_id";
        Date modifiedSince = DateUtils.parseMendeleyApiTimestamp("2014-02-28T11:52:30.000Z");
        Date deletedSince = DateUtils.parseMendeleyApiTimestamp("2014-01-21T11:52:30.000Z");
        int limit = 7;
        boolean reverse = true;
        DocumentEndpoint.DocumentRequestParameters.Order order = DocumentEndpoint.DocumentRequestParameters.Order.DESC;
        DocumentEndpoint.DocumentRequestParameters.Sort sort = DocumentEndpoint.DocumentRequestParameters.Sort.MODIFIED;

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon()
                .appendPath("trash")
                .appendQueryParameter("view", view.getValue())
                .appendQueryParameter("group_id", groupId)
                .appendQueryParameter("modified_since", DateUtils.formatMendeleyApiTimestamp(modifiedSince))
                .appendQueryParameter("limit", String.valueOf(limit))
                .appendQueryParameter("reverse", String.valueOf(reverse))
                .appendQueryParameter("order", order.getValue())
                .appendQueryParameter("sort", sort.getValue())
                .appendQueryParameter("deleted_since", DateUtils.formatMendeleyApiTimestamp(deletedSince))
                .build();

        DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.view = view;
        params.groupId = groupId;
        params.modifiedSince = modifiedSince;
        params.limit = 7;
        params.reverse = true;
        params.order = order;
        params.sort = sort;
        params.deletedSince = deletedSince;


        final Uri actualUrl = getRequestFactory().getTrashedDocuments(params).getUrl();

        assertEquals("Request url is wrong", expectedUrl, actualUrl);
    }


    @SmallTest
    public void test_deleteTrashDocument_useTheRightUrl() {
        final String docId = "theDocId";

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("trash").appendPath(docId).build();
        final Uri actual = getRequestFactory().deleteTrashedDocument(docId).getUrl();

        assertEquals("Request url is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_restoreTrashesDocument_useTheRightUrl() {
        final String docId = "theDocId";

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("trash").appendPath(docId).appendPath("restore").build();
        final Uri actual = getRequestFactory().restoreDocument(docId).getUrl();

        assertEquals("Request url is wrong", expectedUrl, actual);
    }
}
