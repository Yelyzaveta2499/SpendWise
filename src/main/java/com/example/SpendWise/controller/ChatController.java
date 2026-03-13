package com.example.SpendWise.controller;

import com.example.SpendWise.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * POST /api/chat
     * Body: { "message": "...", "history": [ { "role": "user"|"assistant", "content": "..." }, ... ] }
     * Returns: { "reply": "..." }
     */
    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        try {
            String message = (String) body.get("message");
            if (message == null || message.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, String>> history =
                    (List<Map<String, String>>) body.getOrDefault("history", List.of());

            String reply = chatService.chat(message, history);
            return ResponseEntity.ok(Map.of("reply", reply));

        } catch (Exception e) {
            log.error("Azure OpenAI chat error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI service is currently unavailable. Please try again later."));
        }
    }
}

