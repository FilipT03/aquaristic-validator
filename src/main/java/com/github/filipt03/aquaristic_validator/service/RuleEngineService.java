package com.github.filipt03.aquaristic_validator.service;

import com.github.filipt03.aquaristic_validator.dto.EquipmentOption;
import com.github.filipt03.aquaristic_validator.model.backward.GoalRequirement;
import com.github.filipt03.aquaristic_validator.model.backward.ProofResult;
import com.github.filipt03.aquaristic_validator.model.backward.ProofResultNode;
import com.github.filipt03.aquaristic_validator.model.backward.SpeciesSupportTree;
import com.github.filipt03.aquaristic_validator.model.domain.*;
import com.github.filipt03.aquaristic_validator.model.inference.Penalty;

import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleEngineService {
    private static final Logger logger = LoggerFactory.getLogger(RuleEngineService.class);

    private final KieBase kieBase;
    private final List<FishData> fishDataList;
    private final List<EquipmentOption> filterOptions;
    private final List<EquipmentOption> heaterOptions;
    private final List<GoalRequirement> goals;
    private final SpeciesSupportService speciesSupportService;

    public RuleEngineService(SpeciesSupportService speciesSupportService) throws Exception {
        this.speciesSupportService = speciesSupportService;
        String waterRules  = FileService.compileTemplate("/data/water_parameters.csv", "/rules/templates/water_parameters.drt");
        String filterRules = FileService.compileTemplate("/data/filters.csv", "/rules/templates/filters.drt");
        String heaterRules = FileService.compileTemplate("/data/heaters.csv", "/rules/templates/heaters.drt");
        this.fishDataList  = FileService.loadFishData("/data/species.csv");
        this.filterOptions = FileService.loadEquipmentOptions("/data/filters.csv", "FilterModel", "MaxSupportedVolumeL");
        this.heaterOptions = FileService.loadEquipmentOptions("/data/heaters.csv", "HeaterModel", "MaxSupportedVolumeL");
        this.goals         = SpeciesSupportTree.createGoalTree();

        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(waterRules, ResourceType.DRL);
        kieHelper.addContent(filterRules, ResourceType.DRL);
        kieHelper.addContent(heaterRules, ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/traits.drl"), ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/hazards.drl"), ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/penalties.drl"), ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/states.drl"), ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/backward/species_support.drl"), ResourceType.DRL);

        this.kieBase = kieHelper.build();
    }

    public List<FishData> getFishDataList() {
        return fishDataList;
    }
    public List<EquipmentOption> getFilterOptions() { return filterOptions; }
    public List<EquipmentOption> getHeaterOptions() { return heaterOptions; }

    public ValidationResult validate(Aquarium tank, List<Fish> fishes) {
        KieSession kSession = kieBase.newKieSession();
        try {
            Map<Integer, Fish> condesedFishes = new HashMap<>();
            for (Fish f : fishes) {
                if (condesedFishes.containsKey(f.getSpeciesId())) {
                    Fish existing = condesedFishes.get(f.getSpeciesId());
                    existing.setQuantity(existing.getQuantity() + f.getQuantity());
                } else {
                    condesedFishes.put(f.getSpeciesId(), new Fish(f.getSpeciesId(), f.getSpecies(), f.getQuantity()));
                }
            }
            kSession.insert(tank);
            condesedFishes.values().forEach(kSession::insert);
            fishDataList.forEach(kSession::insert);
            kSession.setGlobal("logger", logger);
            kSession.fireAllRules();

            List<Penalty> penalties = kSession.getObjects(o -> o instanceof Penalty)
                .stream().map(o -> (Penalty) o)
                .sorted((o1, o2) -> {
                    int cmp = o1.getType().compareTo(o2.getType());
                    if (cmp != 0) return cmp;
                    return o1.getValue() - o2.getValue();
                }).toList();

            int score = 100;
            List<String> messages = new ArrayList<>();
            for (Penalty p : penalties) {
                score -= p.getValue();
                messages.add(p.getMessage());
            }
            if (score < 0) score = 0;
            return new ValidationResult(score, messages);
        } finally {
            kSession.dispose();
        }
    }

    public Map<String, List<ProofResultNode>> speciesSupport(int speciesId, KieSession kSession) {
        ProofResult result = speciesSupportService.isSupported(kSession, speciesId);
        return result.getTree();
    }

    public KieSession prepareSessionForSpeciesCheck(Aquarium tank, List<Fish> fishes) {
        KieSession kSession = kieBase.newKieSession();
        kSession.insert(tank);
        fishes.forEach(kSession::insert);
        fishDataList.forEach(kSession::insert);
        goals.forEach(kSession::insert);
        kSession.setGlobal("logger", logger);
        kSession.fireAllRules();
        return kSession;
    }

    public record ValidationResult(int score, List<String> messages) {}
}