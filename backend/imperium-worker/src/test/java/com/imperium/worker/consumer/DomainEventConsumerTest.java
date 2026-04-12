package com.imperium.worker.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperium.worker.model.DomainEventEnvelope;
import com.imperium.worker.orchestration.DomainEventRouter;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DomainEventConsumerTest {

    @Mock
    private DomainEventRouter domainEventRouter;

    @Test
    void shouldRouteDocketCreatedEvent() throws Exception {
        DomainEventConsumer consumer = new DomainEventConsumer(domainEventRouter);

        MessageExt message = message("DOCKET_CREATED", new DomainEventEnvelope(1L, "DOCKET", "IMP-1", "DOCKET_CREATED", Map.of()));
        consumer.onMessage(message);

        verify(domainEventRouter).route("DOCKET_CREATED", new DomainEventEnvelope(1L, "DOCKET", "IMP-1", "DOCKET_CREATED", Map.of()));
    }

    @Test
    void shouldRouteSenateOpinionRecordedEvent() throws Exception {
        DomainEventConsumer consumer = new DomainEventConsumer(domainEventRouter);

        MessageExt message = message("SENATE_OPINION_RECORDED", new DomainEventEnvelope(1L, "DOCKET", "IMP-1", "SENATE_OPINION_RECORDED", Map.of()));
        consumer.onMessage(message);

        verify(domainEventRouter).route("SENATE_OPINION_RECORDED", new DomainEventEnvelope(1L, "DOCKET", "IMP-1", "SENATE_OPINION_RECORDED", Map.of()));
    }

    private MessageExt message(String tag, DomainEventEnvelope envelope) throws Exception {
        MessageExt message = new MessageExt();
        message.setTags(tag);
        message.setBody(new ObjectMapper().writeValueAsString(envelope).getBytes(StandardCharsets.UTF_8));
        return message;
    }
}
