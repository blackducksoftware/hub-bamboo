package com.blackducksoftware.integration.hub.bamboo;

public class BDBambooHubPluginException extends Exception {

	private static final long serialVersionUID = -3403527734617340974L;

	public BDBambooHubPluginException() {

	}

	public BDBambooHubPluginException(final String message) {
		super(message);
	}

	public BDBambooHubPluginException(final Throwable cause) {
		super(cause);
	}

	public BDBambooHubPluginException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
