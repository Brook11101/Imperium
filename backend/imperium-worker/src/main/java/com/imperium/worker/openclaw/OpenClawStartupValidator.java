package com.imperium.worker.openclaw;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.domain.entity.RoleConfigEntity;
import com.imperium.domain.mapper.RoleConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Worker 启动时检查 OpenClaw 运行时和角色配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "imperium.openclaw.startup-validation", havingValue = "true", matchIfMissing = true)
public class OpenClawStartupValidator implements ApplicationRunner {

    private final RoleConfigMapper roleConfigMapper;

    @Value("${imperium.openclaw.path:openclaw}")
    private String openClawPath;

    @Value("${imperium.openclaw.workspace-root:/tmp/imperium/workspaces}")
    private String workspaceRoot;

    @Value("${imperium.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${imperium.agent.callback-secret:dev-secret-change-in-prod}")
    private String callbackSecret;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        validateWorkspaceRoot();
        validateOpenClawBinary();
        validateSecrets();
        logConfiguredRoles();
    }

    private void validateWorkspaceRoot() throws Exception {
        Files.createDirectories(Path.of(workspaceRoot));
        log.info("OpenClaw workspace root: {}", workspaceRoot);
    }

    private void validateOpenClawBinary() {
        try {
            Process process = new ProcessBuilder(openClawPath, "--version").start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("OpenClaw CLI 校验失败，exitCode=" + exitCode);
            }
            log.info("OpenClaw binary verified at path: {}", openClawPath);
        } catch (Exception ex) {
            throw new IllegalStateException("OpenClaw CLI 不可用，请检查 OPENCLAW_PATH: " + openClawPath, ex);
        }
    }

    private void validateSecrets() {
        if (callbackSecret == null || callbackSecret.isBlank()) {
            throw new IllegalStateException("AGENT_CALLBACK_SECRET 不能为空");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("IMPERIUM_BASE_URL 不能为空");
        }
    }

    private void logConfiguredRoles() {
        List<RoleConfigEntity> roles = roleConfigMapper.selectList(
            new LambdaQueryWrapper<RoleConfigEntity>()
                .eq(RoleConfigEntity::getEnabled, 1)
                .isNotNull(RoleConfigEntity::getAgentId)
        );
        long mapped = roles.stream().filter(role -> role.getAgentId() != null && !role.getAgentId().isBlank()).count();
        log.info("已启用角色 {} 个，其中已映射 OpenClaw agentId {} 个", roles.size(), mapped);
    }
}
