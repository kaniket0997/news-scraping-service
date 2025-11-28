package com.example.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LLMService {

  private final WebClient webClient;
  private final String apiKey;
  private final String model;
  private final ObjectMapper mapper = new ObjectMapper();

  public LLMService(
      @Value("${llm.openai.apiUrl}") String apiUrl,
      @Value("${OPENAI_API_KEY:}") String apiKey,
      @Value("${llm.openai.model:gpt-4o-mini}") String model) {
    this.webClient = WebClient.builder().baseUrl(apiUrl).build();
    this.apiKey =
        System.getenv("OPENAI_API_KEY") != null ? System.getenv("OPENAI_API_KEY") : apiKey;
    this.model = model;
  }

  public String summarizeArticleSync(String title, String description) {
    if (apiKey == null || apiKey.isBlank()) return ""; // no API key -> skip
    Map<String, Object> request =
        Map.of(
            "model",
            model,
            "messages",
            List.of(
                Map.of("role", "system", "content", "You are a brief summarizer."),
                Map.of(
                    "role",
                    "user",
                    "content",
                    "Summarize the following news article in 2-3 sentences:\n\nTitle: "
                        + title
                        + "\n\n"
                        + description)),
            "max_tokens",
            120);

    Map response =
        webClient
            .post()
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    try {
      List choices = (List) response.get("choices");
      Map first = (Map) choices.get(0);
      Map msg = (Map) first.get("message");
      return (String) msg.get("content");
    } catch (Exception e) {
      return "";
    }
  }

  public Map<String, Object> extractEntitiesAndIntent(String userQuery) {
    if (apiKey == null || apiKey.isBlank())
      return Map.of("entities", List.of(), "intent", "search");
    Map<String, Object> request =
        Map.of(
            "model",
            model,
            "messages",
            List.of(
                Map.of(
                    "role",
                    "system",
                    "content",
                    "Extract entities and intent from the user query and return a JSON object like {\"entities\": [..], \"intent\": \"category|nearby|source|search|score\", \"extras\": {...}}"),
                Map.of("role", "user", "content", userQuery)),
            "max_tokens",
            200);

    Map response =
        webClient
            .post()
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    try {
      List choices = (List) response.get("choices");
      Map first = (Map) choices.get(0);
      Map msg = (Map) first.get("message");
      String content = (String) msg.get("content");
      return mapper.readValue(content, Map.class);
    } catch (Exception e) {
      return Map.of("entities", List.of(), "intent", "search");
    }
  }
}
