package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.ChatRequestDto;
import com.cart.ecom_proj.dto.ChatResponseDto;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.ChatMessage;
import com.cart.ecom_proj.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
@Slf4j
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestBody ChatRequestDto request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        if (request == null || request.message() == null || request.message().trim().isBlank()) {
            return new ResponseEntity<>("Message is required", HttpStatus.BAD_REQUEST);
        }

        try {
            ChatResponseDto response = chatService.getAiResponse(request.message().trim(), currentUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error in chat controller", ex);
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/chat/history")
    public ResponseEntity<?> getHistory(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        try {
            List<ChatMessage> history = chatService.getHistory(currentUser);
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error fetching chat history", ex);
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/chat/history")
    public ResponseEntity<?> clearHistory(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        try {
            chatService.clearHistory(currentUser);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception ex) {
            log.error("Error clearing chat history", ex);
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
