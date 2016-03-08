package com.thomsonreuters.rest.service;

import javax.ws.rs.core.HttpHeaders;

public class Utils {

	public static final String AUTH_HEADER_NAME = "X-1P-User";

	public static String getUserid(HttpHeaders headers) {
		if (headers == null)
			return null;
		try {
			return headers.getRequestHeader(AUTH_HEADER_NAME).get(0);
		} catch (Exception e) {
			return null;
		}
	}
}
