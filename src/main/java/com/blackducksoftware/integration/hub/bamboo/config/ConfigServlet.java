package com.blackducksoftware.integration.hub.bamboo.config;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

public class ConfigServlet extends HttpServlet {

	private static final long serialVersionUID = -6481973138745479044L;

	private final UserManager userManager;
	private final LoginUriProvider loginUriProvider;
	private final TemplateRenderer renderer;

	public ConfigServlet(final UserManager userManager, final LoginUriProvider loginUriProvider,
			final TemplateRenderer renderer) {

		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.renderer = renderer;
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final String userName = userManager.getRemoteUsername(request);

		if (userName == null || !userManager.isSystemAdmin(userName)) {
			redirectToLogin(request, response);
			return;
		}

		response.setContentType("text/html;charset=utf-8");
		renderer.render("config.vm", response.getWriter());
	}

	private void redirectToLogin(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {
		// this uses the machine hostname so if you are using your laptop it may
		// not resolve to a proper hostname.
		response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
	}

	private URI getUri(final HttpServletRequest request) {
		final StringBuffer builder = request.getRequestURL();

		if (StringUtils.isNotBlank(request.getQueryString())) {
			builder.append("?");
			builder.append(request.getQueryString());
		}

		return URI.create(builder.toString());
	}

}
