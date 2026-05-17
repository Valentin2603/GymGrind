package gymgrind.save;

import gymgrind.daily.DailyQuestSaveData;
import gymgrind.daily.DailyQuestType;
import gymgrind.game.LocationId;
import gymgrind.shop.SupplementType;
import gymgrind.training.MachineType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public final class SaveService {

    private static final String SAVE_DIR = ".gymgrind";
    private static final String SAVE_FILE = "save.properties";

    private final Path savePath;

    public SaveService() {
        this(Path.of(System.getProperty("user.home"), SAVE_DIR, SAVE_FILE));
    }

    SaveService(Path savePath) {
        this.savePath = savePath;
    }

    public Path savePath() {
        return savePath;
    }

    public boolean hasSave() {
        return Files.isRegularFile(savePath);
    }

    public boolean save(SaveData data) {
        Properties properties = new Properties();
        properties.setProperty("profileId", data.profileId());
        properties.setProperty("locationId", data.locationId().name());
        properties.setProperty("playerX", Double.toString(data.playerX()));
        properties.setProperty("playerY", Double.toString(data.playerY()));
        properties.setProperty("currentDay", Integer.toString(data.currentDay()));
        properties.setProperty("strength", Integer.toString(data.strength()));
        properties.setProperty("muscle", Integer.toString(data.muscle()));
        properties.setProperty("stamina", Integer.toString(data.stamina()));
        properties.setProperty("fatigue", Integer.toString(data.fatigue()));
        properties.setProperty("money", Integer.toString(data.money()));
        properties.setProperty("bodyFat", Double.toString(data.bodyFat()));
        properties.setProperty("activeSupplements", encodeSupplements(data.activeSupplements()));
        writeDailyQuestData(properties, data.dailyQuests());

        try {
            Files.createDirectories(savePath.getParent());
            try (Writer writer = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
                properties.store(writer, "Gym Grind save");
            }
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public Optional<SaveData> load() {
        if (!hasSave()) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return Optional.of(new SaveData(
                    properties.getProperty("profileId", "street_rookie"),
                    LocationId.valueOf(properties.getProperty("locationId", LocationId.HOME.name())),
                    readDouble(properties, "playerX", 0),
                    readDouble(properties, "playerY", 0),
                    readInt(properties, "currentDay", 1),
                    readInt(properties, "strength", 0),
                    readInt(properties, "muscle", 0),
                    readInt(properties, "stamina", 0),
                    readInt(properties, "fatigue", 0),
                    readInt(properties, "money", 0),
                    readDouble(properties, "bodyFat", 12.0),
                    decodeSupplements(properties.getProperty("activeSupplements", "")),
                    readDailyQuestData(properties)
            ));
        } catch (IOException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String encodeSupplements(Set<SupplementType> supplements) {
        return supplements.stream()
                .map(SupplementType::name)
                .collect(Collectors.joining(","));
    }

    private void writeDailyQuestData(Properties properties, DailyQuestSaveData data) {
        if (data == null || !data.hasData()) {
            return;
        }

        properties.setProperty("dailyQuestTypes", data.quests().stream()
                .map(entry -> entry.type().name())
                .collect(Collectors.joining(",")));
        properties.setProperty("dailyQuestProgress", data.quests().stream()
                .map(entry -> Integer.toString(entry.progress()))
                .collect(Collectors.joining(",")));
        properties.setProperty("dailyQuestCompleted", data.quests().stream()
                .map(entry -> Boolean.toString(entry.completed()))
                .collect(Collectors.joining(",")));

        DailyQuestSaveData.ProgressData progress = data.progress();
        properties.setProperty("dailyProfileId", progress.profileId());
        properties.setProperty("dailyStartStrength", Integer.toString(progress.startStrength()));
        properties.setProperty("dailyStartMuscle", Integer.toString(progress.startMuscle()));
        properties.setProperty("dailyStartStamina", Integer.toString(progress.startStamina()));
        properties.setProperty("dailyStartForm", Integer.toString(progress.startForm()));
        properties.setProperty("dailyNextFormDecade", Integer.toString(progress.nextFormDecade()));
        properties.setProperty("dailyTrainingCount", Integer.toString(progress.trainingCount()));
        properties.setProperty("dailyLightTrainingCount", Integer.toString(progress.lightTrainingCount()));
        properties.setProperty("dailyMediumTrainingCount", Integer.toString(progress.mediumTrainingCount()));
        properties.setProperty("dailyHeavyTrainingCount", Integer.toString(progress.heavyTrainingCount()));
        properties.setProperty("dailyHeavyNoFailCount", Integer.toString(progress.heavyNoFailCount()));
        properties.setProperty("dailyExcellentCount", Integer.toString(progress.excellentCount()));
        properties.setProperty("dailyNoFailTrainingCount", Integer.toString(progress.noFailTrainingCount()));
        properties.setProperty("dailyWorkCount", Integer.toString(progress.workCount()));
        properties.setProperty("dailyMoneyEarned", Integer.toString(progress.moneyEarned()));
        properties.setProperty("dailyRestCount", Integer.toString(progress.restCount()));
        properties.setProperty("dailyRestoredFatigue", Integer.toString(progress.restoredFatigue()));
        properties.setProperty("dailyMaxFatigue", Integer.toString(progress.maxFatigue()));
        properties.setProperty("dailyMinFatigue", Integer.toString(progress.minFatigue()));
        properties.setProperty("dailyStrengthGained", Integer.toString(progress.strengthGained()));
        properties.setProperty("dailyMuscleGained", Integer.toString(progress.muscleGained()));
        properties.setProperty("dailyStaminaGained", Integer.toString(progress.staminaGained()));
        properties.setProperty("dailyFormGained", Integer.toString(progress.formGained()));
        properties.setProperty("dailyAnyStatGained", Integer.toString(progress.anyStatGained()));
        properties.setProperty("dailyCardioCount", Integer.toString(progress.cardioCount()));
        properties.setProperty("dailyStrengthTrainingCount", Integer.toString(progress.strengthTrainingCount()));
        properties.setProperty("dailyHeavyStrengthTrainingCount", Integer.toString(progress.heavyStrengthTrainingCount()));
        properties.setProperty("dailyFailedTrainingToday", Boolean.toString(progress.failedTrainingToday()));
        properties.setProperty("dailyOvertrainedToday", Boolean.toString(progress.overtrainedToday()));
        properties.setProperty("dailySpentMoney", Boolean.toString(progress.spentMoney()));
        properties.setProperty("dailyBoughtSupplement", Boolean.toString(progress.boughtSupplement()));
        properties.setProperty("dailyBoughtProtein", Boolean.toString(progress.boughtProtein()));
        properties.setProperty("dailyBoughtCreatine", Boolean.toString(progress.boughtCreatine()));
        properties.setProperty("dailyBoughtPreWorkout", Boolean.toString(progress.boughtPreWorkout()));
        properties.setProperty("dailyBoughtEnergy", Boolean.toString(progress.boughtEnergy()));
        properties.setProperty("dailyBoughtAndTrained", Boolean.toString(progress.boughtAndTrained()));
        properties.setProperty("dailyUsedSupplement", Boolean.toString(progress.usedSupplement()));
        properties.setProperty("dailyWorkNormalOrBetter", Boolean.toString(progress.workNormalOrBetter()));
        properties.setProperty("dailyWorkingLoadUp", Boolean.toString(progress.workingLoadUp()));
        properties.setProperty("dailyFatReduced", Boolean.toString(progress.fatReduced()));
        properties.setProperty("dailyFormNextTen", Boolean.toString(progress.formNextTen()));
        properties.setProperty("dailyHighFatigueNoFail", Boolean.toString(progress.highFatigueNoFail()));
        properties.setProperty("dailyCardioAfterStrength", Boolean.toString(progress.cardioAfterStrength()));
        properties.setProperty("dailyStrengthAfterCardio", Boolean.toString(progress.strengthAfterCardio()));
        properties.setProperty("dailyLastTrainingWasCardio", Boolean.toString(progress.lastTrainingWasCardio()));
        properties.setProperty("dailyLastTrainingWasStrength", Boolean.toString(progress.lastTrainingWasStrength()));
        properties.setProperty("dailyLastTamikTreadmillExcellent", Boolean.toString(progress.lastTamikTreadmillExcellent()));
        properties.setProperty("dailyDrunTreadmillNoFail", Boolean.toString(progress.drunTreadmillNoFail()));
        properties.setProperty("dailyDrunFatigueReducedUnder40", Boolean.toString(progress.drunFatigueReducedUnder40()));
        properties.setProperty("dailyStageAttempted", Boolean.toString(progress.stageAttempted()));
        properties.setProperty("dailyTrainedMachines", progress.trainedMachines().stream()
                .map(MachineType::name)
                .collect(Collectors.joining(",")));
    }

    private DailyQuestSaveData readDailyQuestData(Properties properties) {
        String questTypes = properties.getProperty("dailyQuestTypes", "");
        if (questTypes.isBlank() || properties.getProperty("dailyProfileId", "").isBlank()) {
            return DailyQuestSaveData.empty();
        }

        List<String> types = splitValues(questTypes);
        List<String> progressValues = splitValues(properties.getProperty("dailyQuestProgress", ""));
        List<String> completedValues = splitValues(properties.getProperty("dailyQuestCompleted", ""));
        List<DailyQuestSaveData.QuestEntry> quests = new java.util.ArrayList<>();
        for (int index = 0; index < types.size(); index++) {
            DailyQuestType type = DailyQuestType.valueOf(types.get(index));
            int questProgress = index < progressValues.size() ? Integer.parseInt(progressValues.get(index)) : 0;
            boolean completed = index < completedValues.size() && Boolean.parseBoolean(completedValues.get(index));
            quests.add(new DailyQuestSaveData.QuestEntry(type, questProgress, completed));
        }

        return new DailyQuestSaveData(quests, new DailyQuestSaveData.ProgressData(
                properties.getProperty("dailyProfileId", ""),
                readInt(properties, "dailyStartStrength", 0),
                readInt(properties, "dailyStartMuscle", 0),
                readInt(properties, "dailyStartStamina", 0),
                readInt(properties, "dailyStartForm", 0),
                readInt(properties, "dailyNextFormDecade", 10),
                readInt(properties, "dailyTrainingCount", 0),
                readInt(properties, "dailyLightTrainingCount", 0),
                readInt(properties, "dailyMediumTrainingCount", 0),
                readInt(properties, "dailyHeavyTrainingCount", 0),
                readInt(properties, "dailyHeavyNoFailCount", 0),
                readInt(properties, "dailyExcellentCount", 0),
                readInt(properties, "dailyNoFailTrainingCount", 0),
                readInt(properties, "dailyWorkCount", 0),
                readInt(properties, "dailyMoneyEarned", 0),
                readInt(properties, "dailyRestCount", 0),
                readInt(properties, "dailyRestoredFatigue", 0),
                readInt(properties, "dailyMaxFatigue", 0),
                readInt(properties, "dailyMinFatigue", 0),
                readInt(properties, "dailyStrengthGained", 0),
                readInt(properties, "dailyMuscleGained", 0),
                readInt(properties, "dailyStaminaGained", 0),
                readInt(properties, "dailyFormGained", 0),
                readInt(properties, "dailyAnyStatGained", 0),
                readInt(properties, "dailyCardioCount", 0),
                readInt(properties, "dailyStrengthTrainingCount", 0),
                readInt(properties, "dailyHeavyStrengthTrainingCount", 0),
                readBool(properties, "dailyFailedTrainingToday"),
                readBool(properties, "dailyOvertrainedToday"),
                readBool(properties, "dailySpentMoney"),
                readBool(properties, "dailyBoughtSupplement"),
                readBool(properties, "dailyBoughtProtein"),
                readBool(properties, "dailyBoughtCreatine"),
                readBool(properties, "dailyBoughtPreWorkout"),
                readBool(properties, "dailyBoughtEnergy"),
                readBool(properties, "dailyBoughtAndTrained"),
                readBool(properties, "dailyUsedSupplement"),
                readBool(properties, "dailyWorkNormalOrBetter"),
                readBool(properties, "dailyWorkingLoadUp"),
                readBool(properties, "dailyFatReduced"),
                readBool(properties, "dailyFormNextTen"),
                readBool(properties, "dailyHighFatigueNoFail"),
                readBool(properties, "dailyCardioAfterStrength"),
                readBool(properties, "dailyStrengthAfterCardio"),
                readBool(properties, "dailyLastTrainingWasCardio"),
                readBool(properties, "dailyLastTrainingWasStrength"),
                readBool(properties, "dailyLastTamikTreadmillExcellent"),
                readBool(properties, "dailyDrunTreadmillNoFail"),
                readBool(properties, "dailyDrunFatigueReducedUnder40"),
                readBool(properties, "dailyStageAttempted"),
                decodeMachines(properties.getProperty("dailyTrainedMachines", ""))
        ));
    }

    private Set<SupplementType> decodeSupplements(String value) {
        EnumSet<SupplementType> result = EnumSet.noneOf(SupplementType.class);
        if (value == null || value.isBlank()) {
            return result;
        }

        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .map(SupplementType::valueOf)
                .forEach(result::add);
        return result;
    }

    private Set<MachineType> decodeMachines(String value) {
        EnumSet<MachineType> result = EnumSet.noneOf(MachineType.class);
        if (value == null || value.isBlank()) {
            return result;
        }

        splitValues(value).stream()
                .map(MachineType::valueOf)
                .forEach(result::add);
        return result;
    }

    private List<String> splitValues(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    private int readInt(Properties properties, String key, int fallback) {
        return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)));
    }

    private double readDouble(Properties properties, String key, double fallback) {
        return Double.parseDouble(properties.getProperty(key, Double.toString(fallback)));
    }

    private boolean readBool(Properties properties, String key) {
        return Boolean.parseBoolean(properties.getProperty(key, "false"));
    }
}
