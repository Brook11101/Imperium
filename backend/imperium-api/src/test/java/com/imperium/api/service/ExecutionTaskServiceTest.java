package com.imperium.api.service;

import com.imperium.api.model.ExecutionTaskCompleteRequest;
import com.imperium.api.model.TransitionRequest;
import com.imperium.domain.entity.DelegationEntity;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.entity.ExecutionTaskEntity;
import com.imperium.domain.mapper.DelegationMapper;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.mapper.ExecutionTaskMapper;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionTaskServiceTest {

    @Mock
    private ExecutionTaskMapper executionTaskMapper;
    @Mock
    private DelegationMapper delegationMapper;
    @Mock
    private DocketMapper docketMapper;
    @Mock
    private DocketService docketService;
    @Mock
    private DocketEventService docketEventService;

    @InjectMocks
    private ExecutionTaskService executionTaskService;

    @Test
    void completeShouldMoveDocketToAuditWhenAllTasksDone() {
        ExecutionTaskEntity task = new ExecutionTaskEntity();
        task.setId("EXT-1");
        task.setDocketId("IMP-1");
        task.setDelegationId("DEL-1");
        task.setRoleCode(RoleCode.LEGATUS);
        task.setStatus("RUNNING");

        DelegationEntity delegation = new DelegationEntity();
        delegation.setId("DEL-1");

        DocketEntity docket = new DocketEntity();
        docket.setId("IMP-1");
        docket.setState(DocketState.IN_EXECUTION);
        docket.setMode(OperatingMode.STANDARD_SENATE);

        when(executionTaskMapper.selectById("EXT-1")).thenReturn(task);
        when(delegationMapper.selectById("DEL-1")).thenReturn(delegation);
        when(docketMapper.selectById("IMP-1")).thenReturn(docket);
        when(executionTaskMapper.selectCount(any())).thenReturn(1L, 1L);

        executionTaskService.complete("EXT-1", new ExecutionTaskCompleteRequest("done"));

        ArgumentCaptor<TransitionRequest> captor = ArgumentCaptor.forClass(TransitionRequest.class);
        verify(docketService).transition(eq("IMP-1"), captor.capture());
        assertThat(captor.getValue().targetState()).isEqualTo(DocketState.UNDER_AUDIT);
    }
}
