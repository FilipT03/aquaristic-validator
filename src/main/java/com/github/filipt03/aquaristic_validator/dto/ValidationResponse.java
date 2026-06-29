package com.github.filipt03.aquaristic_validator.dto;
import java.util.List;
public record ValidationResponse(int score, List<String> messages) {}