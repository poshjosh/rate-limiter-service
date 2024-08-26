package io.github.poshjosh.ratelimiter.raas.model;

import io.github.poshjosh.ratelimiter.web.core.RequestInfo;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.*;

@Component
public class HttpRequestMapper {
    public static final HttpRequestDto NOOP_REQUEST_DTO = HttpRequestDto.builder()
            .contextPath("").method("").requestUri("").servletPath("")
            .build();
    public RequestInfo toRequestInfo(HttpRequestDto dto) {
        return new HttpRequestInfo(dto);
    }

    private static final class HttpRequestInfo implements RequestInfo {
        private final HttpRequestDto httpRequestDto;
        private HttpRequestInfo(HttpRequestDto httpRequestDto) {
            this.httpRequestDto = Objects.requireNonNull(httpRequestDto);
        }

        @Override public String getAuthScheme(String resultIfNone) {
            return httpRequestDto.getAuthType() == null ? resultIfNone :
                    httpRequestDto.getAuthType();
        }

        @Override public String getCharacterEncoding(String resultIfNone) {
            return httpRequestDto.getCharacterEncoding() == null ? resultIfNone :
                    httpRequestDto.getCharacterEncoding();
        }

        @Override public String getContextPath() {
            return httpRequestDto.getContextPath();
        }

        @Override public List<Cookie> getCookies() {
            return httpRequestDto.getCookies() == null ? null :
                    httpRequestDto.getCookies().entrySet().stream()
                    .map(e -> Cookie.of(e.getKey(), e.getValue())).toList();
        }

        @Override public List<String> getHeaders(String name) {
            Map<String, List<String>> headers = httpRequestDto.getHeaders();
            if (headers == null) {
                return null;
            }
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) { // case-insensitive
                    return entry.getValue();
                }
            }
            return null;
        }

        @Override public Object getAttribute(String name, Object resultIfNone) {
            if (httpRequestDto.getAttributes() == null) {
                return resultIfNone;
            }
            final String attribute = httpRequestDto.getAttributes().get(name);
            return attribute == null ? resultIfNone : attribute;
        }

        @Override public List<String> getParameters(String name) {
            return httpRequestDto.getParameters() == null ? null :
                    httpRequestDto.getParameters().getOrDefault(name, null);
        }

        @Override public String getRemoteAddr(String ifNone) {
            return httpRequestDto.getRemoteAddr() == null ? ifNone : httpRequestDto.getRemoteAddr();
        }

        @Override public List<Locale> getLocales() {
            return httpRequestDto.getLocales() == null ? null :
                    httpRequestDto.getLocales().stream()
                    .map(value -> value.replace('_', '-'))
                    .map(Locale::forLanguageTag).toList();
        }

        @Override public String getMethod() {
            return httpRequestDto.getMethod();
        }

        @Override public String getRequestUri() {
            return httpRequestDto.getRequestUri();
        }

        @Override public String getServletPath() {
            return httpRequestDto.getServletPath();
        }

        @Override public String getSessionId(String ifNone) {
            return httpRequestDto.getSessionId() == null ? ifNone : httpRequestDto.getSessionId();
        }

        @Override public Principal getUserPrincipal(Principal principal) {
            final String sval = httpRequestDto.getUserPrincipal();
            return sval == null ? principal : () -> sval;
        }

        @Override public boolean isUserInRole(String role) {
            return httpRequestDto.getUserRoles() != null
                    && httpRequestDto.getUserRoles().contains(role);
        }
    }
}
