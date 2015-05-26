package com.mendeley.api.request.params;

/**
 * This class represents parameters for folder SDK requests.
 * <p>
 * Uninitialised properties will be ignored.
 */
public class FolderRequestParameters {
	/**
	 * Group ID. If not supplied, returns user folders.
	 */
	public String groupId;
	
	/**
	 * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
	 */
	public Integer limit;
}
