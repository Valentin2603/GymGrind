package gymgrind.player;

import java.util.EnumMap;
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

    public String walkSpritePath(PlayerDirection direction) {
        return walkSpritePaths.get(direction);
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
}
