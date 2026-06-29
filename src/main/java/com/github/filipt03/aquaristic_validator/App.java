package com.github.filipt03.aquaristic_validator;

import com.github.filipt03.aquaristic_validator.model.backward.GoalRequirement;
import com.github.filipt03.aquaristic_validator.model.backward.ProofResult;
import com.github.filipt03.aquaristic_validator.model.backward.ProofResultNode;
import com.github.filipt03.aquaristic_validator.model.backward.SpeciesSupportTree;
import com.github.filipt03.aquaristic_validator.model.domain.Aquarium;
import com.github.filipt03.aquaristic_validator.model.domain.Equipment;
import com.github.filipt03.aquaristic_validator.model.domain.Fish;
import com.github.filipt03.aquaristic_validator.model.domain.FishData;
import com.github.filipt03.aquaristic_validator.model.domain.WaterParameters;
import com.github.filipt03.aquaristic_validator.model.inference.Penalty;
import com.github.filipt03.aquaristic_validator.model.types.SubstrateType;
import com.github.filipt03.aquaristic_validator.service.SpeciesSupportService;
import com.github.filipt03.aquaristic_validator.service.FileService;

import java.util.*;

import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        String waterRules = FileService.compileTemplate("/data/water_parameters.csv", "/rules/templates/water_parameters.drt");
        String filterRules = FileService.compileTemplate("/data/filters.csv", "/rules/templates/filters.drt");
        String heaterRules = FileService.compileTemplate("/data/heaters.csv", "/rules/templates/heaters.drt");
        List<FishData> fishDataList = FileService.loadFishData("/data/species.csv");

        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(waterRules, ResourceType.DRL);
        kieHelper.addContent(filterRules, ResourceType.DRL);
        kieHelper.addContent(heaterRules, ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/traits.drl"), ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/hazards.drl"), ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/penalties.drl"), ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/states.drl"), ResourceType.DRL);
        kieHelper.addResource(ResourceFactory.newClassPathResource("rules/backward/species_support.drl"), ResourceType.DRL);

        KieSession kSession = kieHelper.build().newKieSession();
        
        WaterParameters waterParameters = new WaterParameters(8.0, 24.0, 10.0);
        Equipment equipment = new Equipment("AquaClear50", null, 100.0, false, false);
        Aquarium tank = Aquarium.builder()
            .volumeLiters(90.0)
            .lengthCm(50.0)
            .widthCm(15.0)
            .heightCm(140.0)
            .ageInDays(5)
            .substrateType(SubstrateType.SHARP_GRAVEL)
            .waterParameters(waterParameters)
            .equipment(equipment)
            .hasLivePlants(false)
            .build();

        Fish kuhliLoach = new Fish(1, "Pangio kuhlii", 7.0, 8);
        Fish africanCichlid = new Fish(30, "Labidochromis caeruleus", 12.0, 2);
        Fish woodEater = new Fish(13, "Hypostomus plecostomus", 12.0, 2);
        Fish plantEater = new Fish(14, "Xiphophorus maculatus", 12.0, 2);

        List<GoalRequirement> requirements = SpeciesSupportTree.createGoalTree();

        kSession.insert(tank);
        kSession.insert(kuhliLoach);
        kSession.insert(africanCichlid);
        kSession.insert(woodEater);
        kSession.insert(plantEater);
        fishDataList.forEach(kSession::insert);
        requirements.forEach(kSession::insert);
        kSession.setGlobal("logger", logger);

        kSession.fireAllRules();

        SpeciesSupportService supportService = new SpeciesSupportService();
        ProofResult proof = supportService.isSupported(kSession, africanCichlid.getSpeciesId());
        logger.info("SPECIES_SUPPORTED for {}: {}", africanCichlid.getSpecies(), proof.satisfied);
        for (Map.Entry<String, List<ProofResultNode>> entry : proof.tree.entrySet()) {
            logger.info("\t{}", entry.getKey());
            for (ProofResultNode node : entry.getValue()) {
                logger.info("\t\t{}: {}", node.getChild(), node.getExplanation());
            }
        }
        proof = supportService.isSupported(kSession, woodEater.getSpeciesId());
        logger.info("SPECIES_SUPPORTED for {}: {}", woodEater.getSpecies(), proof.satisfied);
        
        for (Map.Entry<String, List<ProofResultNode>> entry : proof.tree.entrySet()) {
            logger.info("\t{}", entry.getKey());
            for (ProofResultNode node : entry.getValue()) {
                logger.info("\t\t{}: {}", node.getChild(), node.getExplanation());
            }
        }proof = supportService.isSupported(kSession, plantEater.getSpeciesId());
        logger.info("SPECIES_SUPPORTED for {}: {}", plantEater.getSpecies(), proof.satisfied);
        
        for (Map.Entry<String, List<ProofResultNode>> entry : proof.tree.entrySet()) {
            logger.info("\t{}", entry.getKey());
            for (ProofResultNode node : entry.getValue()) {
                logger.info("\t\t{}: {}", node.getChild(), node.getExplanation());
            }
        }
        Collection<Penalty> penalties = kSession
            .getObjects(obj -> obj instanceof Penalty)
            .stream()
            .map(obj -> (Penalty) obj)
            .toList();

        int score = 100;
        for (Penalty p : penalties) score -= p.getValue();
        if (score < 0) score = 0;

        logger.info("Final Score: {}", score);
        for (Penalty p : penalties) logger.info("\t{}", p.getMessage());

        kSession.dispose();
    }
}