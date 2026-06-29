package com.github.filipt03.aquaristic_validator.dto;

import com.github.filipt03.aquaristic_validator.model.domain.*;
import com.github.filipt03.aquaristic_validator.model.types.SubstrateType;
import java.util.List;

public record AquariumRequest(
    double volumeLiters, double lengthCm, double widthCm, double heightCm, int ageInDays,
    SubstrateType substrateType, boolean hasCover, boolean hasLivePlants, boolean hasWoodDecoration, int hidingSpots,
    double ph, double temperatureC,
    EquipmentRequest equipment,
    List<FishRequest> fish
) {
    public Aquarium toAquarium() {
        WaterParameters wp = new WaterParameters(ph, temperatureC);
        Equipment eq = new Equipment(equipment.filterModel(), equipment.heaterModel(),
                                      equipment.filterFlowRateLPH(), equipment.hasLights());
        return Aquarium.builder()
            .volumeLiters(volumeLiters).lengthCm(lengthCm).widthCm(widthCm).heightCm(heightCm)
            .ageInDays(ageInDays).substrateType(substrateType).hasCover(hasCover)
            .hasLivePlants(hasLivePlants).hasWoodDecoration(hasWoodDecoration)
            .hidingSpots(hidingSpots).waterParameters(wp).equipment(eq).build();
    }

    public List<Fish> toFishList(List<FishData> fishDataList) {
        return fish.stream().map(fr -> {
            FishData data = fishDataList.stream()
                .filter(fd -> fd.getSpeciesId() == fr.speciesId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown speciesId " + fr.speciesId()));
            return new Fish(fr.speciesId(), data.getSpecies(), fr.quantity());
        }).toList();
    }
}