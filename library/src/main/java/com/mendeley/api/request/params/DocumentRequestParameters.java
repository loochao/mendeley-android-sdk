package com.mendeley.api.request.params;

/**
 * Parameters for requests to retrieve documents.
 * <p>
 * Uninitialised properties will be ignored.
 */
public class DocumentRequestParameters {
	/**
	 * The required document view.
	 */
	public View view;
	
	/**
	 * Group ID. If not supplied, returns user documents.
	 */
	public String groupId;
	
	/**
	 * Returns only documents modified since this timestamp. Should be supplied in ISO 8601 format.
	 */
	public String modifiedSince;
	
	/**
	 * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
	 */
	public Integer limit;
	
	/**
	 * A flag to indicate that the scrolling direction has switched.
	 */
	public Boolean reverse;
	
	/**
	 * The sort order.
	 */
	public Order order;
	
	/**
	 * The field to sort on.
	 */
	public Sort sort;
}
