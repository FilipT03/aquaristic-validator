package com.github.filipt03.aquaristic_validator.model.inference;

import com.github.filipt03.aquaristic_validator.model.types.PenaltyType;

@lombok.Getter
@lombok.AllArgsConstructor
public class Penalty {
    private PenaltyType penaltyType;
    private String affectedSpecies;
    private int value;
    private String message;
}
