package com.github.filipt03.aquaristic_validator;

import com.github.filipt03.aquaristic_validator.model.domain.Aquarium;
import com.github.filipt03.aquaristic_validator.model.domain.Fish;
import com.github.filipt03.aquaristic_validator.model.domain.FishData;
import com.github.filipt03.aquaristic_validator.model.inference.Penalty;
import com.github.filipt03.aquaristic_validator.model.types.SubstrateType;

import java.util.Collection;
import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.newKieSession("aquariumKSession");

        Aquarium tank = new Aquarium(60.0, 5, SubstrateType.SHARP_GRAVEL);
        
        Fish kuhliLoach = new Fish(
            "Pangio semicincta", 
            "Cobitidae", 
            7.0, 
            8
        );
        FishData kuhliLoachData = new FishData(
            "Pangio semicincta", 
            "Pangio", 
            "Cobitidae",
            List.of("benthic"),
            List.of(),
            List.of("freshwater"),
            List.of("omnivore"),
            10.0,
            null, 
            null,
            null, 
            null
        );

        kSession.insert(tank);
        kSession.insert(kuhliLoach);
        kSession.insert(kuhliLoachData);
        kSession.setGlobal("logger", logger);

        kSession.fireAllRules();

        Collection<Penalty> penalties = kSession
            .getObjects(obj -> obj instanceof Penalty)
            .stream()
            .map(obj -> (Penalty)obj)
            .toList();

        int score = 100;

        for (Penalty p : penalties) {
            score -= p.getValue();
        }

        logger.info("Final Score: {}", score);

        for (Penalty p : penalties) {
            logger.info("\t{}", p.getMessage());
        }
        
        kSession.dispose();
    }
}