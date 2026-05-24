package com.github.filipt03.aquaristic_validator.model.inference;

import com.github.filipt03.aquaristic_validator.model.types.HazardType;

@lombok.Getter
@lombok.AllArgsConstructor
public class Hazard {
    private HazardType hazardType;
    private String affectedSpecies;
    private String description;
}