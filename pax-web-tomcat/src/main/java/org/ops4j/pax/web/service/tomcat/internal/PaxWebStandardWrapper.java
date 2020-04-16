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
package org.ops4j.pax.web.service.tomcat.internal;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.valves.ValveBase;
import org.ops4j.pax.web.service.spi.model.OsgiContextModel;
import org.ops4j.pax.web.service.spi.model.elements.ServletModel;
import org.ops4j.pax.web.service.spi.servlet.OsgiInitializedServlet;
import org.ops4j.pax.web.service.spi.servlet.OsgiScopedServletContext;
import org.ops4j.pax.web.service.spi.servlet.OsgiServletContext;
import org.osgi.framework.ServiceReference;

/**
 * Tomcat's version of Jetty's {@code org.ops4j.pax.web.service.jetty.internal.PaxWebServletHolder} which
 * helps with OSGi-specific allocation/deallocation of the servlet
 */
public class PaxWebStandardWrapper extends StandardWrapper {

	private final ServletModel servletModel;
	private final OsgiContextModel osgiContextModel;
	private final PaxWebStandardContext realContext;

	private Class<? extends Servlet> servletClass;
	private ServiceReference<? extends Servlet> serviceReference;

	/** This {@link ServletContext} is scoped to single {@link org.osgi.service.http.context.ServletContextHelper} */
	private final OsgiServletContext osgiServletContext;
	/** This {@link ServletContext} is scoped to particular Whiteboard servlet */
	private final OsgiScopedServletContext servletContext;

	/**
	 * Special flag to mark the holder is one with 404 servlet. In such case we don't run OSGi specific
	 * security methods and we have to tweak filter config to return proper {@link ServletContext}.
	 */
	private boolean is404 = false;

	private Container originalParent;

	/**
	 * Constructor to use when wrapping internal {@link Servlet servlets} which won't use OSGi machinery.
	 *  @param name
	 * @param container
	 * @param servlet
	 */
	PaxWebStandardWrapper(String name, PaxWebStandardContext container, Servlet servlet, boolean is404) {
		super();

		servletModel = null;
		osgiContextModel = null;
		osgiServletContext = null;
		realContext = container;
		servletContext = null;
		this.is404 = is404;

		setServlet(servlet);
		setParent(container);

		getPipeline().addValve(new PaxWebStandardWrapperValve((ValveBase) getPipeline().getBasic(), this, realContext));
	}

	/**
	 * Initialize {@link PaxWebStandardWrapper} with {@link ServletModel} and this particular {@link OsgiContextModel}
	 * in which' context we're adding given servlet to Jetty.
	 * @param servletModel
	 * @param osgiContextModel
	 * @param osgiServletContext {@link org.osgi.service.http.context.ServletContextHelper} specific {@link ServletContext}
	 * @param realContext
	 */
	public PaxWebStandardWrapper(ServletModel servletModel, OsgiContextModel osgiContextModel,
			OsgiServletContext osgiServletContext, PaxWebStandardContext realContext) {
		super();

		this.servletModel = servletModel;
		this.osgiContextModel = osgiContextModel;
		this.osgiServletContext = osgiServletContext;
		this.realContext = realContext;

		setName(servletModel.getName());
		if (servletModel.getServletClass() != null) {
			this.servletClass = servletModel.getServletClass();
		} else if (servletModel.getServlet() != null) {
			setServlet(servletModel.getServlet());
		} else {
			this.serviceReference = servletModel.getElementReference();
		}

		parameters.clear();
		parameters.putAll(servletModel.getInitParams());

		setAsyncSupported(servletModel.getAsyncSupported() != null && servletModel.getAsyncSupported());
		if (servletModel.getLoadOnStartup() != null) {
			setLoadOnStartup(servletModel.getLoadOnStartup());
		}
		setMultipartConfigElement(servletModel.getMultipartConfigElement());

		// TODO: scan servlet for annotations

		// setup proper delegation for ServletContext
		servletContext = new OsgiScopedServletContext(this.osgiServletContext, servletModel.getRegisteringBundle());

		// setup proper pipeline - that will invoke the servlet with proper filter chain and with proper req/res
		// wrappers
		// "basic" valve is org.apache.catalina.core.StandardWrapperValve
		getPipeline().addValve(new PaxWebStandardWrapperValve((ValveBase) getPipeline().getBasic(), this, realContext));
	}

	@Override
	protected synchronized void startInternal() throws LifecycleException {
		super.startInternal();
		originalParent = getParent();
		setParent(new PaxWebScopedStandardContext(this, realContext));
	}

	@Override
	protected synchronized void stopInternal() throws LifecycleException {
		setParent(originalParent);
		super.stopInternal();
	}

	public OsgiContextModel getOsgiContextModel() {
		return osgiContextModel;
	}

	/**
	 * This method is called from {@link javax.servlet.ServletConfig} passed to the servlet instance of this wrapper.
	 * Thus there's no need to create {@link OsgiInitializedServlet} wrapper around the servlet.
	 * @return
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	public boolean is404() {
		return is404;
	}

	@Override
	public synchronized Servlet loadServlet() throws ServletException {
		Servlet instance = super.getServlet();
		if (instance == null) {
			if (servletModel.getElementReference() != null) {
				// obtain Servlet using reference
				instance = servletModel.getRegisteringBundle().getBundleContext().getService(servletModel.getElementReference());
			} else if (servletClass != null) {
				try {
					instance = servletClass.newInstance();
				} catch (Exception e) {
					throw new ServletException(e.getMessage(), e);
				}
			}
		}

		if (instance == null) {
			throw new IllegalStateException("Can't load servlet for " + servletModel);
		}

		// no need to create OsgiInitializedServlet, because ServletConfig will be correct
//		return new OsgiInitializedServlet(instance, servletContext);
		return instance;
	}

	@Override
	public synchronized void unload() throws ServletException {
		super.unload();
		if (servletModel != null && servletModel.getElementReference() != null) {
			servletModel.getRegisteringBundle().getBundleContext().ungetService(servletModel.getElementReference());
		}
	}

}
