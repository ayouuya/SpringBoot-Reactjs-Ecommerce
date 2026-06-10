package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.ChatRequestDto;
import com.cart.ecom_proj.dto.ChatResponseDto;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.ChatMessage;
import com.cart.ecom_proj.model.UserRole;
import com.cart.ecom_proj.service.ChatService;
import com.cart.ecom_proj.service.UserAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserAccessService userAccessService;

    public ChatController(ChatService chatService, UserAccessService userAccessService) {
        this.chatService = chatService;
        this.userAccessService = userAccessService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestBody ChatRequestDto request,
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        AppUser user;
        try {
            user = userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        if (request == null || request.message() == null || request.message().trim().isBlank()) {
            return new ResponseEntity<>("Message is required", HttpStatus.BAD_REQUEST);
        }

        try {
            ChatResponseDto response = chatService.getAiResponse(request.message().trim(), user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error in chat controller", ex);
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/chat/history")
    public ResponseEntity<?> getHistory(
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        AppUser user;
        try {
            user = userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        try {
            List<ChatMessage> history = chatService.getHistory(user);
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error fetching chat history", ex);
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/chat/history")
    public ResponseEntity<?> clearHistory(
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        AppUser user;
        try {
            user = userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        try {
            chatService.clearHistory(user);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception ex) {
            log.error("Error clearing chat history", ex);
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
