# Agent 集成与调度执行设计

## 1. 设计目标

Agent 集成层需要满足以下目标：

- Java 服务通过 CLI 调用 OpenClaw
- 调用过程异步执行，不阻塞主请求
- 支持超时、重试、失败记录
- 支持结构化结果回传
- 支持停滞检测、升级和回滚
- 保持角色权限边界不被破坏

## 2. 集成方式

推荐方式：

- `imperium-worker` 调用 OpenClaw CLI
- 通过 `ProcessBuilder` 执行命令
- 所有调用统一封装在 `OpenClawCliClient`

不建议 `imperium-api` 直接执行 CLI。

原因：

- CLI 调用耗时不稳定
- 长任务不应占用 API 请求线程
- Worker 更适合消费消息并执行异步任务

## 3. CLI 调用模型

命令结构建议：

```bash
openclaw agent --agent <agentId> -m "<prompt>" --timeout <seconds>
```

根据 OpenClaw 实际能力，后续可追加更多参数。

Java 封装建议：

```java
public interface OpenClawCliClient {
    AgentCallResult call(AgentJob job);
}
```

`AgentCallResult` 至少包含：

- `success`
- `exitCode`
- `stdout`
- `stderr`
- `startedAt`
- `endedAt`
- `durationMs`

## 4. Agent Job 设计

每次调度都抽象成标准任务对象。

字段建议：

- `jobId`
- `docketId`
- `roleCode`
- `agentId`
- `jobType`
- `prompt`
- `timeoutSec`
- `retryPolicy`
- `callbackToken`

## 5. Prompt 输入契约

每次给 Agent 的 Prompt 必须包含：

- 当前角色身份
- 当前议案摘要
- 当前状态
- 当前运行模式
- 允许做的事
- 不允许做的事
- 需要回传的接口信息
- 本次任务目标
- 输出格式要求

## 6. Agent 输出契约

建议优先采用结构化输出。

最少应要求 Agent 返回：

- `resultType`
- `summary`
- `details`
- `recommendedNextState`
- `artifacts`
- `riskSignals`

如果 OpenClaw CLI 不方便直接产出 JSON，可以约定：

- 明确的 JSON 区块开始与结束标记
- Worker 侧统一解析

## 7. 回传机制

仅依赖 CLI 最终输出不足以支撑全过程可观测性，因此建议增加 Agent 回调协议。

由 Worker 在 Prompt 中注入：

- 服务地址
- `callbackToken`

建议内部接口：

- `POST /internal/agent-callback/progress`
- `POST /internal/agent-callback/result`
- `POST /internal/agent-callback/fail`

回传内容可用于：

- 元老意见上报
- 执行进度上报
- 阻塞原因上报
- 审计结果上报

## 8. 元老院并行审议实现

元老院不是自由聊天群，而是系统编排的并行审议机制。

流程如下：

1. 议案进入 `Debating`
2. Worker 生成 3 个元老任务
3. 投递到 `imperium-agent-job-topic`
4. 分别调用：
   - `senator_strategos`
   - `senator_juris`
   - `senator_fiscus`
5. 等待三条结果全部返回
6. 进入 `SenateAggregationService`
7. 生成：
   - 共识点
   - 争议点
   - 推荐动议
8. 推进到 `VetoReview`

## 9. 保民官执行策略

`tribune` 是单角色、强判断节点。

进入 `VetoReview` 后：

- Worker 汇总元老院结论
- 调用 `tribune`
- 结果只允许三类：
  - 放行
  - 否决
  - 退回重议

`tribune` 不应直接触发执行任务。

## 10. 执政官执行策略

`consul` 负责：

- 接收已批准议案
- 生成派发方案
- 拆解为多个 `Delegation`
- 为执行角色生成 Agent Job

执行层之间默认不自由互调，由系统和执政官统一协调。

## 11. 停滞检测与恢复

### 11.1 停滞扫描

由 Worker 定时任务执行：

- 扫描 `docket`
- 检查 `lastProgressAt`
- 判断是否进入恢复流程

### 11.2 恢复顺序

建议恢复顺序如下：

1. 重试当前角色任务
2. 升级给 `consul`
3. 升级给 `tribune`
4. 升级到 `AwaitingCaesar`
5. 必要时回滚到最近稳定快照

## 12. 快照与回滚

建议在关键稳定状态保存快照：

- `Triaged`
- `InSenate`
- `AwaitingCaesar`
- `Delegated`

回滚时恢复：

- 状态
- 当前责任角色
- 最近有效授权信息

## 13. 调用日志要求

所有 CLI 调用必须写入 `agent_call_log`，至少包含：

- 角色
- 命令行
- 开始时间
- 结束时间
- 退出码
- 标准输出
- 错误输出
- 是否成功

## 14. 设计结论

Agent 集成层的原则是：

- Worker 异步调用 OpenClaw CLI
- Agent 通过回调接口回传结构化结果
- 所有调度、重试、升级、回滚行为都必须可审计
