package com.github.filipt03.aquaristic_validator.dto;

import com.github.filipt03.aquaristic_validator.model.domain.*;
import com.github.filipt03.aquaristic_validator.model.types.SubstrateType;
import java.util.List;

public record AquariumRequest(
    double volumeLiters, double lengthCm, double widthCm, int ageInDays,
    SubstrateType substrateType, boolean hasCover, boolean hasLivePlants, int hidingSpots,
    double ph, double temperatureC,
    List<FishRequest> fish
) {
    public Aquarium toAquarium() {
        WaterParameters wp = new WaterParameters(ph, temperatureC);
        return Aquarium.builder()
            .volumeLiters(volumeLiters).lengthCm(lengthCm).widthCm(widthCm).ageInDays(ageInDays)
            .substrateType(substrateType).hasCover(hasCover).hasLivePlants(hasLivePlants)
            .hidingSpots(hidingSpots).waterParameters(wp).build();
    }

    public List<Fish> toFishList(List<FishData> fishDataList) {
        return fish.stream().map(fr -> {
            FishData data = fishDataList.stream()
                .filter(fd -> fd.getSpeciesId() == fr.speciesId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown speciesId " + fr.speciesId()));
            return new Fish(fr.speciesId(), data.getSpecies(), 0.0, fr.quantity());
        }).toList();
    }
}