package com.blackducksoftware.integration.hub.bamboo.config.valdators;

import com.blackducksoftware.integration.hub.exception.ValidationException;
import com.blackducksoftware.integration.hub.validate.HubServerConfigValidator;

public class HubBambooServerConfigValidator extends HubServerConfigValidator<ValidationException> {

	@Override
	public ValidationException handleValidationException(final ValidationException e) {
		return BambooValidatorDelegate.getInstance().handleValidationException(e);
	}

	@Override
	public ValidationException handleSuccess() {
		return BambooValidatorDelegate.getInstance().handleSuccess();
	}

}
