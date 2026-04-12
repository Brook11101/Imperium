package com.imperium.worker.openclaw;

import com.imperium.domain.entity.DelegationEntity;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.entity.ExecutionTaskEntity;
import com.imperium.domain.entity.RoleConfigEntity;
import com.imperium.domain.mapper.AgentCallLogMapper;
import com.imperium.domain.mapper.DelegationMapper;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.mapper.ExecutionTaskMapper;
import com.imperium.domain.mapper.RoleConfigMapper;
import com.imperium.domain.mapper.SenateOpinionMapper;
import com.imperium.domain.mapper.SenateSessionMapper;
import com.imperium.domain.mapper.TribuneReviewMapper;
import com.imperium.domain.model.RoleCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentDispatchServiceTest {

    @Mock private ExecutionTaskMapper executionTaskMapper;
    @Mock private RoleConfigMapper roleConfigMapper;
    @Mock private AgentCallLogMapper agentCallLogMapper;
    @Mock private OpenClawCliClient openClawCliClient;
    @Mock private DelegationMapper delegationMapper;
    @Mock private DocketMapper docketMapper;
    @Mock private SenateOpinionMapper senateOpinionMapper;
    @Mock private SenateSessionMapper senateSessionMapper;
    @Mock private TribuneReviewMapper tribuneReviewMapper;

    @Test
    void shouldSkipExecutionDispatchWhenDependenciesNotComplete() throws Exception {
        AgentDispatchService service = new AgentDispatchService(
            executionTaskMapper,
            roleConfigMapper,
            agentCallLogMapper,
            openClawCliClient,
            delegationMapper,
            docketMapper,
            senateOpinionMapper,
            senateSessionMapper,
            tribuneReviewMapper
        );
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "callbackSecret", "secret");
        ReflectionTestUtils.setField(service, "defaultTimeoutSec", 10);

        ExecutionTaskEntity task = new ExecutionTaskEntity();
        task.setId("EXT-1");
        task.setDocketId("IMP-1");
        task.setDelegationId("DEL-1");
        task.setRoleCode(RoleCode.LEGATUS);
        task.setStatus("PENDING");

        DelegationEntity delegation = new DelegationEntity();
        delegation.setId("DEL-1");
        delegation.setDependsOnJson(List.of("DEL-2"));

        DelegationEntity dependency = new DelegationEntity();
        dependency.setId("DEL-2");
        dependency.setStatus("PENDING");

        when(executionTaskMapper.selectList(any())).thenReturn(List.of(task));
        when(delegationMapper.selectById("DEL-1")).thenReturn(delegation);
        when(delegationMapper.selectById("DEL-2")).thenReturn(dependency);

        service.dispatchPendingExecutionTasks("IMP-1");

        verify(openClawCliClient, never()).execute(any(), any(), any(Integer.class));
    }
}
