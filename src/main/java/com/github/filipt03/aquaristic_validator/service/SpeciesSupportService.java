package com.github.filipt03.aquaristic_validator.service;

import com.github.filipt03.aquaristic_validator.model.backward.GoalRequirement;
import com.github.filipt03.aquaristic_validator.model.backward.ProofResult;
import com.github.filipt03.aquaristic_validator.model.backward.ProofResultNode;
import com.github.filipt03.aquaristic_validator.model.backward.SpeciesSupportTree;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpeciesSupportService {

    public ProofResult isSupported(KieSession kSession, int speciesId) {
        return evaluate(kSession, "SPECIES_SUPPORTED", speciesId);
    }

    public ProofResult evaluate(KieSession kSession, String goal, int speciesId) {
        QueryResults results = kSession.getQueryResults(
            "requirementsForGoal", goal, Variable.v, Variable.v, Variable.v, Variable.v, speciesId);

        List<String> failedRequirements = new ArrayList<>();
        List<String> visited = new ArrayList<>();
        boolean allSatisfied = true;
        for (QueryResultsRow row : results) {
            String requirement = (String) row.get("$requirement");
            String rowGoal = (String) row.get("$target");
            boolean terminal = (boolean) row.get("$terminal");
            visited.add(rowGoal + ":" + requirement);
            if (requirement != null && terminal) {
                boolean leafResult = evaluateLeaf(kSession, requirement, speciesId);
                if (!leafResult) {
                    failedRequirements.add(requirement);
                    allSatisfied = false;
                }
            }
        }
        return allSatisfied ? ProofResult.success() : ProofResult.failure(buildTree(failedRequirements, visited));
    }

    private boolean evaluateLeaf(KieSession kSession, String requirement, int speciesId) {
        boolean satisfied = switch (requirement) {
            case "ADEQUATE_LIGHTING" -> kSession.getQueryResults("adequateLightingSatisfied").size() > 0;
            case "NUTRIENT_SUBSTRATE" -> kSession.getQueryResults("nutrientSubstrateSatisfied").size() > 0;
            case "ORGANIC_WASTE_AVAILABLE" -> kSession.getQueryResults("organicWasteAvailableSatisfied").size() > 0;
            case "WATER_PARAMETERS_COMPATIBLE" -> kSession.getQueryResults("waterParametersCompatibleSatisfied", speciesId).size() > 0;
            case "LOW_STRESS_ENVIRONMENT" -> kSession.getQueryResults("lowStressEnvironmentSatisfied", speciesId).size() > 0;
            case "LIVE_PLANTS_PRESENT" -> kSession.getQueryResults("livePlantsPresentSatisfied").size() > 0;
            case "WOOD_PRESENT" -> kSession.getQueryResults("woodPresentSatisfied").size() > 0;
            default -> throw new IllegalStateException("Unknown leaf check: " + requirement);
        };

        return satisfied;
    }

    private Map<String, List<ProofResultNode>> buildTree(List<String> failedRequirements, List<String> visitedRequirements) {
        Map<String, List<ProofResultNode>> tree = new HashMap<>();
        List<GoalRequirement> speciesSupportTree = SpeciesSupportTree.createGoalTree();
        for (GoalRequirement goal : speciesSupportTree) {
            if (!visitedRequirements.contains(goal.getGoal() + ":" + goal.getRequirement())) continue;
            tree.computeIfAbsent(goal.getGoal(), k -> new ArrayList<>()).add(new ProofResultNode(goal.getRequirement(), null));
        }
        for (GoalRequirement goal : speciesSupportTree) {
            if (!tree.containsKey(goal.getRequirement())) {
                boolean failed = failedRequirements.contains(goal.getRequirement());
                if (failed)
                    tree.computeIfAbsent(goal.getRequirement(), k -> new ArrayList<>()).add(new ProofResultNode(null, failed ? goal.getExplanation() : "Satisfied"));
            }
        }
        return tree;
    }
}