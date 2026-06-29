package com.github.filipt03.aquaristic_validator.controller;

import com.github.filipt03.aquaristic_validator.dto.*;
import com.github.filipt03.aquaristic_validator.model.domain.FishData;
import com.github.filipt03.aquaristic_validator.service.RuleEngineService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ValidationController {
    private final RuleEngineService engine;

    public ValidationController(RuleEngineService engine) {
        this.engine = engine;
    }

    @PostMapping("/validate")
    public ValidationResponse validate(@RequestBody AquariumRequest request) {
        var result = engine.validate(request.toAquarium(), request.toFishList(engine.getFishDataList()));
        return new ValidationResponse(result.score(), result.messages());
    }

    @GetMapping("/species")
    public List<FishData> species() {
        return engine.getFishDataList();
    }

    @GetMapping("/filters")
    public List<EquipmentOption> filters() { return engine.getFilterOptions(); }

    @GetMapping("/heaters")
    public List<EquipmentOption> heaters() { return engine.getHeaterOptions(); }
}