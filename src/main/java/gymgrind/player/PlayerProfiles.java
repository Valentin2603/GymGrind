package gymgrind.player;

import gymgrind.shop.SupplementType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class PlayerProfiles {

    private static final PlayerProfile STREET_ROOKIE = new PlayerProfile(
            "street_rookie",
            "Тамик",
            "Сухой новичок с хорошей выносливостью. Ему нужно набирать массу.",
            "/assets/characters/tamik_preview.png",
            idleSprites("tamik"),
            walkSprites("tamik"),
            100,
            125,
            195,
            0,
            300,
            11,
            96,
            96,
            List.of(
                    new PlayerFormDefinition(
                            PlayerForm.SECOND,
                            "tamik_second",
                            128,
                            155,
                            205,
                            10.5,
                            null
                    ),
                    new PlayerFormDefinition(
                            PlayerForm.THIRD,
                            "tamik_third",
                            165,
                            205,
                            222,
                            10.0,
                            null
                    ),
                    new PlayerFormDefinition(
                            PlayerForm.FOURTH_STEROIDS,
                            "tamik_fourth_steroids",
                            215,
                            270,
                            238,
                            9.0,
                            SupplementType.RECOVERY_SHOT
                    )
            )
    );

    private static final PlayerProfile DARK_DRUN = new PlayerProfile(
            "dark_drun",
            "Тёмный друн",
            "Персонаж со средними статами, за него будет интересно прокачиваться на всех тренажёрах.",
            "/assets/characters/dark_drun_preview.png",
            idleSprites("dark_drun"),
            walkSprites("dark_drun"),
            210,
            245,
            115,
            0,
            300,
            30,
            96,
            96,
            List.of(
                    new PlayerFormDefinition(
                            PlayerForm.SECOND,
                            "dark_drun_second",
                            232,
                            270,
                            132,
                            27.0,
                            null
                    ),
                    new PlayerFormDefinition(
                            PlayerForm.THIRD,
                            "dark_drun_third",
                            260,
                            310,
                            158,
                            23.0,
                            null
                    ),
                    new PlayerFormDefinition(
                            PlayerForm.FOURTH_STEROIDS,
                            "dark_drun_fourth_steroids",
                            325,
                            395,
                            200,
                            18.0,
                            SupplementType.RECOVERY_SHOT
                    )
            )
    );

    private static final PlayerProfile FATTY_POPKA = new PlayerProfile(
            "fatty_popka",
            "Жирная Попка",
            "Тяжёлый силовик с мощным стартом по массе и силе, но с низкой выносливостью и высоким процентом жира.",
            "/assets/characters/fatty_popka_preview.png",
            idleSprites("fatty_popka"),
            walkSprites("fatty_popka"),
            280,
            315,
            85,
            0,
            300,
            52,
            96,
            96,
            List.of(
                    new PlayerFormDefinition(
                            PlayerForm.SECOND,
                            "fatty_popka_second",
                            300,
                            340,
                            110,
                            43.0,
                            null
                    ),
                    new PlayerFormDefinition(
                            PlayerForm.THIRD,
                            "fatty_popka_third",
                            325,
                            375,
                            140,
                            34.0,
                            null
                    ),
                    new PlayerFormDefinition(
                            PlayerForm.FOURTH,
                            "fatty_popka_fourth",
                            355,
                            415,
                            170,
                            25.0,
                            null
                    ),
                    new PlayerFormDefinition(
                            PlayerForm.FOURTH_STEROIDS,
                            "fatty_popka_fourth_steroids",
                            395,
                            465,
                            195,
                            19.0,
                            SupplementType.RECOVERY_SHOT
                    )
            )
    );

    private static final List<PlayerProfile> ALL = List.of(STREET_ROOKIE, DARK_DRUN, FATTY_POPKA);

    private PlayerProfiles() {
    }

    public static List<PlayerProfile> all() {
        return ALL;
    }

    public static PlayerProfile defaultProfile() {
        return STREET_ROOKIE;
    }

    public static PlayerProfile findById(String id) {
        return ALL.stream()
                .filter(profile -> profile.id().equals(id))
                .findFirst()
                .orElse(defaultProfile());
    }

    private static Map<PlayerDirection, String> idleSprites(String prefix) {
        Map<PlayerDirection, String> sprites = new EnumMap<>(PlayerDirection.class);
        sprites.put(PlayerDirection.FRONT, spritePath(prefix, "idle_front"));
        sprites.put(PlayerDirection.BACK, spritePath(prefix, "idle_back"));
        sprites.put(PlayerDirection.LEFT, spritePath(prefix, "idle_left"));
        sprites.put(PlayerDirection.RIGHT, spritePath(prefix, "idle_right"));
        return sprites;
    }

    private static Map<PlayerDirection, String> walkSprites(String prefix) {
        Map<PlayerDirection, String> sprites = new EnumMap<>(PlayerDirection.class);
        sprites.put(PlayerDirection.FRONT, spritePath(prefix, "walk_front"));
        sprites.put(PlayerDirection.BACK, spritePath(prefix, "walk_back"));
        sprites.put(PlayerDirection.LEFT, spritePath(prefix, "walk_left"));
        sprites.put(PlayerDirection.RIGHT, spritePath(prefix, "walk_right"));
        return sprites;
    }

    private static String spritePath(String prefix, String pose) {
        return "/assets/characters/" + prefix + "_" + pose + ".png";
    }
}
