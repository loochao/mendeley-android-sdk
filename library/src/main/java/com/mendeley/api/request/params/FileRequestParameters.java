package com.mendeley.api.request.params;

import java.util.Date;

/**
 * This class represents parameters for file SDK requests.
 * <p>
 * Uninitialised properties will be ignored.
 */
public class FileRequestParameters {
	/**
	 * The document ID.
	 */
	public String documentId;
	
	/**
	 * The group ID.
	 */
	public String groupId;
	
	/**
	 * Returns only files added since this timestamp. Should be supplied in ISO 8601 format.
	 */
	public Date addedSince;
	
	/**
	 * Returns files deleted since this timestamp. Should be supplied in ISO 8601 format.
	 */
	public Date deletedSince;
	
	/**
	 * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
	 */
	public Integer limit;
	
	/**
	 * The catalog ID.
	 */
	public String catalogId;
}
