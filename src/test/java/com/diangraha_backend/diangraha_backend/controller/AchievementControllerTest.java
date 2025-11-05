package com.diangraha_backend.diangraha_backend.controller;

import com.diangraha_backend.diangraha_backend.dto.AchievementResponse;
import com.diangraha_backend.diangraha_backend.service.AchievementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AchievementController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {AchievementController.class})
@Import(AchievementService.class)
public class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AchievementService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllAchievements() throws Exception {

        AchievementResponse response1 = new AchievementResponse(
                1L, "Achievement 1", "url1", Instant.now()
        );

        AchievementResponse response2 = new AchievementResponse(
                2L, "Achievement 2", "url2", Instant.now()
        );

        List<AchievementResponse> responses = Arrays.asList(response1, response2);

        when(service.getAllAchievements()).thenReturn(responses);

        mockMvc.perform(get("/api/achievements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Achievement 1"))
                .andExpect(jsonPath("$[1].title").value("Achievement 2"));
    }
}
