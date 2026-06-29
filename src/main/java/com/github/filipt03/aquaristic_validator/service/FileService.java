package com.github.filipt03.aquaristic_validator.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.template.ObjectDataCompiler;
import org.springframework.stereotype.Service;

import com.github.filipt03.aquaristic_validator.App;
import com.github.filipt03.aquaristic_validator.model.domain.FishData;

@Service
public class FileService {
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

    public static String compileTemplate(String csvPath, String drtPath) throws Exception {
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

    public static List<FishData> loadFishData(String speciesCsvPath) throws Exception {
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
                .commonName(row.getOrDefault("CommonName", "").trim())
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
}