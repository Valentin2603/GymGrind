package gymgrind.assets;

import java.net.URL;

public enum SpriteAsset {
    PLAYER("/gymgrind/assets/sprites/player/player.png"),
    COACH("/gymgrind/assets/sprites/npc/coach.png"),
    BENCH_PRESS("/gymgrind/assets/sprites/machines/bench_press.png"),
    SQUAT_RACK("/gymgrind/assets/sprites/machines/squat_rack.png"),
    TREADMILL("/gymgrind/assets/sprites/machines/treadmill.png"),
    DEADLIFT_PLATFORM("/gymgrind/assets/sprites/machines/deadlift_platform.png"),
    SHOP("/gymgrind/assets/sprites/zones/shop.png"),
    REST_ZONE("/gymgrind/assets/sprites/zones/rest_zone.png"),
    WORK_ZONE("/gymgrind/assets/sprites/zones/work_zone.png"),
    STAGE("/gymgrind/assets/sprites/zones/stage.png"),
    GYM_FLOOR("/gymgrind/assets/sprites/background/gym_floor.png");

    private final String path;

    SpriteAsset(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public URL url() {
        return SpriteAsset.class.getResource(path);
    }
}
