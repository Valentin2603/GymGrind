package gymgrind.gym.objects;

import gymgrind.gym.Position;
import javafx.scene.paint.Color;

public final class WarehouseProp extends GymObject {

    private final String label;

    public WarehouseProp(String name,
                         String label,
                         Position position,
                         double width,
                         double height,
                         Color color) {
        super(name, position, width, height, color);
        this.label = label;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public String shortTypeLabel() {
        return label;
    }

    @Override
    public String interact() {
        return "";
    }
}
