package com.imperium.worker.orchestration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Worker 调用 API 内部编排接口的客户端
 */
@Component
public class OrchestrationClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    @Value("${imperium.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${imperium.internal.secret:${imperium.agent.callback-secret:dev-secret-change-in-prod}}")
    private String internalSecret;

    public void triage(String docketId) throws IOException, InterruptedException {
        post("/internal/orchestration/dockets/" + docketId + "/triage");
    }

    public void finalizeSenate(String docketId) throws IOException, InterruptedException {
        post("/internal/orchestration/dockets/" + docketId + "/finalize-senate");
    }

    public void openSenate(String docketId) throws IOException, InterruptedException {
        post("/api/dockets/" + docketId + "/senate/open");
    }

    public void autoCaesarApprove(String docketId) throws IOException, InterruptedException {
        post("/internal/orchestration/dockets/" + docketId + "/auto-caesar-approve");
    }

    public void autoDelegate(String docketId) throws IOException, InterruptedException {
        post("/internal/orchestration/dockets/" + docketId + "/auto-delegate");
    }

    public void autoCompleteExecution(String docketId) throws IOException, InterruptedException {
        post("/internal/orchestration/dockets/" + docketId + "/auto-complete-execution");
    }

    public void autoPassAudit(String docketId) throws IOException, InterruptedException {
        post("/internal/orchestration/dockets/" + docketId + "/auto-pass-audit");
    }

    private void post(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .timeout(Duration.ofSeconds(10))
            .header("X-Imperium-Internal-Secret", internalSecret)
            .POST(HttpRequest.BodyPublishers.ofString(""))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("Internal orchestration call failed: " + response.statusCode() + " body=" + response.body());
        }
    }
}
