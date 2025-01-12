package io.github.poshjosh.ratelimiter.raas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.poshjosh.ratelimiter.raas.model.*;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

public class RateLimiterClient {

    @Getter
    public static class ServerException extends Exception {
        private final transient HttpResponse<?> httpResponse;
        public ServerException(HttpResponse<?> httpResponse) {
            super("Server returned an error response: " + httpResponse.statusCode() +
                    " with body:\n" + httpResponse.body());
            this.httpResponse = Objects.requireNonNull(httpResponse);
        }
    }

    private final String serverBaseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RateLimiterClient(String serverBaseUrl) {
        this(serverBaseUrl, new ObjectMapper().findAndRegisterModules());
    }

    public RateLimiterClient(String serverBaseUrl, ObjectMapper objectMapper) {
        this.serverBaseUrl = Objects.requireNonNull(serverBaseUrl);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Add the specified rates/limits (if not already added) then try to acquire 1 permit.
     * @param request The HttpServletRequest to acquire permits for.
     * @param rateId The id of the rate/limit to acquire permits from.
     * @param rate The rate at which permits may be acquired.
     *             E.g. "1/s", "10/m", "100/h".
     * @param condition The condition to acquire permits under.
     *                  E.g. "web.session.id!= & jvm.memory.available<1GB".
     * @return True if permits are available, false otherwise.
     * @throws IOException If there was an error communicating with the server.
     * @throws InterruptedException If the operation was interrupted.
     * @throws ServerException If the server returned an error response.
     * @see #isWithinLimit(LimitDto)
     */
    public boolean isWithinLimit(
            /* Nullable */ HttpServletRequest request, String rateId,
            String rate, /* Nullable */ String condition)
            throws IOException, InterruptedException, ServerException {
        LimitDto limitDto = buildLimit(HttpRequestDtos.of(request), rateId, rate, condition);
        return isWithinLimit(limitDto);
    }

    /**
     * Add the specified rates/limits (if not already added) then try to acquire 1 permit.
     * @param limitDto An object encapsulating request data, to add rates and acquire permits for.
     * @return True if permits are available, false otherwise.
     * @throws IOException If there was an error communicating with the server.
     * @throws InterruptedException If the operation was interrupted.
     * @throws ServerException If the server returned an error response.
     */
    public boolean isWithinLimit(LimitDto limitDto)
            throws IOException, InterruptedException, ServerException {
        HttpRequest request = request("/permits/limit").POST(requestBody(limitDto)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (isSuccessful(response)) {
            return true;
        }
        if (response.statusCode() == 429) {
            return false;
        }
        throw new ServerException(response);
    }

    public RatesDto getLimit(String id) throws IOException, InterruptedException, ServerException {
        return send(request("/rates/" + id).GET().build(), RatesDto.class);
    }

    public RatesDto postLimit(RatesDto ratesDto) throws IOException, InterruptedException, ServerException {
        return send(request("/rates").POST(requestBody(ratesDto)).build(), RatesDto.class);
    }

    public void deleteLimit(String id) throws IOException, InterruptedException, ServerException {
        send(request("/rates/" + id).DELETE().build(), null);
    }

    private LimitDto buildLimit(
            /* Nullable */ HttpRequestDto request, String rateId,
            String rate, /* Nullable */ String condition) {
        RateDto rateDto = RateDto.builder().rate(rate).when(condition).build();
        RatesDto ratesDto = RatesDto.builder().id(rateId).rates(List.of(rateDto)).build();
        return LimitDto.builder().limit(ratesDto).request(request).build();
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(serverBaseUrl + path))
                .header("Content-Type", "application/json");
    }

    private <T> T send(HttpRequest request, /* Nullable */ Class<T> responseType)
            throws IOException, InterruptedException, ServerException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (isSuccessful(response)) {
            if (responseType == null) {
                return null;
            }
            String body = response.body();
            return body == null ? null : objectMapper.readValue(body, responseType);
        }
        throw new ServerException(response);
    }

    private HttpRequest.BodyPublisher requestBody(Object body) throws IOException {
        return body == null ? HttpRequest.BodyPublishers.noBody() :
                HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
    }

    private boolean isSuccessful(HttpResponse<?> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    public static void main(String [] args)
            throws IOException, InterruptedException, ServerException {

        final RateLimiterClient client = new RateLimiterClient("http://localhost:8080");
        final int numberOfRequests = 3;
        final String limitId = "test-limit";
        final String limit = "2/m";
        final String condition = "jvm.memory.available < 100gb";

        // The third request (which exceeds the above limit of 2) will fail.
        for (int i = 0; i < numberOfRequests; i++) {
            boolean withinLimit = client.isWithinLimit(null, limitId, limit, condition);
            System.out.println("Request " + i + " is within limit: " + withinLimit);
        }
    }
}
