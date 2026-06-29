package com.github.filipt03.aquaristic_validator.model.inference;

import com.github.filipt03.aquaristic_validator.model.types.StateType;

@lombok.Getter
@lombok.AllArgsConstructor
public class State {
    private StateType stateType;
    private int causingSpeciesId;

    public State(StateType stateType) {
        this.stateType = stateType;
        this.causingSpeciesId = -1;
    }
}
