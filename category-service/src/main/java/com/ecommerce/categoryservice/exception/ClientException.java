package com.ecommerce.categoryservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientException extends RuntimeException {

	private int errorCode;

	private String errorMessage;

	private String errorDescription;

	public ClientException(String message, int errorCode, String errorMessage, String errorDescription) {
		super(message);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.errorDescription = errorDescription;
	}

}
