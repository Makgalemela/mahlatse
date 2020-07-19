package com.cloud.gateway.security;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

@Component
public class TrackingFilter extends ZuulFilter {

	private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

	private static final int FILTER_ORDER = 1;
	private static final boolean SHOULD_FILTER = true;
	
	private static final String UPGRADE = "Upgrade";

	@Autowired
	FilterUtils filterUtils;

	@Override
	public String filterType() {
		return FilterUtils.PRE_FILTER_TYPE;
	}

	@Override
	public int filterOrder() {
		return FILTER_ORDER;
	}

	public boolean shouldFilter() {
		return SHOULD_FILTER;
	}

	private boolean isCorrelationIdPresent() {
		return filterUtils.getCorrelationId() != null ? true : false;

	}

	private String generateCorrelationId() {
		return java.util.UUID.randomUUID().toString();
	}

	public Object run() {

		if (isCorrelationIdPresent()) {
			logger.debug("tmx-correlation-id found in tracking filter: {}", filterUtils.getCorrelationId());
		} else {
			filterUtils.setCorrelationId(generateCorrelationId());
			logger.debug("tmx-correlation-id generated in tracking filter: {}", filterUtils.getCorrelationId());
		}

		RequestContext ctx = RequestContext.getCurrentContext();
		RequestWrapper wrapper = new RequestWrapper(ctx.getRequest());
		String upgradeHeader = wrapper.getHeader(UPGRADE);
		if (null == upgradeHeader) {
			upgradeHeader = wrapper.getHeader("upgrade");
		}
		if (null != upgradeHeader && "websocket".equalsIgnoreCase(upgradeHeader)) {
			wrapper.addHeader("connection", UPGRADE);
			ctx.addZuulRequestHeader("connection", UPGRADE);
			ctx.setRequest(wrapper);
		}
		logger.info("Processing incoming request for {} ", ctx.getRequest().getRequestURI());
		return null;
	}

	private static class RequestWrapper extends HttpServletRequestWrapper {

		public RequestWrapper(HttpServletRequest request) {
			super(request);
		}

		private Map<String, String> headerMap = new HashMap<String, String>();

		/**
		 * add a header with given name and value
		 *
		 * @param name
		 * @param value
		 */
		public void addHeader(String name, String value) {
			headerMap.put(name, value);
		}

		@Override
		public String getHeader(String name) {
			String headerValue = super.getHeader(name);
			if (headerMap.containsKey(name)) {
				headerValue = headerMap.get(name);
			}
			return headerValue;
		}

		/**
		 * get the Header names
		 */
		@Override
		public Enumeration<String> getHeaderNames() {
			List<String> names = Collections.list(super.getHeaderNames());
			for (String name : headerMap.keySet()) {
				names.add(name);
			}
			return Collections.enumeration(names);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			List<String> values = Collections.list(super.getHeaders(name));

			if (headerMap.containsKey(name)) {
				values.add(headerMap.get(name));
			}

			return Collections.enumeration(values);
		}

	}

}
