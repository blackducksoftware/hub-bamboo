package com.blackducksoftware.integration.hub.bamboo.config.valdators;

import com.blackducksoftware.integration.hub.validate.HubServerConfigValidator;
import com.blackducksoftware.integration.hub.validate.ValidationResult;

public class HubBambooServerConfigValidator extends HubServerConfigValidator<ValidationResult> {
	@Override
	public ValidationResult processResult(final ValidationResult result) {
		return result;
	}
}
