/*
 * Copyright 2020 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.web.service.spi.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.ops4j.pax.web.service.spi.model.OsgiContextModel;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 * <p>This class provides special {@link ServletContext#getClassLoader()} method for Whiteboard elements.
 * All Whiteboard services should use a {@link ServletContext} which uses single
 * {@link org.osgi.service.http.HttpContext} or {@link org.osgi.service.http.context.ServletContextHelper}, but
 * additionally, class loader should come from the bundle which registered the Whiteboard service (e.g., a servlet)
 * itself, not from the bundle that has registered the {@link org.osgi.service.http.context.ServletContextHelper}.</p>
 */
public class OsgiScopedServletContext implements ServletContext {

	private final OsgiServletContext osgiContext;
	private final Bundle bundle;

	public OsgiScopedServletContext(OsgiServletContext osgiContext, Bundle bundle) {
		this.osgiContext = osgiContext;
		this.bundle = bundle;
	}

	public OsgiContextModel getOsgiContextModel() {
		return osgiContext.getOsgiContextModel();
	}

	// --- methods that throw UnsupportedOperationException

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, String className) {
		throw new UnsupportedOperationException("addFilter() is not supported.");
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		throw new UnsupportedOperationException("addFilter() is not supported.");
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		throw new UnsupportedOperationException("addFilter() is not supported.");
	}

	@Override
	public void addListener(String className) {
		throw new UnsupportedOperationException("addListener() is not supported.");
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		throw new UnsupportedOperationException("addListener() is not supported.");
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		throw new UnsupportedOperationException("addListener() is not supported.");
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, String className) {
		throw new UnsupportedOperationException("addServlet() is not supported.");
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
		throw new UnsupportedOperationException("addServlet() is not supported.");
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		throw new UnsupportedOperationException("addServlet() is not supported.");
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException("createFilter() is not supported.");
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException("createListener() is not supported.");
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException("createServlet() is not supported.");
	}

	@Override
	public void declareRoles(String... roleNames) {
		throw new UnsupportedOperationException("declareRoles() is not supported.");
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		throw new UnsupportedOperationException("setInitParameter() is not supported.");
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		throw new UnsupportedOperationException("setSessionTrackingModes() is not supported.");
	}

	@Override
	public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
		throw new UnsupportedOperationException("addJspFile() is not supported.");
	}

	// --- methods that are scoped to HttpContext/ServletContextHelper

	@Override
	public Object getAttribute(String name) {
		return osgiContext.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return osgiContext.getAttributeNames();
	}

	@Override
	public void setAttribute(String name, Object object) {
		osgiContext.setAttribute(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		osgiContext.removeAttribute(name);
	}

	// --- methods simply delegating to server-specific ServletContext. Backed by the Servlet Container.

	@Override
	public String getContextPath() {
		return osgiContext.getContextPath();
	}

	@Override
	public ServletContext getContext(String uripath) {
		return osgiContext.getContext(uripath);
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return osgiContext.getDefaultSessionTrackingModes();
	}

	@Override
	public int getEffectiveMajorVersion() {
		return osgiContext.getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return osgiContext.getEffectiveMinorVersion();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return osgiContext.getEffectiveSessionTrackingModes();
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		return osgiContext.getFilterRegistration(filterName);
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return osgiContext.getFilterRegistrations();
	}

	@Override
	public int getMajorVersion() {
		return osgiContext.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return osgiContext.getMinorVersion();
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		return osgiContext.getServlet(name);
	}

	@Override
	public Enumeration<String> getServletNames() {
		return osgiContext.getServletNames();
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return osgiContext.getServletRegistration(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return osgiContext.getServletRegistrations();
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		return osgiContext.getServlets();
	}

	@Override
	public String getVirtualServerName() {
		return osgiContext.getVirtualServerName();
	}

	@Override
	public String getServerInfo() {
		return osgiContext.getServerInfo();
	}

	@Override
	public void log(String msg) {
		osgiContext.log(msg);
	}

	@Override
	public void log(Exception exception, String msg) {
		osgiContext.log(exception, msg);
	}

	@Override
	public void log(String message, Throwable throwable) {
		osgiContext.log(message, throwable);
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return osgiContext.getSessionCookieConfig();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return osgiContext.getJspConfigDescriptor();
	}

	// --- methods also delegating to server-specific ServletContext, but added in Servlet spec 4.0

	@Override
	public int getSessionTimeout() {
		return osgiContext.getSessionTimeout();
	}

	@Override
	public void setSessionTimeout(int sessionTimeout) {
		osgiContext.setSessionTimeout(sessionTimeout);
	}

	@Override
	public String getRequestCharacterEncoding() {
		return osgiContext.getRequestCharacterEncoding();
	}

	@Override
	public void setRequestCharacterEncoding(String encoding) {
		osgiContext.setRequestCharacterEncoding(encoding);
	}

	@Override
	public String getResponseCharacterEncoding() {
		return osgiContext.getResponseCharacterEncoding();
	}

	@Override
	public void setResponseCharacterEncoding(String encoding) {
		osgiContext.setResponseCharacterEncoding(encoding);
	}

	// --- methods backed by the ServletContextHelper.

	@Override
	public String getMimeType(String file) {
		return osgiContext.getMimeType(file);
	}

	@Override
	public String getRealPath(String path) {
		return osgiContext.getRealPath(path);
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return osgiContext.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return osgiContext.getResourceAsStream(path);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return osgiContext.getResourcePaths(path);
	}

	@Override
	public String getInitParameter(String name) {
		return osgiContext.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return osgiContext.getInitParameterNames();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return osgiContext.getRequestDispatcher(path);
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return osgiContext.getNamedDispatcher(name);
	}

	@Override
	public String getServletContextName() {
		return osgiContext.getServletContextName();
	}

	// --- methods dependent on which actual servlet/filter uses the context

	@Override
	public ClassLoader getClassLoader() {
		return bundle.adapt(BundleWiring.class).getClassLoader();
	}

}
