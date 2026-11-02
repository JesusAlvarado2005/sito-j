package dev.jj.projects.sync;

import java.time.Instant;
import java.util.Map;

/** Dati di un repository letti da GitHub/GitLab. languages = percentuale per linguaggio. */
public record RepoInfo(int stars, int forks, String primaryLanguage,
                       Map<String, Double> languages, Instant lastCommitAt) {

    /** Da byte per linguaggio a percentuali (1 decimale); il linguaggio primario è il più grande. */
    static Map<String, Double> toPercentages(Map<String, Long> bytes) {
        double total = bytes.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Double> result = new java.util.LinkedHashMap<>();
        bytes.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> result.put(e.getKey(), total == 0 ? 0.0 : Math.round(e.getValue() / total * 1000) / 10.0));
        return result;
    }

    static String primaryOf(Map<String, Double> languages) {
        return languages.isEmpty() ? null : languages.keySet().iterator().next();
    }
}
