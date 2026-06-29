package com.github.filipt03.aquaristic_validator.dto;
public record EquipmentRequest(String filterModel, String heaterModel, Double filterFlowRateLPH, boolean hasLights) {}