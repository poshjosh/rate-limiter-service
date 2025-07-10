# rate limiter service

### Rate limiting as a service (RaaS)

## Usage

Setup

- Post rates of 9 per minute for the login page.
`POST /rates {"id":"login", "rates":[ {"rate":"9/m"} ]}`

Use

- Try to acquire a permit before each access of the login page. 
`GET /permits?id=login`. Returns `true` if the permit was acquired, otherwise `false`.

- Allow access to the login page only if the permit was acquired.

## Service Clients

Rather than concern yourself with the building requests and response,
you could use one of the following client libraries.

- [rate-limiter-java-client](https://github.com/poshjosh/rate-limiter-java-client)

## Endpoints

### /rates

#### POST /rates `{"id":"critical", "rates":[ {"rate":"99/s"} ]}`
#### POST /rates `{"parentId":"critical", "id":"search", "rates":[ {"rate":"9/s"} ], "when":"web.request.user.role = GUEST"}`
#### POST /rates `{"parentId":"critical", "id":"login", "rates":[ {"rate":"9/m"} ]}` 
#### POST /rates/tree
```json
{
    "id": "critical",
    "rates":[ {"rate":"99/s"} ],
    "search": {
        "rates":[ {"rate":"9/s"} ],
        "when":"web.request.user.role = GUEST"  
    },
    "login": {
        "rates":[ {"rate":"9/m"} ]
    }
}
```
#### GET /rates?id=search
Returns the rates for the specified id.

### /permits

#### GET /permits?id=search
Return `true` if the identified limit has permits available, otherwise `false`.

### PUT /permits?id=search
- Increase the used permits for the identified limit.
- Return `true` if we are still within limit after the increase, otherwise return `false`.

## Notes

- `parentId` is optional.
- Rates of a parent will apply to each of the parent's children, in addition to each child's rates.
- `web.request.user.role = GUEST` - The endpoint is rate limited if the user role is GUEST. For more 
on rate conditions see [Rate Condition Expressions](https://github.com/poshjosh/rate-limiter-web-core/blob/master/docs/RATE-CONDITION-EXPRESSION-LANGUAGE.md).

## Authentication

You could enable basic authentication by simply setting the following properties:

- `security.user.name`
- `security.user.password`

:warning: Basic authentication is not recommended because is increases request time significantly
(about 3 times. E.g. from 35ms to 105ms). Rate limiting should not slow down the actual
processing of requests.

## Supported Environment Variables

See [Supported Environment Variables](./docs/environment.md)

## Docker Compose support

This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined:

* redis: [`redis:latest`](https://hub.docker.com/_/redis)

## Local Development

- To make life easier, use the scripts in the `shell` directory to: build, run, deploy etc.

## Testing

We use LocalStack and Testcontainers.

* [LocalStack](https://www.localstack.cloud/) is a cloud service emulator that enables local 
development and testing of AWS services, without the need for connecting to a remote cloud 
provider. During tests, we provision the required S3 bucket inside this emulator.

* [Testcontainers](https://java.testcontainers.org/modules/localstack/) is a library that 
provides lightweight, throwaway instances of Docker containers for integration testing. 
We start our LocalStack container via this library.

An up and running Docker instance is needed to run the LocalStack emulator via Testcontainers.
Ensure this requirement is met when running the test suite either locally or in a CI/CD pipeline.

## Example client

```java
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
```


