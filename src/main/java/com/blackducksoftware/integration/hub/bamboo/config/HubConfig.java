package com.blackducksoftware.integration.hub.bamboo.config;

import java.io.Serializable;

public class HubConfig implements Serializable {

	private static final long serialVersionUID = -3260976719663527625L;

	private String hubUrl;
	private String hubUser;
	private String hubPass;
	private String hubProxyUrl;
	private String hubProxyPort;
	private String hubNoProxyHost;
	private String hubProxyUser;
	private String hubProxyPass;

	public HubConfig() {

	}

	public HubConfig(final String hubUrl, final String hubUser, final String hubPass, final String hubProxyUrl,
			final String hubProxyPort, final String hubNoProxyHost, final String hubProxyUser,
			final String hubProxyPass) {

		this.hubUrl = hubUrl;
		this.hubUser = hubUser;
		this.hubPass = hubPass;
		this.hubProxyUrl = hubProxyUrl;
		this.hubProxyPort = hubProxyPort;
		this.hubNoProxyHost = hubNoProxyHost;
		this.hubProxyUser = hubProxyUser;
		this.hubProxyPass = hubProxyPass;
	}

	public String getHubUrl() {
		return hubUrl;
	}

	public void setHubUrl(final String hubUrl) {
		this.hubUrl = hubUrl;
	}

	public String getHubUser() {
		return hubUser;
	}

	public void setHubUser(final String hubUser) {
		this.hubUser = hubUser;
	}

	public String getHubPass() {
		return hubPass;
	}

	public void setHubPass(final String hubPass) {
		this.hubPass = hubPass;
	}

	public String getHubProxyUrl() {
		return hubProxyUrl;
	}

	public void setHubProxyUrl(final String hubProxyUrl) {
		this.hubProxyUrl = hubProxyUrl;
	}

	public String getHubProxyPort() {
		return hubProxyPort;
	}

	public void setHubProxyPort(final String hubProxyPort) {
		this.hubProxyPort = hubProxyPort;
	}

	public String getHubNoProxyHost() {
		return hubNoProxyHost;
	}

	public void setHubNoProxyHost(final String hubNoProxyHost) {
		this.hubNoProxyHost = hubNoProxyHost;
	}

	public String getHubProxyUser() {
		return hubProxyUser;
	}

	public void setHubProxyUser(final String hubProxyUser) {
		this.hubProxyUser = hubProxyUser;
	}

	public String getHubProxyPass() {
		return hubProxyPass;
	}

	public void setHubProxyPass(final String hubProxyPass) {
		this.hubProxyPass = hubProxyPass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hubNoProxyHost == null) ? 0 : hubNoProxyHost.hashCode());
		result = prime * result + ((hubPass == null) ? 0 : hubPass.hashCode());
		result = prime * result + ((hubProxyPass == null) ? 0 : hubProxyPass.hashCode());
		result = prime * result + ((hubProxyPort == null) ? 0 : hubProxyPort.hashCode());
		result = prime * result + ((hubProxyUrl == null) ? 0 : hubProxyUrl.hashCode());
		result = prime * result + ((hubProxyUser == null) ? 0 : hubProxyUser.hashCode());
		result = prime * result + ((hubUrl == null) ? 0 : hubUrl.hashCode());
		result = prime * result + ((hubUser == null) ? 0 : hubUser.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final HubConfig other = (HubConfig) obj;
		if (hubNoProxyHost == null) {
			if (other.hubNoProxyHost != null) {
				return false;
			}
		} else if (!hubNoProxyHost.equals(other.hubNoProxyHost)) {
			return false;
		}
		if (hubPass == null) {
			if (other.hubPass != null) {
				return false;
			}
		} else if (!hubPass.equals(other.hubPass)) {
			return false;
		}
		if (hubProxyPass == null) {
			if (other.hubProxyPass != null) {
				return false;
			}
		} else if (!hubProxyPass.equals(other.hubProxyPass)) {
			return false;
		}
		if (hubProxyPort == null) {
			if (other.hubProxyPort != null) {
				return false;
			}
		} else if (!hubProxyPort.equals(other.hubProxyPort)) {
			return false;
		}
		if (hubProxyUrl == null) {
			if (other.hubProxyUrl != null) {
				return false;
			}
		} else if (!hubProxyUrl.equals(other.hubProxyUrl)) {
			return false;
		}
		if (hubProxyUser == null) {
			if (other.hubProxyUser != null) {
				return false;
			}
		} else if (!hubProxyUser.equals(other.hubProxyUser)) {
			return false;
		}
		if (hubUrl == null) {
			if (other.hubUrl != null) {
				return false;
			}
		} else if (!hubUrl.equals(other.hubUrl)) {
			return false;
		}
		if (hubUser == null) {
			if (other.hubUser != null) {
				return false;
			}
		} else if (!hubUser.equals(other.hubUser)) {
			return false;
		}
		return true;
	}
}
