FROM openjdk:17-alpine

LABEL io.github.poshosh.image.authors="https://github.com/poshjosh"

WORKDIR /raas
#
#RUN addgroup -S spring && adduser -S spring -G spring
#USER spring:spring

COPY . .

RUN mkdir -p /app/lib

ARG APP_VERSION
ARG TIMEZONE=UTC

# We do this so all containers can share the same timezone,
# We need tzdata because alpine linux does not come with it, by default.
RUN apk add --no-cache tzdata \
    && ln -snf /usr/share/zoneinfo/"$TIMEZONE" /etc/localtime && echo "$TIMEZONE" > /etc/timezone

RUN jar xf target/rate-limiter-service-${APP_VERSION}.jar  \
    && cp -Rf BOOT-INF/lib /app \
    && cp -Rf META-INF /app/META-INF \
    && cp -Rf BOOT-INF/classes /app

RUN chmod +x docker-entrypoint.sh
ENTRYPOINT ["./docker-entrypoint.sh"]