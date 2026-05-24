package com.github.filipt03.aquaristic_validator;

import com.github.filipt03.aquaristic_validator.model.domain.Aquarium;
import com.github.filipt03.aquaristic_validator.model.domain.Fish;
import com.github.filipt03.aquaristic_validator.model.inference.Penalty;
import com.github.filipt03.aquaristic_validator.model.types.SubstrateType;

import java.util.Collection;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class App {
    public static void main(String[] args) {
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.newKieSession("aquariumKSession");

        Aquarium tank = new Aquarium(60.0, 5, SubstrateType.SHARP_GRAVEL);
        
        Fish kuhliLoach = new Fish(
            "Pangio semicincta", 
            "Cobitidae", 
            "benthopelagic", 
            10.0, 
            8
        );

        kSession.insert(tank);
        kSession.insert(kuhliLoach);

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

        System.out.println("Final Score: " + score);

        for (Penalty p : penalties) {
            System.out.println("\t" + p.getMessage());
        }
        
        kSession.dispose();
    }
}