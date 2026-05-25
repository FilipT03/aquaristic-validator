package com.github.filipt03.aquaristic_validator.model.report;
import java.util.ArrayList;
import java.util.List;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class ValidationReport {
    private int score = 100;
    private List<String> messages = new ArrayList<>();

    public void decreaseScore(int penalty) {
        this.score -= penalty;
        if (this.score < 0) this.score = 0;
    }
    public void addMessage(String msg) {
        this.messages.add(msg);
    }
}