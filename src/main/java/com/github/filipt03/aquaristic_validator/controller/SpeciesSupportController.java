package com.github.filipt03.aquaristic_validator.controller;

import com.github.filipt03.aquaristic_validator.dto.AquariumRequest;
import com.github.filipt03.aquaristic_validator.model.backward.ProofResultNode;
import com.github.filipt03.aquaristic_validator.service.RuleEngineService;
import org.kie.api.runtime.KieSession;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpeciesSupportController {
    private final RuleEngineService engine;

    public SpeciesSupportController(RuleEngineService engine) {
        this.engine = engine;
    }

    @PostMapping("/species-support")
    public Map<String, List<ProofResultNode>> check(@RequestBody AquariumRequest request,
                                                      @RequestParam int speciesId) {
        var tank = request.toAquarium();
        var fishes = request.toFishList(engine.getFishDataList());
        KieSession kSession = engine.prepareSessionForSpeciesCheck(tank, fishes);
        try {
            return engine.speciesSupport(speciesId, kSession);
        } finally {
            kSession.dispose();
        }
    }
}