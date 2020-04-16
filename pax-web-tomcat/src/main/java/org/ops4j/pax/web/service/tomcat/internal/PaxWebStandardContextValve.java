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

import java.io.IOException;
import javax.servlet.ServletException;

import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.ops4j.pax.web.service.spi.servlet.Default404Servlet;

/**
 * <p>This valve for entire context ensures (for now, could do more soon) that if there's no target servlet
 * mapped, we still invoke preprocessors and filters.
 * Tomcat itself ensures it by adding "default servlet" which acts as "resource servlet". We don't
 * want to do it in Pax Web, because resource serving should not be "default" action - it has to be
 * configured explicitly.</p>
 *
 * <p>This valve is invoked before original {@link org.apache.catalina.core.StandardContextValve}.</p>
 */
public class PaxWebStandardContextValve extends ValveBase {

	private final PaxWebStandardWrapper wrapperFor404Servlet;

	public PaxWebStandardContextValve(ValveBase next, Default404Servlet defaultServlet) {
		setNext(next);
		setAsyncSupported(next.isAsyncSupported());
		setContainer(next.getContainer());
		setDomain(next.getDomain());

		wrapperFor404Servlet = new PaxWebStandardWrapper(null, (PaxWebStandardContext) getContainer(),
				defaultServlet, true);
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		Wrapper wrapper = request.getWrapper();
		if (wrapper == null) {
			// we need SOME wrapper, so preprocessors/security/filters are called correctly
			request.getMappingData().wrapper = wrapperFor404Servlet;
		}

		getNext().invoke(request, response);
	}

}
