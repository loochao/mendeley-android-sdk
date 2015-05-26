package com.mendeley.api.request.provider;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Annotation;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.procedure.GetNetworkRequest;
import com.mendeley.api.request.procedure.PatchNetworkRequest;
import com.mendeley.api.request.procedure.PostNetworkRequest;
import com.mendeley.api.request.params.AnnotationRequestParameters;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.mendeley.api.request.NetworkUtils.API_URL;

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

    public static class GetAnnotationRequest extends GetNetworkRequest<Annotation> {
        public GetAnnotationRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, CONTENT_TYPE, authTokenManager, clientCredentials);
        }

        @Override
        protected Annotation parseJsonString(String jsonString) throws JSONException {
            return JsonParser.parseAnnotation(jsonString);
        }
    }

    public static class GetAnnotationsRequest extends GetNetworkRequest<List<Annotation>> {
        public GetAnnotationsRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, CONTENT_TYPE, authTokenManager, clientCredentials);
        }

        @Override
        protected List<Annotation> parseJsonString(String jsonString) throws JSONException {
            return JsonParser.parseAnnotationList(jsonString);
        }
   }

    public static class PostAnnotationRequest extends PostNetworkRequest<Annotation> {
        private final Annotation annotation;

        public PostAnnotationRequest(Annotation annotation, AuthTokenManager authTokenManager, ClientCredentials clientCredentials){
            super(ANNOTATIONS_BASE_URL, CONTENT_TYPE, authTokenManager, clientCredentials);
            this.annotation = annotation;
        }

        @Override
        protected String obtainJsonToPost() throws JSONException {
            return JsonParser.jsonFromAnnotation(annotation);
        }

        @Override
        protected Annotation parseJsonString(String jsonString) throws JSONException {
            return JsonParser.parseAnnotation(jsonString);
        }
    }

    public static class PatchAnnotationRequest extends PatchNetworkRequest<Annotation> {
        private final Annotation annotation;

        public PatchAnnotationRequest(String annotationId, Annotation annotation, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getUrl(annotationId), CONTENT_TYPE, null, authTokenManager, clientCredentials);
            this.annotation = annotation;
        }

        private static String getUrl(String annotationId) {
            return ANNOTATIONS_BASE_URL + "/" + annotationId;
        }

        @Override
        protected String obtainJsonToPost() throws JSONException {
            return JsonParser.jsonFromAnnotation(annotation);
        }

        @Override
        protected Annotation processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseAnnotation(jsonString);
        }
    }
}
