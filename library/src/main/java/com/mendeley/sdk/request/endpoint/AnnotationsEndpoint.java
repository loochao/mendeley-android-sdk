package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.model.Annotation;
import com.mendeley.sdk.request.DeleteAuthorizedRequest;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.request.PatchAuthorizedRequest;
import com.mendeley.sdk.request.PostAuthorizedRequest;
import com.mendeley.sdk.util.DateUtils;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mendeley.sdk.request.Request.MENDELEY_API_BASE_URL;


public class AnnotationsEndpoint {

    public static String ANNOTATIONS_BASE_URL = MENDELEY_API_BASE_URL + "annotations";
    private static String ANNOTATIONS_CONTENT_TYPE = "application/vnd.mendeley-annotation.1+json";

    public static class GetAnnotationRequest extends GetAuthorizedRequest<Annotation> {
        public GetAnnotationRequest(String annotationId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(ANNOTATIONS_BASE_URL + "/" + annotationId), authTokenManager, clientCredentials);
        }

        @Override
        protected Annotation manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseAnnotation(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", ANNOTATIONS_CONTENT_TYPE);
        }
    }

    public static class GetAnnotationsRequest extends GetAuthorizedRequest<List<Annotation>> {

        private static Uri getAnnotationsUrl(AnnotationRequestParameters params) {
            final Uri uri = Uri.parse(ANNOTATIONS_BASE_URL);
            return params != null ? params.appendToUi(uri) : uri;
        }

        public GetAnnotationsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetAnnotationsRequest(AnnotationRequestParameters parameters, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(getAnnotationsUrl(parameters), authTokenManager, clientCredentials);
        }

        @Override
        protected List<Annotation> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseAnnotationList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", ANNOTATIONS_CONTENT_TYPE);
        }
   }

    public static class PostAnnotationRequest extends PostAuthorizedRequest<Annotation> {
        private final Annotation annotation;

        public PostAnnotationRequest(Annotation annotation, AuthTokenManager authTokenManager, ClientCredentials clientCredentials){
            super(Uri.parse(ANNOTATIONS_BASE_URL), authTokenManager, clientCredentials);
            this.annotation = annotation;
        }

        @Override
        protected void writePostBody(OutputStream os) throws Exception {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(JsonParser.jsonFromAnnotation(annotation));
            writer.flush();
        }
        @Override
        protected Annotation manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.parseAnnotation(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", ANNOTATIONS_CONTENT_TYPE);
        }
    }

    public static class PatchAnnotationRequest extends PatchAuthorizedRequest<Annotation> {
        private final Annotation annotation;

        public PatchAnnotationRequest(String annotationId, Annotation annotation, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getUrl(annotationId), null, authTokenManager, clientCredentials);
            this.annotation = annotation;
        }

        private static Uri getUrl(String annotationId) {
            return Uri.parse(ANNOTATIONS_BASE_URL + "/" + annotationId);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", ANNOTATIONS_CONTENT_TYPE);
        }

        @Override
        protected HttpEntity createPatchingEntity() throws Exception {
            final String json = JsonParser.jsonFromAnnotation(annotation);
            return new StringEntity(json, "UTF-8");
        }

        @Override
        protected Annotation manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.parseAnnotation(reader);
        }
    }

    public static class DeleteAnnotationRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteAnnotationRequest(String annotationId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(ANNOTATIONS_BASE_URL + "/" + annotationId), authTokenManager, clientCredentials);
        }
    }

    /**
     * Parameters for requests to retrieve annotations.
     * <p>
     * Uninitialised properties will be ignored.
     */
    public static class AnnotationRequestParameters {
        public String documentId;

        public String groupId;

        public Boolean includeTrashed;

        /**
         * Returns only annotations modified since this timestamp. Should be supplied in ISO 8601 format.
         */
        public Date modifiedSince;

        /**
         * Returns only annotations deleted since this timestamp. Should be supplied in ISO 8601 format.
         */
        public Date deletedSince;

        /**
         * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
         */
        public Integer limit;


        Uri appendToUi(Uri uri) {
            final Uri.Builder bld = uri.buildUpon();


            if (documentId != null) {
                bld.appendQueryParameter("document_id", documentId);
            }
            if (groupId != null) {
                bld.appendQueryParameter("group_id", groupId);
            }
            if (includeTrashed != null) {
                bld.appendQueryParameter("include_trashed", String.valueOf(includeTrashed));
            }
            if (modifiedSince != null) {
                bld.appendQueryParameter("modified_since", DateUtils.formatMendeleyApiTimestamp(modifiedSince));
            }
            if (deletedSince != null) {
                bld.appendQueryParameter("deleted_since", DateUtils.formatMendeleyApiTimestamp(deletedSince));
            }
            if (limit != null) {
                bld.appendQueryParameter("limit", String.valueOf(limit));
            }

            return bld.build();
        }

    }
}
