package com.imperium.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 执政官批量派发请求
 */
public record CreateDelegationRequest(
    @Valid
    @NotEmpty(message = "至少需要一个派发项")
    List<DelegationItemRequest> items
) {
}
