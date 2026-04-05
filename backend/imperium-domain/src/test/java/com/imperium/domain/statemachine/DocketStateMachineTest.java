package com.imperium.domain.statemachine;

import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocketStateMachineTest {

    @Test
    void shouldAllowPraecoToEnterSenateInStandardMode() {
        assertThat(DocketStateMachine.isValid(
            DocketState.TRIAGED,
            DocketState.IN_SENATE,
            RoleCode.PRAECO,
            OperatingMode.STANDARD_SENATE
        )).isTrue();
    }

    @Test
    void shouldRejectTriagedToInSenateInDirectDecreeMode() {
        assertThat(DocketStateMachine.isValid(
            DocketState.TRIAGED,
            DocketState.IN_SENATE,
            RoleCode.PRAECO,
            OperatingMode.DIRECT_DECREE
        )).isFalse();
    }

    @Test
    void shouldReturnModeAwareAvailableTransitions() {
        assertThat(DocketStateMachine.availableTransitions(DocketState.TRIAGED, OperatingMode.STANDARD_SENATE))
            .containsExactly(DocketState.IN_SENATE);

        assertThat(DocketStateMachine.availableTransitions(DocketState.TRIAGED, OperatingMode.DIRECT_DECREE))
            .containsExactly(DocketState.AWAITING_CAESAR);
    }
}
