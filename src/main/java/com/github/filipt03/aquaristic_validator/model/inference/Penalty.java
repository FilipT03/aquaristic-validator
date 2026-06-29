package com.github.filipt03.aquaristic_validator.model.inference;

import com.github.filipt03.aquaristic_validator.model.types.RiskType;

@lombok.Getter
@lombok.AllArgsConstructor
public class Penalty {
    private RiskType type;
    private int affectedSpeciesId;
    private int value;
    private String message;
}
