package com.github.filipt03.aquaristic_validator.model.backward;

@lombok.Getter
@lombok.AllArgsConstructor
public class Condition {
    private String field;
    private String value;
    private String operator;
    private boolean alwaysTrue = false;

    public static Condition trueCondition() { return new Condition("true", "true", "equals", true); }
}
