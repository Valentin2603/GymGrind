package gymgrind.game;

import gymgrind.player.PlayerProfile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PoseAssetLoader {

    private static final String COMPETITION_ROOT = "/assets/competition";
    private static final Map<String, CharacterAssetDescriptor> DESCRIPTORS = Map.of(
            "street_rookie", new CharacterAssetDescriptor(
                    "tamik",
                    new String[]{"tamik.png", "tamik2.png", "tamik3.png"},
                    new String[]{"sterTamik.png", "sterTamik2.png", "sterTamik3.png"}
            ),
            "dark_drun", new CharacterAssetDescriptor(
                    "drun",
                    new String[]{"drun1.png", "drun2.png", "drun3.png"},
                    new String[]{"sterDrun.png", "sterDrun2.png", "sterDrun3.png"}
            ),
            "fatty_popka", new CharacterAssetDescriptor(
                    "popa",
                    new String[]{"popa.png", "popa2.png", "popa3.png"},
                    new String[]{"sterpopa1.png", "sterpopa2.png", "sterpopa3.png"}
            )
    );

    private final Map<String, CharacterPoseAssets> cache = new HashMap<>();

    public CharacterPoseAssets loadFor(PlayerProfile profile) {
        return cache.computeIfAbsent(profile.id(), ignored -> loadCharacterAssets(profile));
    }

    public BufferedImage loadBackground(String fileName) {
        return loadBufferedImage(COMPETITION_ROOT + "/" + fileName, false);
    }

    private CharacterPoseAssets loadCharacterAssets(PlayerProfile profile) {
        CharacterAssetDescriptor descriptor = DESCRIPTORS.get(profile.id());
        if (descriptor == null) {
            throw new IllegalArgumentException("No competition pose assets configured for profile: " + profile.id());
        }

        PoseAssetSet naturalSet = new PoseAssetSet(
                loadPose(descriptor.folderName(), descriptor.naturalFiles()[0]),
                loadPose(descriptor.folderName(), descriptor.naturalFiles()[1]),
                loadPose(descriptor.folderName(), descriptor.naturalFiles()[2])
        );
        PoseAssetSet steroidSet = new PoseAssetSet(
                loadPose(descriptor.folderName(), descriptor.steroidFiles()[0]),
                loadPose(descriptor.folderName(), descriptor.steroidFiles()[1]),
                loadPose(descriptor.folderName(), descriptor.steroidFiles()[2])
        );
        return new CharacterPoseAssets(profile.id(), naturalSet, steroidSet);
    }

    private BufferedImage loadPose(String folderName, String fileName) {
        return loadBufferedImage(COMPETITION_ROOT + "/" + folderName + "/" + fileName, true);
    }

    private BufferedImage loadBufferedImage(String resourcePath, boolean trimTransparentPadding) {
        try (InputStream inputStream = PoseAssetLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing competition resource: " + resourcePath);
            }
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalStateException("Unreadable competition resource: " + resourcePath);
            }
            return trimTransparentPadding ? preparePoseImage(image) : image;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load competition resource: " + resourcePath, exception);
        }
    }

    /**
     * Competition pose sheets often arrive with a baked-in pale backdrop instead of real transparency.
     * Adjust the background predicate in {@link #isLightBackdropPixel(int)} if future characters need a looser mask.
     */
    public static BufferedImage preparePoseImage(BufferedImage image) {
        return trimTransparentPadding(removeLightBackdrop(image));
    }

    public static BufferedImage removeLightBackdrop(BufferedImage image) {
        BufferedImage prepared = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                prepared.setRGB(x, y, image.getRGB(x, y));
            }
        }

        boolean[][] visited = new boolean[prepared.getHeight()][prepared.getWidth()];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        enqueueBorderPixels(prepared, queue);

        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            int x = point[0];
            int y = point[1];
            if (x < 0 || y < 0 || x >= prepared.getWidth() || y >= prepared.getHeight() || visited[y][x]) {
                continue;
            }
            visited[y][x] = true;

            int argb = prepared.getRGB(x, y);
            if (!isLightBackdropPixel(argb)) {
                continue;
            }

            prepared.setRGB(x, y, argb & 0x00FFFFFF);
            queue.addLast(new int[]{x + 1, y});
            queue.addLast(new int[]{x - 1, y});
            queue.addLast(new int[]{x, y + 1});
            queue.addLast(new int[]{x, y - 1});
        }

        return prepared;
    }

    /**
     * Competition pose assets may contain large transparent borders.
     * Adjust this method if you want a looser or tighter crop.
     */
    public static BufferedImage trimTransparentPadding(BufferedImage image) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = (image.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        if (maxX < minX || maxY < minY) {
            return image;
        }

        BufferedImage trimmed = new BufferedImage(maxX - minX + 1, maxY - minY + 1, BufferedImage.TYPE_INT_ARGB);
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                trimmed.setRGB(x - minX, y - minY, image.getRGB(x, y));
            }
        }
        return trimmed;
    }

    private static void enqueueBorderPixels(BufferedImage image, ArrayDeque<int[]> queue) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < width; x++) {
            queue.addLast(new int[]{x, 0});
            queue.addLast(new int[]{x, height - 1});
        }
        for (int y = 1; y < height - 1; y++) {
            queue.addLast(new int[]{0, y});
            queue.addLast(new int[]{width - 1, y});
        }
    }

    private static boolean isLightBackdropPixel(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return false;
        }

        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        int brightness = (red + green + blue) / 3;
        int spread = Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue));

        return brightness >= 208 && spread <= 42;
    }

    private record CharacterAssetDescriptor(String folderName, String[] naturalFiles, String[] steroidFiles) {
        private CharacterAssetDescriptor {
            Objects.requireNonNull(folderName);
            Objects.requireNonNull(naturalFiles);
            Objects.requireNonNull(steroidFiles);
            if (naturalFiles.length != 3 || steroidFiles.length != 3) {
                throw new IllegalArgumentException("Each competition asset set must contain exactly 3 poses.");
            }
        }
    }
}
