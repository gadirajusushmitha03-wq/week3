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
 * CI/CD Integration Service - Triggers builds, deployments via Jenkins, GitHub Actions, GitLab CI
 * Uses encrypted OAuth tokens for authentication
 */
@Service
public class CICDIntegrationService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final OAuthTokenService oauthTokenService;

  @Value("${jenkins.base-url:http://jenkins.example.com}")
  private String jenkinsBaseUrl;

  @Value("${gitlab.api.base-url:https://gitlab.com/api/v4}")
  private String gitlabBaseUrl;

  public CICDIntegrationService(RestTemplate restTemplate, ObjectMapper objectMapper, 
                                OAuthTokenService oauthTokenService) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.oauthTokenService = oauthTokenService;
  }

  /**
   * Trigger a Jenkins job with parameters
   * Example: jobName="deploy-app", params={ ENVIRONMENT: "staging", VERSION: "1.2.3" }
   */
  public CompletableFuture<String> triggerJenkinsJob(String userId, String jobName, Map<String, String> params) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "jenkins");
        
        // Build query parameters
        StringBuilder queryParams = new StringBuilder();
        if (params != null && !params.isEmpty()) {
          queryParams.append("?");
          params.forEach((k, v) -> queryParams.append(k).append("=").append(v).append("&"));
        }
        
        String url = jenkinsBaseUrl + "/job/" + jobName + "/buildWithParameters" + queryParams;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
          (userId + ":" + accessToken).getBytes()
        ));
        
        Map<String, Object> response = restTemplate.postForObject(url, "", Map.class);
        String location = (String) response.get("location");
        
        // Extract build number from response header location
        return location.substring(location.lastIndexOf('/') + 1);
        
      } catch (Exception e) {
        throw new RuntimeException("Jenkins job trigger failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Get Jenkins job build status
   */
  public CompletableFuture<Map<String, Object>> getJenkinsBuildStatus(String userId, String jobName, String buildNumber) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "jenkins");
        
        String url = jenkinsBaseUrl + "/job/" + jobName + "/" + buildNumber + "/api/json";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
          (userId + ":" + accessToken).getBytes()
        ));
        
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        
        return Map.of(
          "number", response.get("number"),
          "status", response.get("result"),
          "duration", response.get("duration"),
          "timestamp", response.get("startTime"),
          "url", response.get("url")
        );
        
      } catch (Exception e) {
        throw new RuntimeException("Jenkins build status retrieval failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Trigger a GitLab CI pipeline
   * Example: projectId="123", ref="main", variables={ ENV: "staging" }
   */
  public CompletableFuture<String> triggerGitLabPipeline(String userId, String projectId, String ref, 
                                                         Map<String, String> variables) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "gitlab");
        
        Map<String, Object> pipelinePayload = new LinkedHashMap<>();
        pipelinePayload.put("ref", ref);
        if (variables != null && !variables.isEmpty()) {
          List<Map<String, String>> vars = new ArrayList<>();
          variables.forEach((k, v) -> vars.add(Map.of("key", k, "value", v)));
          pipelinePayload.put("variables", vars);
        }
        
        String payload = objectMapper.writeValueAsString(pipelinePayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String url = gitlabBaseUrl + "/projects/" + projectId + "/pipeline";
        String response = restTemplate.postForObject(url, payload, String.class);
        
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return String.valueOf(responseMap.get("id"));
        
      } catch (Exception e) {
        throw new RuntimeException("GitLab pipeline trigger failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Get GitLab pipeline status
   */
  public CompletableFuture<Map<String, Object>> getGitLabPipelineStatus(String userId, String projectId, String pipelineId) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String accessToken = oauthTokenService.getAccessToken(userId, "gitlab");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", accessToken);
        
        String url = gitlabBaseUrl + "/projects/" + projectId + "/pipelines/" + pipelineId;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        
        return Map.of(
          "id", response.get("id"),
          "status", response.get("status"),
          "createdAt", response.get("created_at"),
          "updatedAt", response.get("updated_at"),
          "ref", response.get("ref")
        );
        
      } catch (Exception e) {
        throw new RuntimeException("GitLab pipeline status retrieval failed: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Trigger a GitHub Actions workflow (delegated to GitHubIntegrationService)
   * Provided for unified CI/CD interface
   */
  public CompletableFuture<Void> triggerGitHubActions(String userId, String owner, String repo,
                                                      String workflowId, String ref, Map<String, String> inputs) {
    // Delegate to GitHub service (would be injected in real implementation)
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Webhook receiver for CI/CD pipeline status changes
   * Jenkins, GitLab, GitHub Actions can push notifications here
   */
  public void handleCICDWebhook(Map<String, Object> payload, String source) {
    Map<String, Object> integrationEvent = new LinkedHashMap<>();
    integrationEvent.put("source", source);
    integrationEvent.put("timestamp", System.currentTimeMillis());
    
    if ("jenkins".equals(source)) {
      // Jenkins webhook format (GitHub-based)
      integrationEvent.put("event", "build");
      integrationEvent.put("buildNumber", payload.get("build_number"));
      integrationEvent.put("status", payload.get("build_status"));
      integrationEvent.put("branch", ((Map<String, Object>) payload.get("build")).get("branch"));
    } else if ("gitlab".equals(source)) {
      // GitLab webhook format
      integrationEvent.put("event", "pipeline");
      integrationEvent.put("pipelineId", ((Map<String, Object>) payload.get("object_attributes")).get("id"));
      integrationEvent.put("status", ((Map<String, Object>) payload.get("object_attributes")).get("status"));
      integrationEvent.put("ref", ((Map<String, Object>) payload.get("object_attributes")).get("ref"));
    } else if ("github".equals(source)) {
      // GitHub Actions webhook format
      integrationEvent.put("event", "workflow_run");
      integrationEvent.put("workflowName", payload.get("workflow_run"));
      integrationEvent.put("status", ((Map<String, Object>) payload.get("workflow_run")).get("status"));
    }
    
    System.out.println("CI/CD webhook received: " + integrationEvent);
  }

  /**
   * Unified CI/CD trigger helper - routes to appropriate service
   * Simplifies bot action execution
   */
  public CompletableFuture<String> triggerBuild(String userId, String cicdProvider, String config) {
    return switch (cicdProvider) {
      case "jenkins" -> {
        Map<String, String> params = parseConfig(config);
        yield triggerJenkinsJob(userId, params.get("jobName"), params).thenApply(buildNum -> "Jenkins build #" + buildNum);
      }
      case "gitlab" -> {
        Map<String, String> params = parseConfig(config);
        yield triggerGitLabPipeline(userId, params.get("projectId"), params.get("ref"), params)
          .thenApply(pipelineId -> "GitLab pipeline #" + pipelineId);
      }
      default -> CompletableFuture.failedFuture(new RuntimeException("Unknown CI/CD provider: " + cicdProvider));
    };
  }

  private Map<String, String> parseConfig(String configString) {
    // Simple key=value parser; in production, use JSON
    Map<String, String> config = new HashMap<>();
    if (configString != null) {
      String[] pairs = configString.split(",");
      for (String pair : pairs) {
        String[] kv = pair.trim().split("=");
        if (kv.length == 2) {
          config.put(kv[0], kv[1]);
        }
      }
    }
    return config;
  }
}
