package io.github.poshjosh.ratelimiter.raas;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.github.poshjosh.ratelimiter.raas.model.HttpRequestDto;
import io.github.poshjosh.ratelimiter.raas.model.RateDto;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import io.github.poshjosh.ratelimiter.raas.resources.PermitResource;
import io.github.poshjosh.ratelimiter.raas.resources.RateResource;
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
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
class HappyPathMockTest extends RedisSetup {

    private static final MediaType contentType = MediaType.APPLICATION_JSON;

    @Autowired private MockMvc mockMvc;
    @MockBean private RateService rateService;
    @MockBean private PermitService permitService;

    @Test
    void test() throws Exception {
        final String rateId = this.getClass().getSimpleName();
        final RateDto rate = RateDto.builder().rate("1/s").build();
        final RatesDto rates = RatesDto.builder().id(rateId).rates(List.of(rate)).build();
        final String ratesJson = "{\"id\":\"" + rateId + "\",\"rates\":[{\"rate\":\"1/s\"}]}";

        when(rateService.addRates(rates)).thenReturn(rates);

        mockMvc.perform(post(RateResource.PATH).contentType(contentType).content(ratesJson))
                .andDo(print()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(rateId));

        when(rateService.findRates(rateId)).thenReturn(Optional.of(rates));
        mockMvc.perform(get(RateResource.PATH + "/" + rateId))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rateId));

        when(permitService.isAvailable(rateId, HttpRequestDto.NOOP)).thenReturn(true);
        mockMvc.perform(patch(PermitResource.PATH + "/available?rateId=" + rateId).contentType(contentType))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("true"));

        when(permitService.tryAcquire(rateId, 1, HttpRequestDto.NOOP)).thenReturn(true);
        mockMvc.perform(patch(PermitResource.PATH + "/acquire?rateId=" + rateId).contentType(contentType))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("true"));

        when(permitService.tryAcquire(rateId, 1, HttpRequestDto.NOOP)).thenReturn(false);
        mockMvc.perform(patch(PermitResource.PATH + "/acquire?rateId=" + rateId).contentType(contentType))
                .andDo(print()).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));

        when(rateService.deleteRates(rateId)).thenReturn(Optional.of(rates));
        mockMvc.perform(delete(RateResource.PATH + "/" + rateId))
                .andDo(print()).andExpect(status().isOk());

        when(rateService.findRates(rateId)).thenReturn(Optional.empty());
        mockMvc.perform(get(RateResource.PATH + "/" + rateId))
                .andDo(print()).andExpect(status().isNotFound());
    }
}