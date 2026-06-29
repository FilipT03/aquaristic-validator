package com.github.filipt03.aquaristic_validator.model.backward;

import java.util.List;
import java.util.Map;

@lombok.Getter
@lombok.AllArgsConstructor
public class ProofResult {
    public final boolean satisfied;
    public final Map<String, List<ProofResultNode>> tree;

    public static ProofResult success() { return new ProofResult(true, Map.of()); }
    public static ProofResult failure(Map<String, List<ProofResultNode>> tree) { return new ProofResult(false, tree); }
}