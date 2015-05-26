package com.mendeley.api.request.params;

/**
 * Parameters for requests to retrieve annotations.
 * <p>
 * Uninitialised properties will be ignored.
 */
public class AnnotationRequestParameters {
    public String documentId;

	public String groupId;

	public Boolean includeTrashed;

	/**
	 * Returns only annotations modified since this timestamp. Should be supplied in ISO 8601 format.
	 */
	public String modifiedSince;

    /**
     * Returns only annotations deleted since this timestamp. Should be supplied in ISO 8601 format.
     */
    public String deletedSince;

    /**
	 * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
	 */
	public Integer limit;
}
