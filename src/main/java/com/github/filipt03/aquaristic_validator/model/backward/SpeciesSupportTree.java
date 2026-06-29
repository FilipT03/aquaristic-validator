package com.github.filipt03.aquaristic_validator.model.backward;

import java.util.ArrayList;
import java.util.List;

public class SpeciesSupportTree {
    public static List<GoalRequirement> createGoalTree() {
        List<GoalRequirement> edges = new ArrayList<>();

        edges.add(new GoalRequirement("SPECIES_SUPPORTED", "STABLE_FOOD_SOURCE", null, null, false));
        edges.add(new GoalRequirement("SPECIES_SUPPORTED", "LOW_STRESS_ENVIRONMENT", null,
            "No social-stress or territorial-conflict hazards should affect this species.", true));
        edges.add(new GoalRequirement("SPECIES_SUPPORTED", "WATER_PARAMETERS_COMPATIBLE", null,
            "Tank pH and temperature should fall inside this species' required range.", true));

        edges.add(new GoalRequirement("STABLE_FOOD_SOURCE", "SNAIL_POPULATION_STABLE", new Condition("diet", "snails", "contains", false),
            "Tank should be able to sustain a snail population because of species' diet.", false));
        edges.add(new GoalRequirement("STABLE_FOOD_SOURCE", "LIVE_PLANTS_STABLE", new Condition("diet", "plant matter", "contains", false), null, false));
        edges.add(new GoalRequirement("STABLE_FOOD_SOURCE", "WOOD_PRESENT", new Condition("diet", "wood", "contains", false),
            "Tank should contain wood because of species' diet.", true));

        edges.add(new GoalRequirement("SNAIL_POPULATION_STABLE", "LIVE_PLANTS_STABLE", null, null, false));
        edges.add(new GoalRequirement("SNAIL_POPULATION_STABLE", "ORGANIC_WASTE_AVAILABLE", null,
            "Decomposing organic matter (fish waste, leftover food) must be present to sustain a snail population.", true));

        edges.add(new GoalRequirement("LIVE_PLANTS_STABLE", "ADEQUATE_LIGHTING", null,
            "Live plants require adequate lighting to photosynthesize and survive.", true));
        edges.add(new GoalRequirement("LIVE_PLANTS_STABLE", "NUTRIENT_SUBSTRATE", null,
            "Live plants require a nutrient-rich substrate to root in and draw nutrients from.", true));
        edges.add(new GoalRequirement("LIVE_PLANTS_STABLE", "LIVE_PLANTS_PRESENT", null,
            "Live plants are required to be present in the tank.", true));

        for (GoalRequirement edge : edges) {
            if (edge.getCondition() == null) {
                edge.setCondition(Condition.trueCondition());
            }
        }

        return edges;
    }
}