package gymgrind.player;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class PlayerProfiles {

    private static final PlayerProfile STREET_ROOKIE = new PlayerProfile(
            "street_rookie",
            "Тамик",
            "Сухой новичок с хорошей выносливостью. Ему проще жечь жир, чем быстро набирать массу.",
            "/assets/characters/tamik_preview.png",
            idleSprites(),
            walkSprites(),
            8,
            52,
            13,
            0,
            300,
            11,
            96,
            96
    );

    private static final List<PlayerProfile> ALL = List.of(STREET_ROOKIE);

    private PlayerProfiles() {
    }

    public static List<PlayerProfile> all() {
        return ALL;
    }

    public static PlayerProfile defaultProfile() {
        return STREET_ROOKIE;
    }

    private static Map<PlayerDirection, String> idleSprites() {
        Map<PlayerDirection, String> sprites = new EnumMap<>(PlayerDirection.class);
        sprites.put(PlayerDirection.FRONT, "/assets/characters/tamik_idle_front.png");
        sprites.put(PlayerDirection.BACK, "/assets/characters/tamik_idle_back.png");
        sprites.put(PlayerDirection.LEFT, "/assets/characters/tamik_idle_left.png");
        sprites.put(PlayerDirection.RIGHT, "/assets/characters/tamik_idle_right.png");
        return sprites;
    }

    private static Map<PlayerDirection, String> walkSprites() {
        Map<PlayerDirection, String> sprites = new EnumMap<>(PlayerDirection.class);
        sprites.put(PlayerDirection.FRONT, "/assets/characters/tamik_walk_front.png");
        sprites.put(PlayerDirection.BACK, "/assets/characters/tamik_walk_back.png");
        sprites.put(PlayerDirection.LEFT, "/assets/characters/tamik_walk_left.png");
        sprites.put(PlayerDirection.RIGHT, "/assets/characters/tamik_walk_right.png");
        return sprites;
    }
}
