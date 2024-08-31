package io.github.poshjosh.ratelimiter.raas.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestDto {
    public static final HttpRequestDto NOOP = HttpRequestDto.builder()
            .contextPath("").method("").requestUri("").servletPath("").build();
    private String authType;
    private String characterEncoding;
    @NotNull(message = "required.contextPath")
    private String contextPath;
    private Map<String, String> cookies;
    private Map<String, List<String>> headers;
    private Map<String, String> attributes;
    @NotBlank(message = "required.method")
    private String method;
    private Map<String, List<String>> parameters;
    private String remoteAddr;
    private List<String> locales;
    private List<String> userRoles;
    private String userPrincipal;
    @NotBlank(message = "required.requestUri")
    private String requestUri;
    @NotNull(message = "required.servletPath")
    private String servletPath;
    private String sessionId;

    @Override
    public String toString() {
        return "HttpRequestDto{" + method + ' ' + requestUri + "?sessionId=" + sessionId +
                ", headers=" + headers + ", cookies=" + cookies + ", locales" + locales + "}";
    }
}
