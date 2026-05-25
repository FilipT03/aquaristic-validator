package com.github.filipt03.aquaristic_validator.model.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Equipment {
    private double filterFlowRateLpH; // Liters per hour
    private double heaterPowerW;      // Watts
    private boolean hasAirPump;
    private boolean hasLights;
}