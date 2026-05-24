package com.github.filipt03.aquaristic_validator.model.domain;

import com.github.filipt03.aquaristic_validator.model.types.SubstrateType;

@lombok.Getter
@lombok.AllArgsConstructor
public class Aquarium {
    private double volumeLiters;
    private int ageInDays;
    private SubstrateType substrateType;
}