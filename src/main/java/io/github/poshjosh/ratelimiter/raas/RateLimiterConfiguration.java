package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.RateLimiterContext;
import io.github.poshjosh.ratelimiter.RateLimiterRegistries;
import io.github.poshjosh.ratelimiter.RateLimiterRegistry;
import io.github.poshjosh.ratelimiter.matcher.Matcher;
import io.github.poshjosh.ratelimiter.matcher.Matchers;
import io.github.poshjosh.ratelimiter.model.RateConfig;
import io.github.poshjosh.ratelimiter.model.RateSource;
import io.github.poshjosh.ratelimiter.raas.redis.RedisBandwidthCache;
import io.github.poshjosh.ratelimiter.raas.redis.RedisRatesCache;
import io.github.poshjosh.ratelimiter.web.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class RateLimiterConfiguration {

    public static final String RATE_ID_HEADER = "X-RATE-ID";

    @Bean
    public RateLimiterRegistry<RequestInfo> rateLimiterRegistry(
            RateLimiterContext<RequestInfo> rateLimiterContext){
        return RateLimiterRegistries.of(rateLimiterContext);
    }

    @Bean
    public RateLimiterContext<RequestInfo> rateLimiterContext(
            RedisBandwidthCache bandwidthCache, RedisRatesCache ratesCache) {
        return RateLimiterContext.<RequestInfo>builder()
                .store(bandwidthCache)
                .rates(ratesCache.getAll())
                .matcherProvider(new MatcherProvider())
                .build();
    }

    private static final class MatcherProvider extends AbstractWebMatcherProvider {
        private MatcherProvider() {
            super(new WebExpressionMatcher());
        }

        @Override
        protected boolean isWebType(RateSource source) {
            return !source.isGroupType()/* && source.isGenericDeclaration()*/;
        }

        @Override
        protected boolean isMatchNone(RateConfig rateConfig, boolean isExpressionPresent) {
            return super.isMatchNone(rateConfig, isExpressionPresent)
                    && !rateConfig.shouldDelegateToParent();
        }

        @Override
        protected Matcher<RequestInfo> createWebRequestMatcher(RateConfig rateConfig) {
            return new AlreadyMatchedRequestMatcher(rateConfig);
        }

        /**
         * Matcher to match http request by (path patterns, request method etc) declared on an element.
         */
        private static final class AlreadyMatchedRequestMatcher implements Matcher<RequestInfo> {
            private final RateConfig rateConfig;
            private AlreadyMatchedRequestMatcher(RateConfig rateConfig) {
                this.rateConfig = Objects.requireNonNull(rateConfig);
            }

            @Override
            public String match(RequestInfo request) {
                final String id = rateConfig.getId();
                return request.getHeaders(RATE_ID_HEADER).contains(id)
                        ? getId() : Matchers.NO_MATCH;
            }

            private String getId() {
                if (rateConfig.shouldDelegateToParent()) {
                    final String parentId = rateConfig.getParent().getId();
                    return parentId.isEmpty() ? rateConfig.getId() : parentId;
                }
                return rateConfig.getId();
            }

            @Override
            public String toString() {
                return "AlreadyMatchedRequestMatcher{" + rateConfig + "}";
            }
        }
    }
}
