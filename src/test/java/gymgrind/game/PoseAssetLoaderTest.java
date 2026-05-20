package gymgrind.game;

import gymgrind.player.PlayerProfiles;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoseAssetLoaderTest {

    @Test
    void loadsCompetitionPoseAssetsForEveryProfile() {
        PoseAssetLoader loader = new PoseAssetLoader();

        for (var profile : PlayerProfiles.all()) {
            CharacterPoseAssets assets = loader.loadFor(profile);
            assertNotNull(assets);
            assertNotNull(assets.natural().pose1());
            assertNotNull(assets.natural().pose2());
            assertNotNull(assets.natural().pose3());
            assertNotNull(assets.steroid().pose1());
            assertNotNull(assets.steroid().pose2());
            assertNotNull(assets.steroid().pose3());
            assertTrue(assets.natural().pose1().getWidth() > 0);
            assertTrue(assets.steroid().pose1().getHeight() > 0);
        }
    }

    @Test
    void trimsTransparentPaddingFromPoseImages() {
        BufferedImage source = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(4, 5, new Color(255, 120, 20, 255).getRGB());

        BufferedImage trimmed = PoseAssetLoader.trimTransparentPadding(source);

        assertEquals(1, trimmed.getWidth());
        assertEquals(1, trimmed.getHeight());
        assertEquals(new Color(255, 120, 20, 255).getRGB(), trimmed.getRGB(0, 0));
    }

    @Test
    void removesLightBackdropConnectedToImageBorder() {
        BufferedImage source = new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB);
        int backdrop = new Color(242, 240, 236, 255).getRGB();
        int foreground = new Color(32, 180, 96, 255).getRGB();
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                source.setRGB(x, y, backdrop);
            }
        }
        source.setRGB(2, 2, foreground);
        source.setRGB(3, 2, foreground);
        source.setRGB(2, 3, foreground);
        source.setRGB(3, 3, foreground);

        BufferedImage cleaned = PoseAssetLoader.removeLightBackdrop(source);

        assertEquals(0, (cleaned.getRGB(0, 0) >>> 24) & 0xFF);
        assertEquals(foreground, cleaned.getRGB(2, 2));
    }
}
