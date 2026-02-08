package com.agarg.securecollab.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.agarg.securecollab.chatservice.service.OAuthTokenService;

/**
 * Jira Integration Service - Creates issues, transitions, and links via REST API
 * Uses encrypted OAuth tokens for authentication
 */
@Service
public class JiraIntegrationService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final OAuthTokenService oauthTokenService;

  @Value("${jira.api.base-url:https://your-jira.atlassian.net/rest/api/3}")
  private String jiraBaseUrl;

  public JiraIntegrationService(RestTemplate restTemplate, ObjectMapper objectMapper, 
                                OAuthTokenService oauthTokenService) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.oauthTokenService = oauthTokenService;
  }

  /**
   * Create a new Jira issue from a chat message
   * Example: `/api/integration/jira/create-issue` → POST body: { projectKey, issueType, summary, description, userId }
   */
  public CompletableFuture<String> createIssue(String userId, String projectKey, String issueType, 
                                               String summary, String description) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "jira");
        
        Map<String, Object> issue = new LinkedHashMap<>();
        
        // Fields
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("project", Map.of("key", projectKey));
        fields.put("issuetype", Map.of("name", issueType));
        fields.put("summary", summary);
        fields.put("description", Map.of("version", 1, "type", "doc", 
                                          "content", Arrays.asList(
                                            Map.of("type", "paragraph", 
                                                   "content", Arrays.asList(Map.of("type", "text", "text", description))))));
        
        issue.put("fields", fields);
        
        String requestBody = objectMapper.writeValueAsString(issue);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // POST to /issue
        String response = restTemplate.postForObject(jiraBaseUrl + "/issue", requestBody, String.class);
        
        // Extract issue key from response
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return (String) responseMap.get("key");
        
      } catch (Exception e) {
        throw new RuntimeException("Jira issue creation failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Transition a Jira issue to a new status
   * Example: ISSUE-123 → "In Progress"
   */
  public CompletableFuture<Void> transitionIssue(String userId, String issueKey, String targetStatus) {
    return CompletableFuture.runAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "jira");
        
        // Get available transitions
        String transitionsUrl = jiraBaseUrl + "/issue/" + issueKey + "/transitions";
        Map<String, Object> transitionsResponse = restTemplate.getForObject(transitionsUrl, Map.class);
        
        List<Map<String, Object>> transitions = (List) transitionsResponse.get("transitions");
        String transitionId = transitions.stream()
          .filter(t -> targetStatus.equalsIgnoreCase((String) ((Map<String, Object>) t.get("to")).get("name")))
          .map(t -> (String) t.get("id"))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Status '" + targetStatus + "' not found"));
        
        // POST transition
        Map<String, Object> transitionPayload = Map.of("transition", Map.of("id", transitionId));
        String payload = objectMapper.writeValueAsString(transitionPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        restTemplate.postForObject(transitionsUrl, payload, String.class);
        
      } catch (Exception e) {
        throw new RuntimeException("Jira transition failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Link two Jira issues
   * Example: Link ISSUE-123 to ISSUE-456 with "relates to" relationship
   */
  public CompletableFuture<Void> linkIssues(String userId, String issueKey1, String issueKey2, String linkType) {
    return CompletableFuture.runAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "jira");
        
        Map<String, Object> linkPayload = new LinkedHashMap<>();
        linkPayload.put("type", Map.of("name", linkType));
        linkPayload.put("inwardIssue", Map.of("key", issueKey1));
        linkPayload.put("outwardIssue", Map.of("key", issueKey2));
        
        String payload = objectMapper.writeValueAsString(linkPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        restTemplate.postForObject(jiraBaseUrl + "/issueLink", payload, String.class);
        
      } catch (Exception e) {
        throw new RuntimeException("Jira linking failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Add a comment to a Jira issue
   */
  public CompletableFuture<Void> addComment(String userId, String issueKey, String comment) {
    return CompletableFuture.runAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "jira");
        
        Map<String, Object> commentPayload = Map.of("body", 
          Map.of("version", 1, "type", "doc", 
                 "content", Arrays.asList(
                   Map.of("type", "paragraph", 
                          "content", Arrays.asList(Map.of("type", "text", "text", comment))))));
        
        String payload = objectMapper.writeValueAsString(commentPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        restTemplate.postForObject(jiraBaseUrl + "/issue/" + issueKey + "/comments", payload, String.class);
        
      } catch (Exception e) {
        throw new RuntimeException("Jira comment failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Search for Jira issues using JQL
   */
  public CompletableFuture<List<Map<String, Object>>> searchIssues(String userId, String jql) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "jira");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        
        String searchUrl = jiraBaseUrl + "/search?jql=" + java.net.URLEncoder.encode(jql, "UTF-8");
        Map<String, Object> response = restTemplate.getForObject(searchUrl, Map.class);
        
        return (List<Map<String, Object>>) response.get("issues");
        
      } catch (Exception e) {
        throw new RuntimeException("Jira search failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Webhook receiver for Jira events (issue created, updated, deleted)
   * Publishes to Kafka: integrations.events topic
   */
  public void handleJiraWebhook(Map<String, Object> payload) {
    String event = (String) payload.get("webhookEvent");
    Map<String, Object> issue = (Map<String, Object>) payload.get("issue");
    
    if (issue == null) return;
    
    Map<String, Object> integrationEvent = Map.of(
      "source", "jira",
      "event", event,
      "issueKey", ((Map<String, Object>) issue.get("key")).get("key"),
      "summary", ((Map<String, Object>) issue.get("fields")).get("summary"),
      "status", ((Map<String, Object>) ((Map<String, Object>) issue.get("fields")).get("status")).get("name"),
      "timestamp", System.currentTimeMillis()
    );
    
    // This would be published to Kafka by EventPublisher
    System.out.println("Jira webhook received: " + integrationEvent);
  }
}
