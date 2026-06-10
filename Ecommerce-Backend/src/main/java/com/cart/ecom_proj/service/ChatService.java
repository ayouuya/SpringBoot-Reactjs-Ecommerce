package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.*;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.ChatMessage;
import com.cart.ecom_proj.repo.ChatMessageRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatService {

    private final WebClient webClient;
    private final ChatMessageRepo chatMessageRepo;

    @Value("${openrouter.model}")
    private String model;

    public ChatService(WebClient webClient, ChatMessageRepo chatMessageRepo) {
        this.webClient = webClient;
        this.chatMessageRepo = chatMessageRepo;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getHistory(AppUser user) {
        return chatMessageRepo.findByUserOrderByTimestampAsc(user);
    }

    @Transactional
    public void clearHistory(AppUser user) {
        chatMessageRepo.deleteByUser(user);
    }

    @Transactional
    public ChatResponseDto getAiResponse(String userMessage, AppUser user) {
        log.info("Processing chat message for user: {}", user.getEmail());

        // 1. Sauvegarder le message de l'utilisateur
        ChatMessage userChat = new ChatMessage();
        userChat.setUser(user);
        userChat.setRole("user");
        userChat.setContent(userMessage);
        userChat.setTimestamp(LocalDateTime.now());
        chatMessageRepo.save(userChat);

        // 2. Charger l'historique complet pour envoyer au modèle
        List<ChatMessage> history = chatMessageRepo.findByUserOrderByTimestampAsc(user);

        // 3. Préparer les messages pour l'API OpenRouter
        List<OpenRouterMessage> apiMessages = new ArrayList<>();

        // Message système principal
        String systemPrompt = "Vous êtes l'assistant IA de DigiTech, un site e-commerce marocain moderne proposant une large gamme de produits électroniques, informatiques et gadgets de haute qualité.\n" +
                "Consignes importantes pour vos réponses :\n" +
                "1. Répondez de manière polie, chaleureuse, concise et professionnelle.\n" +
                "2. Les délais de livraison standards sont de 2 à 5 jours ouvrables partout au Maroc. La livraison est gratuite pour toute commande supérieure à 500 MAD (sinon, des frais de 40 MAD s'appliquent).\n" +
                "3. Les modes de paiement acceptés sont le paiement par carte bancaire et PayPal.\n" +
                "4. Les retours sont acceptés sous 14 jours après réception du produit, à condition qu'il soit dans son emballage d'origine.\n" +
                "5. Si un utilisateur pose des questions spécifiques sur ses commandes, ou si vous ne pouvez pas répondre à sa demande, invitez-le amicalement à contacter le service client à support@digitech.ma.";

        apiMessages.add(new OpenRouterMessage("system", systemPrompt));

        // Ajouter l'historique (limité aux 20 derniers messages pour éviter de saturer le contexte)
        int historySize = history.size();
        int startIndex = Math.max(0, historySize - 20);
        for (int i = startIndex; i < historySize; i++) {
            ChatMessage msg = history.get(i);
            apiMessages.add(new OpenRouterMessage(msg.getRole(), msg.getContent()));
        }

        // 4. Appeler l'API OpenRouter via WebClient
        String aiAnswer = "Désolé, je rencontre des difficultés techniques actuellement. Veuillez réessayer plus tard.";
        try {
            OpenRouterRequest requestPayload = new OpenRouterRequest(model, apiMessages);

            OpenRouterResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(OpenRouterResponse.class)
                    .block(); // Bloquant car l'API REST de Spring Boot est synchrone

            if (response != null && response.choices() != null && !response.choices().isEmpty()) {
                OpenRouterMessage responseMsg = response.choices().get(0).message();
                if (responseMsg != null && responseMsg.content() != null) {
                    aiAnswer = responseMsg.content();
                }
            }
        } catch (WebClientResponseException ex) {
            log.error("OpenRouter API error response: Status = {}, Body = {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            aiAnswer = "Désolé, l'assistant IA est temporairement indisponible (Erreur de communication avec l'API).";
        } catch (Exception ex) {
            log.error("Unexpected error during OpenRouter API call", ex);
            aiAnswer = "Désolé, je ne parviens pas à traiter votre demande pour le moment. Veuillez réessayer.";
        }

        // 5. Sauvegarder la réponse de l'IA dans l'historique
        ChatMessage aiChat = new ChatMessage();
        aiChat.setUser(user);
        aiChat.setRole("assistant");
        aiChat.setContent(aiAnswer);
        aiChat.setTimestamp(LocalDateTime.now());
        chatMessageRepo.save(aiChat);

        return new ChatResponseDto(aiAnswer);
    }
}
