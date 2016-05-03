package com.blackducksoftware.integration.hub.bamboo.config.valdators;

import com.blackducksoftware.integration.hub.validate.HubCredentialsValidator;
import com.blackducksoftware.integration.hub.validate.ValidationResult;

public class HubBambooCredentialsValidator extends HubCredentialsValidator<ValidationResult> {
	@Override
	public ValidationResult processResult(final ValidationResult result) {
		return result;
	}
}
