package com.example.SpendWise.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final OpenAIClient client;
    private final String deploymentName;

    private static final String SYSTEM_PROMPT =
            "You are SpendWise AI, a friendly and knowledgeable personal finance assistant built into the SpendWise app. " +
            "You help users track expenses, manage budgets, set financial goals, and understand their spending patterns. " +
            "You can give advice on saving money, reducing unnecessary expenses, planning budgets, and reaching financial goals. " +
            "Keep your responses concise, clear, and actionable. Use bullet points when listing multiple items. " +
            "If a user asks about something unrelated to personal finance, gently redirect them back to financial topics.";

    public ChatService(
            @Value("${azure.openai.endpoint}") String endpoint,
            @Value("${azure.openai.api-key}") String apiKey,
            @Value("${azure.openai.deployment-name}") String deploymentName) {

        this.deploymentName = deploymentName;
        this.client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();
    }

    /**
     * Sends a message to Azure OpenAI and returns the assistant reply.
     *
     * @param userMessage the latest user message
     * @param history     previous conversation turns, each map has "role" and "content"
     * @return the assistant's reply text
     */
    public String chat(String userMessage, List<Map<String, String>> history) {
        List<ChatRequestMessage> messages = new ArrayList<>();
        messages.add(new ChatRequestSystemMessage(SYSTEM_PROMPT));

        // Replay conversation history for multi-turn context
        if (history != null) {
            for (Map<String, String> turn : history) {
                String role = turn.get("role");
                String content = turn.get("content");
                if ("user".equals(role)) {
                    messages.add(new ChatRequestUserMessage(content));
                } else if ("assistant".equals(role)) {
                    messages.add(new ChatRequestAssistantMessage(content));
                }
            }
        }

        messages.add(new ChatRequestUserMessage(userMessage));

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);

        ChatCompletions completions = client.getChatCompletions(deploymentName, options);
        ChatChoice choice = completions.getChoices().get(0);
        return choice.getMessage().getContent();
    }
}

