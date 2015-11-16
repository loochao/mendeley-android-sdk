package com.mendeley.api.request.provider;

import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Annotation;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.params.AnnotationRequestParameters;
import com.mendeley.api.request.PatchAuthorizedRequest;
import com.mendeley.api.request.PostNetworkRequest;

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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.API_URL;

/**
 * NetworkProvider for Annotations API calls.
 */
public class AnnotationsNetworkProvider {
    public static String ANNOTATIONS_BASE_URL = API_URL + "annotations";
    private static String CONTENT_TYPE = "application/vnd.mendeley-annotation.1+json";


    /* URLS */

    public static String deleteAnnotationUrl(String documentId) {
        return ANNOTATIONS_BASE_URL + "/" + documentId;
    }

    public static String getAnnotationUrl(String documentId) {
        return ANNOTATIONS_BASE_URL + "/" + documentId;
    }

	public static String getAnnotationsUrl(AnnotationRequestParameters params) throws UnsupportedEncodingException {
		StringBuilder url = new StringBuilder();
		url.append(ANNOTATIONS_BASE_URL);

		if (params != null) {
            StringBuilder paramsString = new StringBuilder();
			boolean firstParam = true;
            if (params.documentId != null) {
                paramsString.append(firstParam ? "?" : "&").append("document_id=" + params.documentId);
                firstParam = false;
            }
			if (params.groupId != null) {
				paramsString.append(firstParam ? "?" : "&").append("group_id=" + params.groupId);
				firstParam = false;
			}
            if (params.includeTrashed != null) {
                paramsString.append(firstParam ? "?" : "&").append("include_trashed=" + params.includeTrashed);
                firstParam = false;
            }
			if (params.modifiedSince != null) {
				paramsString.append(firstParam ? "?" : "&").append("modified_since="
                        + URLEncoder.encode(params.modifiedSince, "ISO-8859-1"));
				firstParam = false;
			}
            if (params.deletedSince != null) {
                paramsString.append(firstParam ? "?" : "&").append("deleted_since="
                        + URLEncoder.encode(params.deletedSince, "ISO-8859-1"));
                firstParam = false;
            }
			if (params.limit != null) {
				paramsString.append(firstParam ? "?" : "&").append("limit=" + params.limit);
				firstParam = false;
			}
            url.append(paramsString.toString());
		}
		
		return url.toString();
	}

    /* PROCEDURES */

    public static class GetAnnotationRequest extends GetAuthorizedRequest<Annotation> {
        public GetAnnotationRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        @Override
        protected Annotation manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseAnnotation(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", CONTENT_TYPE);
        }
    }

    public static class GetAnnotationsRequest extends GetAuthorizedRequest<List<Annotation>> {
        public GetAnnotationsRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        @Override
        protected List<Annotation> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseAnnotationList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", CONTENT_TYPE);
        }
   }

    public static class PostAnnotationRequest extends PostNetworkRequest<Annotation> {
        private final Annotation annotation;

        public PostAnnotationRequest(Annotation annotation, AuthTokenManager authTokenManager, ClientCredentials clientCredentials){
            super(ANNOTATIONS_BASE_URL, authTokenManager, clientCredentials);
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
            headers.put("Content-type", CONTENT_TYPE);
        }
    }

    public static class PatchAnnotationAuthorizedRequest extends PatchAuthorizedRequest<Annotation> {
        private final Annotation annotation;

        public PatchAnnotationAuthorizedRequest(String annotationId, Annotation annotation, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getUrl(annotationId), null, authTokenManager, clientCredentials);
            this.annotation = annotation;
        }

        private static String getUrl(String annotationId) {
            return ANNOTATIONS_BASE_URL + "/" + annotationId;
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", CONTENT_TYPE);
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
}
