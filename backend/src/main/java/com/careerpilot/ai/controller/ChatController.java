package com.careerpilot.ai.controller;

import com.careerpilot.ai.dto.ChatRequest;
import com.careerpilot.ai.entity.ChatHistory;
import com.careerpilot.ai.service.ChatAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ChatController {

    private final ChatAssistantService chatAssistantService;

    @GetMapping("/{analysisId}")
    public ResponseEntity<List<ChatHistory>> getChatHistory(@PathVariable Long analysisId) {
        return ResponseEntity.ok(chatAssistantService.getChatHistory(analysisId));
    }

    @PostMapping("/{analysisId}")
    public ResponseEntity<ChatHistory> sendMessage(@PathVariable Long analysisId,
                                                   @Valid @RequestBody ChatRequest request) {
        log.info("Received chat message for analysis: {}", analysisId);
        try {
            ChatHistory response = chatAssistantService.answerQuestion(analysisId, request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to answer chat question", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
