package com.imperium.worker.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperium.domain.entity.SenateSessionEntity;
import com.imperium.domain.mapper.SenateSessionMapper;
import com.imperium.worker.model.DomainEventEnvelope;
import com.imperium.worker.openclaw.AgentDispatchService;
import com.imperium.worker.orchestration.OrchestrationClient;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainEventConsumerTest {

    @Mock
    private AgentDispatchService agentDispatchService;
    @Mock
    private SenateSessionMapper senateSessionMapper;
    @Mock
    private OrchestrationClient orchestrationClient;

    @Test
    void shouldCallTriageOnDocketCreated() throws Exception {
        DomainEventConsumer consumer = new DomainEventConsumer(new ObjectMapper(), agentDispatchService, senateSessionMapper, orchestrationClient);
        doNothing().when(orchestrationClient).triage("IMP-1");

        MessageExt message = message("DOCKET_CREATED", new DomainEventEnvelope(1L, "DOCKET", "IMP-1", "DOCKET_CREATED", Map.of()));
        consumer.onMessage(message);

        verify(orchestrationClient).triage("IMP-1");
    }

    @Test
    void shouldFinalizeClosedSenateOnOpinionRecorded() throws Exception {
        DomainEventConsumer consumer = new DomainEventConsumer(new ObjectMapper(), agentDispatchService, senateSessionMapper, orchestrationClient);
        SenateSessionEntity session = new SenateSessionEntity();
        session.setId("SEN-1");
        when(senateSessionMapper.selectOne(any())).thenReturn(session);

        MessageExt message = message("SENATE_OPINION_RECORDED", new DomainEventEnvelope(1L, "DOCKET", "IMP-1", "SENATE_OPINION_RECORDED", Map.of()));
        consumer.onMessage(message);

        verify(orchestrationClient).finalizeSenate("IMP-1");
    }

    private MessageExt message(String tag, DomainEventEnvelope envelope) throws Exception {
        MessageExt message = new MessageExt();
        message.setTags(tag);
        message.setBody(new ObjectMapper().writeValueAsString(envelope).getBytes(StandardCharsets.UTF_8));
        return message;
    }
}
