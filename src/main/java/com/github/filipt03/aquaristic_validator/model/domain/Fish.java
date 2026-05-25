package com.github.filipt03.aquaristic_validator.model.domain;

@lombok.Getter
@lombok.AllArgsConstructor
public class Fish {
    private String species;
    private String family;
    private double averageLengthCm;
    private int quantity;
}