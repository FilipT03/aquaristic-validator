package com.github.filipt03.aquaristic_validator.model.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Equipment {
    private String filterModel;
    private String heaterModel;
    private Double filterFlowRateLPH;
    private boolean hasAirPump;
    private boolean hasLights;
}