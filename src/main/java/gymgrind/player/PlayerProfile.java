package gymgrind.player;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class PlayerProfile {

    private final String id;
    private final String displayName;
    private final String description;
    private final String previewSpritePath;
    private final Map<PlayerDirection, String> idleSpritePaths;
    private final Map<PlayerDirection, String> walkSpritePaths;
    private final int baseStrength;
    private final int baseMuscle;
    private final int baseStamina;
    private final int baseFatigue;
    private final int baseMoney;
    private final double baseBodyFat;
    private final double renderWidth;
    private final double renderHeight;
    private final List<PlayerFormDefinition> formProgression;

    public PlayerProfile(String id,
                         String displayName,
                         String description,
                         String previewSpritePath,
                         Map<PlayerDirection, String> idleSpritePaths,
                         Map<PlayerDirection, String> walkSpritePaths,
                         int baseStrength,
                         int baseMuscle,
                         int baseStamina,
                         int baseFatigue,
                         int baseMoney,
                         double baseBodyFat,
                         double renderWidth,
                         double renderHeight) {
        this(
                id,
                displayName,
                description,
                previewSpritePath,
                idleSpritePaths,
                walkSpritePaths,
                baseStrength,
                baseMuscle,
                baseStamina,
                baseFatigue,
                baseMoney,
                baseBodyFat,
                renderWidth,
                renderHeight,
                List.of()
        );
    }

    public PlayerProfile(String id,
                         String displayName,
                         String description,
                         String previewSpritePath,
                         Map<PlayerDirection, String> idleSpritePaths,
                         Map<PlayerDirection, String> walkSpritePaths,
                         int baseStrength,
                         int baseMuscle,
                         int baseStamina,
                         int baseFatigue,
                         int baseMoney,
                         double baseBodyFat,
                         double renderWidth,
                         double renderHeight,
                         List<PlayerFormDefinition> formProgression) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.previewSpritePath = previewSpritePath;
        this.idleSpritePaths = new EnumMap<>(idleSpritePaths);
        this.walkSpritePaths = new EnumMap<>(walkSpritePaths);
        this.baseStrength = baseStrength;
        this.baseMuscle = baseMuscle;
        this.baseStamina = baseStamina;
        this.baseFatigue = baseFatigue;
        this.baseMoney = baseMoney;
        this.baseBodyFat = baseBodyFat;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
        this.formProgression = List.copyOf(formProgression);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public String previewSpritePath() {
        return previewSpritePath;
    }

    public String idleSpritePath(PlayerDirection direction) {
        return idleSpritePaths.get(direction);
    }

    public String idleSpritePath(PlayerDirection direction, PlayerForm form) {
        if (form == PlayerForm.BASE) {
            return idleSpritePath(direction);
        }

        PlayerFormDefinition definition = formDefinition(form);
        if (definition == null) {
            return idleSpritePath(direction);
        }

        return spritePath(definition.spritePrefix(), "idle_" + directionSuffix(direction));
    }

    public String walkSpritePath(PlayerDirection direction) {
        return walkSpritePaths.get(direction);
    }

    public String walkSpritePath(PlayerDirection direction, PlayerForm form) {
        if (form == PlayerForm.BASE) {
            return walkSpritePath(direction);
        }

        PlayerFormDefinition definition = formDefinition(form);
        if (definition == null) {
            return walkSpritePath(direction);
        }

        return spritePath(definition.spritePrefix(), "walk_" + directionSuffix(direction));
    }

    public int baseStrength() {
        return baseStrength;
    }

    public int baseMuscle() {
        return baseMuscle;
    }

    public int baseStamina() {
        return baseStamina;
    }

    public int baseFatigue() {
        return baseFatigue;
    }

    public int baseMoney() {
        return baseMoney;
    }

    public double baseBodyFat() {
        return baseBodyFat;
    }

    public double renderWidth() {
        return renderWidth;
    }

    public double renderHeight() {
        return renderHeight;
    }

    public List<PlayerFormDefinition> formProgression() {
        return formProgression;
    }

    private PlayerFormDefinition formDefinition(PlayerForm form) {
        return formProgression.stream()
                .filter(definition -> definition.form() == form)
                .findFirst()
                .orElse(null);
    }

    private String directionSuffix(PlayerDirection direction) {
        return switch (direction) {
            case FRONT -> "front";
            case BACK -> "back";
            case LEFT -> "left";
            case RIGHT -> "right";
        };
    }

    private String spritePath(String prefix, String pose) {
        return "/assets/characters/" + prefix + "_" + pose + ".png";
    }
}
