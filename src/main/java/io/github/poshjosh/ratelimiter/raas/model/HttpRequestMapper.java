package io.github.poshjosh.ratelimiter.raas.model;

import io.github.poshjosh.ratelimiter.web.core.RequestInfo;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class HttpRequestMapper {
    public static final HttpRequestDto NOOP_REQUEST_DTO = HttpRequestDto.builder()
            .contextPath("").method("").requestUri("").servletPath("")
            .build();
    public RequestInfo toRequestInfo(HttpRequestDto dto) {
        return new RequestInfoImpl(dto);
    }

    private static final class RequestInfoImpl implements RequestInfo {
        private final HttpRequestDto httpRequestDto;
        private RequestInfoImpl(HttpRequestDto httpRequestDto) {
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
            return httpRequestDto.getCookies() == null ? Collections.emptyList() :
                    httpRequestDto.getCookies().entrySet().stream()
                    .map(e -> Cookie.of(e.getKey(), e.getValue())).collect(Collectors.toList());
        }

        @Override public List<String> getHeaders(String name) {
            return httpRequestDto.getHeaders() == null ? Collections.emptyList() :
                    httpRequestDto.getHeaders().getOrDefault(name, Collections.emptyList());
        }

        @Override public Object getAttribute(String name, Object resultIfNone) {
            return httpRequestDto.getAttributes() == null ? resultIfNone :
                    httpRequestDto.getAttributes().getOrDefault(name, resultIfNone);
        }

        @Override public List<String> getParameters(String name) {
            return httpRequestDto.getParameters() == null ? Collections.emptyList() :
                    httpRequestDto.getParameters().getOrDefault(name, Collections.emptyList());
        }

        @Override public String getRemoteAddr(String ifNone) {
            return httpRequestDto.getRemoteAddr() == null ? ifNone : httpRequestDto.getRemoteAddr();
        }

        @Override public List<Locale> getLocales() {
            return httpRequestDto.getLocales() == null ? Collections.emptyList() :
                    httpRequestDto.getLocales().stream()
                    .map(value -> value.replace('_', '-'))
                    .map(Locale::forLanguageTag).collect(Collectors.toList());
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
