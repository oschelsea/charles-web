package com.xenon.ows.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;

/**
 * Filter to make OGC request parameters case-insensitive.
 * Wraps the HttpServletRequest to provide normalized access to parameters.
 */
public class CaseInsensitiveRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            // Only wrap for OGC service endpoints
            String uri = httpRequest.getRequestURI();
            if (uri.contains("/wms") || uri.contains("/wmts") || uri.contains("/wfs")) {
                chain.doFilter(new CaseInsensitiveRequestWrapper(httpRequest), response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static class CaseInsensitiveRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> params = new HashMap<>();

        public CaseInsensitiveRequestWrapper(HttpServletRequest request) {
            super(request);
            // Copy parameters to local map with lower-case keys
            // If multiple keys differ only by case (unlikely for valid OGC), values merge?
            // OGC spec says keys are case-insensitive, so we normalize to one format.
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                String key = entry.getKey();
                // We map all to lowercase key, but keep original values
                String lowerKey = key.toLowerCase();
                String[] values = entry.getValue();
                
                // If collision (e.g. REQUEST and request), standard servlet might already merge or we just append?
                // Simple approach: Overwrite or merge.
                if (params.containsKey(lowerKey)) {
                   // Merge arrays
                   String[] existing = params.get(lowerKey);
                   String[] merged = new String[existing.length + values.length];
                   System.arraycopy(existing, 0, merged, 0, existing.length);
                   System.arraycopy(values, 0, merged, existing.length, values.length);
                   params.put(lowerKey, merged);
                } else {
                   params.put(lowerKey, values);
                }
            }
        }

        @Override
        public String getParameter(String name) {
            if (name == null) return null;
            String[] values = params.get(name.toLowerCase());
            return (values != null && values.length > 0) ? values[0] : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(params);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(params.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            if (name == null) return null;
            return params.get(name.toLowerCase());
        }
    }
}
