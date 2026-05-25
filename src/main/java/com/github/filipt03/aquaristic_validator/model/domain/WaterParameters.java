package com.github.filipt03.aquaristic_validator.model.domain;

@lombok.Getter
@lombok.AllArgsConstructor
public class WaterParameters {
    private double pH;
    private double temperatureC; // Celsius
    private double gh;
}
