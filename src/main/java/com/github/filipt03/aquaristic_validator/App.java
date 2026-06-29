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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.drools.template.ObjectDataCompiler;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.Variable;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        String waterRules = compileTemplate("/data/water_parameters.csv", "/rules/templates/water_parameters.drt");
        String filterRules = compileTemplate("/data/filters.csv", "/rules/templates/filters.drt");
        String heaterRules = compileTemplate("/data/heaters.csv", "/rules/templates/heaters.drt");
        List<FishData> fishDataList = loadFishData("/data/species.csv");

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


    private static List<FishData> loadFishData(String speciesCsvPath) throws Exception {
        List<Map<String, String>> rows = parseCsv(speciesCsvPath);
        List<FishData> result = new ArrayList<>();

        for (Map<String, String> row : rows) {
            int speciesId = parseIntOrDefault(row.get("SpeciesId"), -1);
            String genus = row.getOrDefault("Genus", "").trim();
            String speciesName = row.getOrDefault("Species", "").trim();
            String fullSpeciesName = (genus + " " + speciesName).trim();

            FishData data = FishData.builder()
                .speciesId(speciesId)
                .species(fullSpeciesName)
                .genus(genus)
                .family(row.getOrDefault("Family", "").trim())
                .ecology(toListIfPresent(row.get("DemersPelag")))
                .airBreathing(row.getOrDefault("AirBreathing", "").trim())
                .behaviors(buildBehaviors(row))
                .habitats(List.of())
                .diet(List.of())
                .maxAdultLengthCm(parseDoubleOrNull(row.get("Length")))
                .trophicLevel(parseDoubleOrNull(row.get("FoodTroph")))
                .minGroupSize(parseIntOrNull(row.get("MinGroupSize")))
                .aggressionLevel(parseIntOrNull(row.get("AggressionLevel")))
                .tropical("1".equals(row.getOrDefault("Tropical", "0").trim()))
                .diet(toListIfPresent(row.get("Diet")))
                .build();

            result.add(data);
        }
        return result;
    }

    private static List<String> buildBehaviors(Map<String, String> row) {
        List<String> behaviors = new ArrayList<>();
        if ("1".equals(row.get("Schooling"))) behaviors.add("schooling");
        if ("1".equals(row.get("Shoaling"))) behaviors.add("shoaling");
        return behaviors;
    }

    private static List<String> toListIfPresent(String value) {
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split("[;]")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static Double parseDoubleOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<Map<String, String>> parseCsv(String csvPath) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(App.class.getResourceAsStream(csvPath)))) {

            List<Map<String, String>> dataList = new ArrayList<>();
            String headerLine = reader.readLine().replace("\uFEFF", "");
            String[] headers = headerLine.split("[,]");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.split("[,]").length < 2) continue;

                String[] cols = line.split("[,]", -1);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < cols.length; i++) {
                    row.put(headers[i].trim(), cols[i].trim());
                }
                dataList.add(row);
            }
            return dataList;
        }
    }

    private static String compileTemplate(String csvPath, String drtPath) throws Exception {
        List<Map<String, String>> rows = parseCsv(csvPath);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, Object> typedRow = new HashMap<>(row);
            dataList.add(typedRow);
        }

        InputStream templateStream = App.class.getResourceAsStream(drtPath);
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        return compiler.compile(dataList, templateStream);
    }
}