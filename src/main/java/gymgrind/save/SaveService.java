package gymgrind.save;

import gymgrind.game.LocationId;
import gymgrind.shop.SupplementType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
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
                    decodeSupplements(properties.getProperty("activeSupplements", ""))
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

    private int readInt(Properties properties, String key, int fallback) {
        return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)));
    }

    private double readDouble(Properties properties, String key, double fallback) {
        return Double.parseDouble(properties.getProperty(key, Double.toString(fallback)));
    }
}
