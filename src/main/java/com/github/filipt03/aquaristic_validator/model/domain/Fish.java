package com.github.filipt03.aquaristic_validator.model.domain;

@lombok.Getter
@lombok.AllArgsConstructor
public class Fish {
    private int speciesId;
    private String species;
    @lombok.Setter
    private int quantity;
}