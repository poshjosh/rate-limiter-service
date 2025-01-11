package io.github.poshjosh.ratelimiter.raas.resources;

import io.github.poshjosh.ratelimiter.raas.cache.RedisInitializer;
import io.github.poshjosh.ratelimiter.raas.model.RateDto;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import io.github.poshjosh.ratelimiter.raas.persistence.InitializeS3Bucket;
import io.github.poshjosh.ratelimiter.raas.services.PermitService;
import io.github.poshjosh.ratelimiter.raas.services.RateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@InitializeS3Bucket
@AutoConfigureMockMvc
class PermitResourceTest implements RedisInitializer {

    private static final MediaType contentType = MediaType.APPLICATION_JSON;

    @Autowired private MockMvc mockMvc;
    @MockBean private PermitService permitService;
    @MockBean private RateService rateService;

    @Test
    void shouldAcquirePermitsWhenAvailable() throws Exception {
        final String rateId = this.getClass().getSimpleName();
        final RateDto rate = RateDto.builder().rate("100/s").build();
        final RatesDto rates = RatesDto.builder().id(rateId).rates(List.of(rate)).build();
        final String ratesJson = "{\"id\":\"" + rateId + "\",\"rates\":[{\"rate\":\"100/s\"}]}";

        when(rateService.addRates(rates)).thenReturn(rates);
        when(permitService.tryAcquire(anyString(), anyInt(), isNull())).thenReturn(true);

        mockMvc.perform(post(RateResource.PATH).contentType(contentType).content(ratesJson))
                .andDo(print()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(rateId));

        mockMvc.perform(get(PermitResource.PATH + "/acquire")
                        .contentType(contentType)
                        .param("rateId", rateId)
                    )
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldAddRatesAndAcquirePermits() throws Exception {
        final String rateId = this.getClass().getSimpleName();
        final RateDto rate = RateDto.builder().rate("100/s").build();
        final RatesDto rates = RatesDto.builder().id(rateId).rates(List.of(rate)).build();
        final String ratesJson = "{\"id\":\"" + rateId + "\",\"rates\":[{\"rate\":\"100/s\"}]}";

        when(rateService.addRates(rates)).thenReturn(rates);
        when(permitService.tryAcquire(anyString(), anyInt(), isNull())).thenReturn(true);

        mockMvc.perform(post(PermitResource.PATH + "/limit")
                        .contentType(contentType).content("{\"limit\":" + ratesJson + "}")
                        .param("rateId", rateId)
                )
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}