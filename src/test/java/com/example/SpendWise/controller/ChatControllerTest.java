package com.example.SpendWise.controller;

import com.example.SpendWise.service.ChatService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // nothing — each test builds its own controller + MockMvc
    }

    @Test
    void postToChat_withLocalStub_returnsDemoReply() throws Exception {
        // Construct ChatService with the default test API key so it uses the local stub path
        ChatService svc = new ChatService("https://example.invalid", "test-api-key", "deployment", "You are SpendWise AI.");
        ChatController controller = new ChatController(svc);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        String body = "{\"message\":\"Hello SpendWise\"}";

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reply", Matchers.containsString("SpendWise AI (local demo)")));
    }

    @Test
    void postToChat_withMockService_returnsServiceReply() throws Exception {
        ChatService svc = Mockito.mock(ChatService.class);
        when(svc.chat(eq("Tell me something"), any())).thenReturn("Assistant: This is a mocked live reply.");

        ChatController controller = new ChatController(svc);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        String body = "{\"message\":\"Tell me something\",\"history\":[]}";

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reply").value("Assistant: This is a mocked live reply."));

        verify(svc).chat(eq("Tell me something"), any());
    }

    @Test
    void postToChat_withEmptyMessage_returnsBadRequest_andDoesNotCallService() throws Exception {
        ChatService svc = Mockito.mock(ChatService.class);

        ChatController controller = new ChatController(svc);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        String body = "{\"message\":\"   \"}"; // blank message

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Message cannot be empty"));

        // Service should not be invoked when controller rejects the request early
        verify(svc, Mockito.never()).chat(any(), any());
    }

    @Test
    void postToChat_whenServiceThrows_returnsInternalServerError() throws Exception {
        ChatService svc = Mockito.mock(ChatService.class);
        when(svc.chat(any(), any())).thenThrow(new RuntimeException("boom"));

        ChatController controller = new ChatController(svc);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        String body = "{\"message\":\"Trigger error\",\"history\":[]}";

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("AI service is currently unavailable. Please try again later."));
    }
}


