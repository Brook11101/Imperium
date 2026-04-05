package com.imperium.worker.openclaw;

/**
 * OpenClaw CLI 调用结果
 */
public record OpenClawCliResult(
    int exitCode,
    String stdout,
    String stderr
) {
    public boolean success() {
        return exitCode == 0;
    }
}
