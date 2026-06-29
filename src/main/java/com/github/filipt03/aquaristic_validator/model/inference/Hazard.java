package com.github.filipt03.aquaristic_validator.model.inference;

import com.github.filipt03.aquaristic_validator.model.types.RiskType;

@lombok.Getter
@lombok.AllArgsConstructor
public class Hazard {
    private RiskType type;
    private int affectedSpeciesId;
    private String description;
    private double severity;

    public Hazard(RiskType type, int affectedSpeciesId, String description) {
        this.type = type;
        this.affectedSpeciesId = affectedSpeciesId;
        this.description = description;
        this.severity = 1;
    }
}