package io.github.poshjosh.ratelimiter.raas.resources;

import io.github.poshjosh.ratelimiter.raas.RedisSetup;
import io.github.poshjosh.ratelimiter.raas.model.RateDto;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import io.github.poshjosh.ratelimiter.raas.services.RateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RateSourceTest extends RedisSetup {

    private static final MediaType contentType = MediaType.APPLICATION_JSON;

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
                .getMessage("required.id", null, Locale.getDefault());
        final String expectedRatesError = messageSource
                .getMessage("required.rates", null, Locale.getDefault());
        mockMvc.perform(post(RateResource.PATH).contentType(contentType).content(ratesJson))
                .andDo(print()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(containsString(expectedIdError)))
                .andExpect(jsonPath("$.detail").value(containsString(expectedRatesError)));

    }
}