package com.agarg.securecollab.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.agarg.securecollab.chatservice.service.OAuthTokenService;

/**
 * GitHub Integration Service - Creates issues, PRs, adds labels via REST API v3
 * Uses encrypted OAuth tokens for authentication
 */
@Service
public class GitHubIntegrationService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final OAuthTokenService oauthTokenService;

  @Value("${github.api.base-url:https://api.github.com}")
  private String githubBaseUrl;

  public GitHubIntegrationService(RestTemplate restTemplate, ObjectMapper objectMapper, 
                                  OAuthTokenService oauthTokenService) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.oauthTokenService = oauthTokenService;
  }

  /**
   * Create a GitHub issue
   * Example: owner=MyOrg, repo=MyRepo, title="Bug: Chat crash", body="On disconnect..."
   */
  public CompletableFuture<String> createIssue(String userId, String owner, String repo, 
                                               String title, String body, List<String> labels) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "github");
        
        Map<String, Object> issuePayload = new LinkedHashMap<>();
        issuePayload.put("title", title);
        issuePayload.put("body", body);
        if (labels != null && !labels.isEmpty()) {
          issuePayload.put("labels", labels);
        }
        
        String payload = objectMapper.writeValueAsString(issuePayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String url = githubBaseUrl + "/repos/" + owner + "/" + repo + "/issues";
        String response = restTemplate.postForObject(url, payload, String.class);
        
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return "#" + responseMap.get("number");
        
      } catch (Exception e) {
        throw new RuntimeException("GitHub issue creation failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Create a GitHub pull request
   * Example: head=feature/new-auth, base=main
   */
  public CompletableFuture<String> createPullRequest(String userId, String owner, String repo,
                                                     String title, String body, String head, String base) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "github");
        
        Map<String, Object> prPayload = new LinkedHashMap<>();
        prPayload.put("title", title);
        prPayload.put("body", body);
        prPayload.put("head", head);
        prPayload.put("base", base);
        prPayload.put("draft", false);
        
        String payload = objectMapper.writeValueAsString(prPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String url = githubBaseUrl + "/repos/" + owner + "/" + repo + "/pulls";
        String response = restTemplate.postForObject(url, payload, String.class);
        
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return "#" + responseMap.get("number");
        
      } catch (Exception e) {
        throw new RuntimeException("GitHub PR creation failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Add a label to a GitHub issue or PR
   */
  public CompletableFuture<Void> addLabel(String userId, String owner, String repo, int issueNumber, String label) {
    return CompletableFuture.runAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "github");
        
        Map<String, Object> labelPayload = Map.of("labels", Arrays.asList(label));
        String payload = objectMapper.writeValueAsString(labelPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String url = githubBaseUrl + "/repos/" + owner + "/" + repo + "/issues/" + issueNumber + "/labels";
        restTemplate.postForObject(url, payload, String.class);
        
      } catch (Exception e) {
        throw new RuntimeException("GitHub label addition failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Add a comment to a GitHub issue or PR
   */
  public CompletableFuture<Void> addComment(String userId, String owner, String repo, int issueNumber, String comment) {
    return CompletableFuture.runAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "github");
        
        Map<String, Object> commentPayload = Map.of("body", comment);
        String payload = objectMapper.writeValueAsString(commentPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String url = githubBaseUrl + "/repos/" + owner + "/" + repo + "/issues/" + issueNumber + "/comments";
        restTemplate.postForObject(url, payload, String.class);
        
      } catch (Exception e) {
        throw new RuntimeException("GitHub comment addition failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Dispatch a GitHub workflow (for CI/CD automation)
   * Example: workflow_id="build.yml", ref="main", inputs={ key: value }
   */
  public CompletableFuture<Void> dispatchWorkflow(String userId, String owner, String repo,
                                                  String workflowId, String ref, Map<String, String> inputs) {
    return CompletableFuture.runAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "github");
        
        Map<String, Object> dispatchPayload = new LinkedHashMap<>();
        dispatchPayload.put("ref", ref);
        if (inputs != null && !inputs.isEmpty()) {
          dispatchPayload.put("inputs", inputs);
        }
        
        String payload = objectMapper.writeValueAsString(dispatchPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String url = githubBaseUrl + "/repos/" + owner + "/" + repo + "/actions/workflows/" + workflowId + "/dispatches";
        restTemplate.postForObject(url, payload, String.class);
        
      } catch (Exception e) {
        throw new RuntimeException("GitHub workflow dispatch failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Search for GitHub issues or PRs using GitHub search syntax
   * Example: query="repo:MyOrg/MyRepo is:open label:bug"
   */
  public CompletableFuture<List<Map<String, Object>>> search(String userId, String query) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "github");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        
        String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
        String url = githubBaseUrl + "/search/issues?q=" + encodedQuery;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        
        return (List<Map<String, Object>>) response.get("items");
        
      } catch (Exception e) {
        throw new RuntimeException("GitHub search failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Webhook receiver for GitHub events (push, PR, issue, etc.)
   * Publishes to Kafka: integrations.events topic
   */
  public void handleGitHubWebhook(Map<String, Object> payload, String event) {
    Map<String, Object> integrationEvent = new LinkedHashMap<>();
    integrationEvent.put("source", "github");
    integrationEvent.put("event", event);
    integrationEvent.put("timestamp", System.currentTimeMillis());
    
    // Extract relevant fields based on event type
    if ("push".equals(event)) {
      integrationEvent.put("branch", ((String) payload.get("ref")).replace("refs/heads/", ""));
      integrationEvent.put("commits", ((List) payload.get("commits")).size());
      integrationEvent.put("pusher", ((Map<String, Object>) payload.get("pusher")).get("name"));
    } else if ("pull_request".equals(event)) {
      Map<String, Object> pr = (Map<String, Object>) payload.get("pull_request");
      integrationEvent.put("action", payload.get("action"));
      integrationEvent.put("prNumber", pr.get("number"));
      integrationEvent.put("prTitle", pr.get("title"));
      integrationEvent.put("author", ((Map<String, Object>) pr.get("user")).get("login"));
    } else if ("issues".equals(event)) {
      Map<String, Object> issue = (Map<String, Object>) payload.get("issue");
      integrationEvent.put("action", payload.get("action"));
      integrationEvent.put("issueNumber", issue.get("number"));
      integrationEvent.put("issueTitle", issue.get("title"));
      integrationEvent.put("author", ((Map<String, Object>) issue.get("user")).get("login"));
    }
    
    System.out.println("GitHub webhook received: " + integrationEvent);
  }
}
