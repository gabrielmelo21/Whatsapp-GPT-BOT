package com.whatsapp.bot.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ChatGPTService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String gptConnection(String prompt) {
        String openaiApiKey = "sk-proj-eCEG1aBYeTJKzRJGBLRpT3BlbkFJ2qOQGkQrSQmZ4w5iEXX2";
        String openaiEndpoint = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openaiApiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", "You are a chatbot."));
        messages.add(createMessage("user", prompt));

        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("messages", messages);
        requestBodyMap.put("model", "gpt-3.5-turbo");

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBodyMap, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(openaiEndpoint, HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String responseBody = responseEntity.getBody();

            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode choices = jsonNode.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    String content = choices.get(0).path("message").path("content").asText();
                    return content;
                } else {
                    // Trate o caso em que a resposta não tem o formato esperado
                    return "Resposta inválida da API OpenAI.";
                }
            } catch (Exception e) {
                // Logar a exceção ou fazer algo apropriado
                e.printStackTrace();
                return "Erro ao processar a resposta da API OpenAI.";
            }
        } else {
            System.out.println("Erro na chamada da API OpenAI. Código de status: " + responseEntity.getStatusCodeValue());
            return "Error";
        }
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
}
