package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.RateLimiterContext;
import io.github.poshjosh.ratelimiter.RateLimiterRegistries;
import io.github.poshjosh.ratelimiter.RateLimiterRegistry;
import io.github.poshjosh.ratelimiter.expression.*;
import io.github.poshjosh.ratelimiter.matcher.Matcher;
import io.github.poshjosh.ratelimiter.matcher.Matchers;
import io.github.poshjosh.ratelimiter.model.RateConfig;
import io.github.poshjosh.ratelimiter.model.RateSource;
import io.github.poshjosh.ratelimiter.model.Rates;
import io.github.poshjosh.ratelimiter.raas.redis.RedisBandwidthCache;
import io.github.poshjosh.ratelimiter.raas.redis.RedisRatesCache;
import io.github.poshjosh.ratelimiter.util.*;
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
                .matcherProvider(new MatcherProviderX())
                .build();
    }

    private static final class MatcherProviderX extends AbstractMatcherProvider<RequestInfo> {
        private MatcherProviderX() {
            super(ExpressionMatchers.any(new WebExpressionMatcher(), ExpressionMatchers.ofDefaults()));
        }
        @Override
        public Matcher<RequestInfo> createMainMatcher(RateConfig rateConfig) {
            final Rates rates = rateConfig.getRates();
            final RateSource source = rateConfig.getSource();
            final Matcher<RequestInfo> expressionMatcher =
                    createExpressionMatcher(rates.getCondition()).orElse(null);
            if (isMatchNone(rateConfig, expressionMatcher != null)) {
                return Matchers.matchNone();
            }
            if (!source.isGroupType()/* && source.isGenericDeclaration()*/) {
                Matcher<RequestInfo> webRequestMatcher = createWebRequestMatcher(rateConfig);
                if (expressionMatcher == null) {
                    return webRequestMatcher;
                }
                return webRequestMatcher.and(expressionMatcher);
            }
            return expressionMatcher == null ? Matchers.matchNone() : expressionMatcher;
        }

        @Override
        protected boolean isMatchNone(RateConfig rateConfig, boolean isExpressionPresent) {
            return super.isMatchNone(rateConfig, isExpressionPresent)
                    && !rateConfig.shouldDelegateToParent();
        }

        private Matcher<RequestInfo> createWebRequestMatcher(RateConfig rateConfig) {
            return new AlreadyMatchedRequestMatcher(rateConfig);
        }

        /**
         * Matcher to match http request by (path patterns, request method etc) declared on an element.
         */
        private static class AlreadyMatchedRequestMatcher implements Matcher<RequestInfo> {
            private final RateConfig rateConfig;
            private AlreadyMatchedRequestMatcher(RateConfig rateConfig) {
                this.rateConfig = Objects.requireNonNull(rateConfig);
            }

            @Override
            public String match(RequestInfo request) {
                final String id = rateConfig.getId();
                return request.getHeaders(RATE_ID_HEADER).contains(id) ? getId(request) : Matchers.NO_MATCH;
            }

            private String getId(RequestInfo request) {
                if (rateConfig.shouldDelegateToParent()) {
                    final String parentId = rateConfig.getParent().getId();
                    return parentId.isEmpty() ? buildId(request) : parentId;
                }
                return buildId(request);
            }

            private String buildId(RequestInfo request) {
                final String path = request.getServletPath();
                final String method = request.getMethod();
                return path.isEmpty() ? method : method + path;
            }

            @Override
            public String toString() {
                return "AlreadyMatchedRequestMatcher{" + rateConfig + "}";
            }
        }
    }

    private static final class MatcherProviderImpl extends AbstractMatcherProvider<RequestInfo> {
        private MatcherProviderImpl() {
            super(ExpressionMatchers.any(new WebExpressionMatcher(), ExpressionMatchers.ofDefaults()));
        }
        @Override
        public Matcher<RequestInfo> createMainMatcher(RateConfig rateConfig) {
            final Rates rates = rateConfig.getRates();
            final Matcher<RequestInfo> expressionMatcher =
                    createExpressionMatcher(rates.getCondition()).orElse(null);
            if (isMatchNone(rateConfig, expressionMatcher != null)) {
                return Matchers.matchNone();
            }
//            final Matcher<RequestInfo> idMatcher = new RatesIdMatcher(rateConfig.getId());
//            if (expressionMatcher == null) {
//                return idMatcher;
//            }
//            return idMatcher.and(expressionMatcher);
            return andSourceMatcher(expressionMatcher, rateConfig);
        }

        @Override
        protected boolean isMatchNone(RateConfig rateConfig, boolean isExpressionPresent) {
            return super.isMatchNone(rateConfig, isExpressionPresent)
                    && !rateConfig.shouldDelegateToParent();
        }

        private record RatesIdMatcher(String id) implements Matcher<RequestInfo> {
            private RatesIdMatcher(String id) {
                        this.id = Objects.requireNonNull(id);
                    }
            @Override public String match(RequestInfo requestInfo) {
                return requestInfo.getHeaders(RATE_ID_HEADER).contains(id) ? id : Matchers.NO_MATCH;
            }
            @Override public String toString() { return "RatesIdMatcher{id='" + id + '\'' + '}'; }
        }
    }
}
