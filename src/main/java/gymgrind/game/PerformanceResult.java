package gymgrind.game;

public record PerformanceResult(
        double techniqueScore,
        double charismaScore,
        double powerScore,
        double totalScore,
        int successfulPresses,
        int failedPresses,
        int maxCombo,
        double progressPercent,
        boolean passed
) {

    public PerformanceResult {
        techniqueScore = clampScore(techniqueScore);
        charismaScore = clampScore(charismaScore);
        powerScore = clampScore(powerScore);
        totalScore = clampScore(totalScore);
        progressPercent = Math.max(0.0, Math.min(100.0, progressPercent));
    }

    public String rankLabel() {
        if (totalScore >= 9.0) {
            return "Легенда сцены";
        }
        if (totalScore >= 7.5) {
            return "Фаворит публики";
        }
        if (totalScore >= 6.0) {
            return "Крепкий выход";
        }
        if (totalScore >= 4.0) {
            return "Нестабильное выступление";
        }
        return "Сырой дебют";
    }

    public String summary() {
        return passed
                ? "Выступление засчитано. Судьи готовы выставить оценки."
                : "Выступление вышло скомканным, но судьи всё равно выставят баллы.";
    }

    private static double clampScore(double value) {
        return Math.max(0.0, Math.min(10.0, roundToTenth(value)));
    }

    private static double roundToTenth(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
