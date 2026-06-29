package com.github.filipt03.aquaristic_validator.model.domain;

import java.util.List;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.Builder
public class FishData {
    private int speciesId;
    private String species;
    private String genus;
    private String family;
    
    private List<String> ecology;
    private String airBreathing;
    private List<String> behaviors;
    private List<String> habitats;
    private List<String> diet;

    private Double maxAdultLengthCm;
    private Double trophicLevel;
    private Integer minGroupSize;
    private Integer aggressionLevel; // 1-10 scale
    private boolean tropical;
}
