package com.imperium.worker.openclaw;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OpenClaw CLI 客户端
 */
@Component
public class OpenClawCliClient {

    @Value("${imperium.openclaw.path:openclaw}")
    private String openClawPath;

    public OpenClawCliResult execute(String agentId, String prompt, int timeoutSec) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(List.of(
            openClawPath,
            "agent",
            "--agent",
            agentId,
            "-m",
            prompt,
            "--timeout",
            String.valueOf(timeoutSec)
        ));

        Process process = processBuilder.start();
        boolean finished = process.waitFor(timeoutSec + 5L, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return new OpenClawCliResult(124, "", "OpenClaw CLI timeout");
        }

        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        return new OpenClawCliResult(process.exitValue(), stdout, stderr);
    }
}
