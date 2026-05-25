package com.github.filipt03.aquaristic_validator.model.domain;

import com.github.filipt03.aquaristic_validator.model.types.SubstrateType;

@lombok.Getter
@lombok.AllArgsConstructor
public class Aquarium {
    private double volumeLiters;
    private double lengthCm;
    private double widthCm;
    private int ageInDays;

    private SubstrateType substrateType;
    private boolean hasCover;
    private boolean hasLivePlants;
    private boolean hasWoodDecoration;
    private int hidingSpots;

    private WaterParameters waterParameters;
    private Equipment equipment;
}