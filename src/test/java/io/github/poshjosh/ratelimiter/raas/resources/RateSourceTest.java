package io.github.poshjosh.ratelimiter.raas.resources;

import io.github.poshjosh.ratelimiter.raas.cache.RedisInitializer;
import io.github.poshjosh.ratelimiter.raas.model.RateDto;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import io.github.poshjosh.ratelimiter.raas.persistence.InitializeS3Bucket;
import io.github.poshjosh.ratelimiter.raas.services.RateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@InitializeS3Bucket
@AutoConfigureMockMvc
class RateSourceTest implements RedisInitializer {

    private static final MediaType contentType = MediaType.APPLICATION_JSON;
    private static final String idError = "required.id";
    private static final String ratesError = "required.rates";

    @Autowired private MockMvc mockMvc;
    @Autowired private MessageSource messageSource;
    @MockBean private RateService rateService;

    @Test
    void shouldPostValidRates() throws Exception {
        final String rateId = this.getClass().getSimpleName();
        final RateDto rate = RateDto.builder().rate("1/s").build();
        final RatesDto rates = RatesDto.builder().id(rateId).rates(List.of(rate)).build();
        final String ratesJson = "{\"id\":\"" + rateId + "\",\"rates\":[{\"rate\":\"1/s\"}]}";

        when(rateService.addRates(rates)).thenReturn(rates);

        mockMvc.perform(post(RateResource.PATH).contentType(contentType).content(ratesJson))
                .andDo(print()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(rateId));
    }

    @Test
    void shouldNotPostInvalidRates() throws Exception {
        final String ratesJson = "{\"id\":\"\",\"rates\":[]}";
        final String expectedIdError = messageSource
                .getMessage(idError, null, Locale.getDefault());
        final String expectedRatesError = messageSource
                .getMessage(ratesError, null, Locale.getDefault());
        mockMvc.perform(post(RateResource.PATH).contentType(contentType).content(ratesJson))
                .andDo(print()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(containsString(expectedIdError)))
                .andExpect(jsonPath("$.detail").value(containsString(expectedRatesError)));

    }

    @Test
    void shouldPostValidRatesTree() throws Exception {
        final String rateId = this.getClass().getSimpleName();
        final Map<String, Object> rate = Map.of("rate", "1/s");
        final Map<String, Object> rates = Map.of("id", rateId, "rates", List.of(rate));
        final String ratesJson = "{\"id\":\"" + rateId + "\",\"rates\":[{\"rate\":\"1/s\"}]}";

        when(rateService.addRateTree(rates)).thenReturn(Collections.emptyList());

        mockMvc.perform(post(RateResource.PATH+"/tree").contentType(contentType).content(ratesJson))
                .andDo(print()).andExpect(status().isCreated());
    }
}