package com.imperium.api.model;

import com.imperium.domain.model.OperatingMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建议案请求
 */
public record CreateDocketRequest(

    @NotBlank(message = "法令内容不能为空")
    String edictRaw,

    @NotNull(message = "运行模式不能为空")
    OperatingMode mode,

    String priority
) {
}
