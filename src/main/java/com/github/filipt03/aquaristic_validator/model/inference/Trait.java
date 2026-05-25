package com.github.filipt03.aquaristic_validator.model.inference;

import com.github.filipt03.aquaristic_validator.model.types.TraitType;

@lombok.Getter
@lombok.AllArgsConstructor
public class Trait {
    private String fishSpecies;
    private TraitType traitType;
}
