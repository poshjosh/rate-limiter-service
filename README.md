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

## Docker Compose support

This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined:

* redis: [`redis:latest`](https://hub.docker.com/_/redis)

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






