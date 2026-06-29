package com.github.filipt03.aquaristic_validator.model.backward;

@lombok.Getter
@lombok.AllArgsConstructor
public class GoalRequirement {
    private String goal;
    private String requirement;
    @lombok.Setter
    private Condition condition;
    private String explanation;
    private boolean terminal;
}