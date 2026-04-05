package com.imperium.api.service;

import com.imperium.api.model.AuditActionRequest;
import com.imperium.api.model.TransitionRequest;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.mapper.ArchiveRecordMapper;
import com.imperium.domain.mapper.AuditRecordMapper;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.RoleCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditArchiveServiceTest {

    @Mock
    private DocketMapper docketMapper;
    @Mock
    private AuditRecordMapper auditRecordMapper;
    @Mock
    private ArchiveRecordMapper archiveRecordMapper;
    @Mock
    private DocketService docketService;
    @Mock
    private DocketEventService docketEventService;

    @InjectMocks
    private AuditArchiveService auditArchiveService;

    @Test
    void passShouldArchiveAndTransitionDocket() {
        DocketEntity docket = new DocketEntity();
        docket.setId("IMP-1");
        docket.setTitle("Imperium Test Docket");
        docket.setState(DocketState.UNDER_AUDIT);
        docket.setSummary("summary");

        when(docketMapper.selectById("IMP-1")).thenReturn(docket, docket);
        when(archiveRecordMapper.selectOne(any())).thenReturn(null);

        auditArchiveService.pass("IMP-1", new AuditActionRequest(List.of("risk"), List.of("quality"), "final summary", List.of("artifact.txt")));

        verify(archiveRecordMapper).insert(any(com.imperium.domain.entity.ArchiveRecordEntity.class));

        ArgumentCaptor<TransitionRequest> captor = ArgumentCaptor.forClass(TransitionRequest.class);
        verify(docketService).transition(eq("IMP-1"), captor.capture());
        assertThat(captor.getValue().targetState()).isEqualTo(DocketState.ARCHIVED);
        assertThat(captor.getValue().actor()).isEqualTo(RoleCode.SCRIBA);
    }
}
