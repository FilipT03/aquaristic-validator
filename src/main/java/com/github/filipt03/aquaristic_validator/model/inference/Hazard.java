package com.github.filipt03.aquaristic_validator.model.inference;

import com.github.filipt03.aquaristic_validator.model.types.HazardType;

@lombok.Getter
@lombok.AllArgsConstructor
public class Hazard {
    private HazardType hazardType;
    private int affectedSpeciesId;
    private String description;
    private double severity;

    public Hazard(HazardType hazardType, int affectedSpeciesId, String description) {
        this.hazardType = hazardType;
        this.affectedSpeciesId = affectedSpeciesId;
        this.description = description;
        this.severity = 0;
    }
}