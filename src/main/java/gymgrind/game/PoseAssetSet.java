package gymgrind.game;

import java.awt.image.BufferedImage;
import java.util.List;

public record PoseAssetSet(
        BufferedImage pose1,
        BufferedImage pose2,
        BufferedImage pose3
) {

    public PoseAssetSet {
        if (pose1 == null || pose2 == null || pose3 == null) {
            throw new IllegalArgumentException("All three pose images must be present.");
        }
    }

    public BufferedImage pose(int poseIndex) {
        return switch (Math.floorMod(poseIndex, 3)) {
            case 0 -> pose1;
            case 1 -> pose2;
            default -> pose3;
        };
    }

    public List<BufferedImage> all() {
        return List.of(pose1, pose2, pose3);
    }
}
