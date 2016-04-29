package com.blackducksoftware.integration.hub.bamboo.config.valdators;

import com.blackducksoftware.integration.hub.exception.ValidationException;

public class BambooValidatorDelegate implements Cloneable {

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private static BambooValidatorDelegate INSTANCE = new BambooValidatorDelegate();

	private BambooValidatorDelegate() {
	}

	public static BambooValidatorDelegate getInstance() {
		return INSTANCE;
	}

	public ValidationException handleValidationException(final ValidationException e) {
		return e;
	}

	public ValidationException handleSuccess() {
		return null;
	}
}
