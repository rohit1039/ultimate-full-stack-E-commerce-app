package com.ecommerce.productservice.exception;

public class DuplicateProductException extends RuntimeException {

	public DuplicateProductException(String msg) {
		super(msg);
	}

}
