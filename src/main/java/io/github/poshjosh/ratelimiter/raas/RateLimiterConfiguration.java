package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.RateLimiterContext;
import io.github.poshjosh.ratelimiter.RateLimiterRegistries;
import io.github.poshjosh.ratelimiter.RateLimiterRegistry;
import io.github.poshjosh.ratelimiter.expression.ExpressionMatcher;
import io.github.poshjosh.ratelimiter.expression.ExpressionMatchers;
import io.github.poshjosh.ratelimiter.model.RateConfig;
import io.github.poshjosh.ratelimiter.model.Rates;
import io.github.poshjosh.ratelimiter.raas.redis.RedisBandwidthCache;
import io.github.poshjosh.ratelimiter.raas.redis.RedisRatesCache;
import io.github.poshjosh.ratelimiter.util.AbstractMatcherProvider;
import io.github.poshjosh.ratelimiter.util.Matcher;
import io.github.poshjosh.ratelimiter.util.MatcherProvider;
import io.github.poshjosh.ratelimiter.util.Matchers;
import io.github.poshjosh.ratelimiter.web.core.RequestInfo;
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
                .matcherProvider(matcherProvider())
                .build();
    }

    private MatcherProvider<RequestInfo> matcherProvider() {
        return new MatcherProviderImpl(ExpressionMatchers.ofDefaults());
    }

    private static final class MatcherProviderImpl extends AbstractMatcherProvider<RequestInfo> {
        private MatcherProviderImpl(ExpressionMatcher<RequestInfo> expressionMatcher) {
            super(expressionMatcher);
        }
        @Override
        public Matcher<RequestInfo> createMainMatcher(RateConfig rateConfig) {
            final Rates rates = rateConfig.getRates();
            final Matcher<RequestInfo> expressionMatcher =
                    createExpressionMatcher(rates.getCondition()).orElse(null);
            if (isMatchNone(rateConfig, expressionMatcher != null)) {
                return Matchers.matchNone();
            }
            Matcher<RequestInfo> idMatcher = new IdMatcher(rateConfig.getId());
            if (expressionMatcher == null) {
                return idMatcher;
            }
            return idMatcher.and(expressionMatcher);
        }

        @Override
        protected boolean isMatchNone(RateConfig rateConfig, boolean isExpressionPresent) {
            return super.isMatchNone(rateConfig, isExpressionPresent)
                    && !rateConfig.shouldDelegateToParent();
        }

        private record IdMatcher(String id) implements Matcher<RequestInfo> {
            private IdMatcher(String id) {
                        this.id = Objects.requireNonNull(id);
                    }
            @Override public String match(RequestInfo requestInfo) {
                return requestInfo.getHeaders(RATE_ID_HEADER).contains(id) ?
                        id : Matchers.NO_MATCH;
            }
            @Override public String toString() {
                        return "IdMatcher{" + "id='" + id + '\'' + '}';
                    }
        }
    }
}
