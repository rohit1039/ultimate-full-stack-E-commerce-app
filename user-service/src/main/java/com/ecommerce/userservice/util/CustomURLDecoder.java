package com.ecommerce.userservice.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CustomURLDecoder {

	public static String decodeValue(String value) {

		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

}
