package com.blackducksoftware.integration.hub.bamboo.config.valdators;

import com.blackducksoftware.integration.hub.validate.HubProxyInfoValidator;
import com.blackducksoftware.integration.hub.validate.ValidationResult;

public class HubBambooProxyInfoValidator extends HubProxyInfoValidator<ValidationResult> {
	@Override
	public ValidationResult processResult(final ValidationResult result) {
		return result;
	}
}
