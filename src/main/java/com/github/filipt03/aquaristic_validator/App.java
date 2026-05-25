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

        Aquarium tank = Aquarium.builder()
            .volumeLiters(60.0)
            .lengthCm(50.0)
            .widthCm(15.0)
            .ageInDays(5)
            .substrateType(SubstrateType.SHARP_GRAVEL)
            .build(); 
        
        Fish kuhliLoach = new Fish(
            "Pangio semicincta", 
            "Cobitidae", 
            7.0, 
            8
        );
        FishData kuhliLoachData = FishData.builder()
            .species("Pangio semicincta")
            .genus("Pangio")
            .family("Cobitidae")
            .ecology(List.of("benthic"))
            .behaviors(List.of())
            .habitats(List.of("freshwater"))
            .diet(List.of("omnivore"))
            .maxAdultLengthCm(10.0)
            .build();

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