package com.github.filipt03.aquaristic_validator.dto;
import java.util.List;

public record ValidationResponse(int score, String verdict, SeverityCounts issueCounts, List<String> messages) {
    public record SeverityCounts(int critical, int major, int moderate, int minor) {}
}