package gymgrind.model;

import javafx.scene.paint.Color;

public final class InteractiveZone extends GymObject {

    private final ZoneType zoneType;
    private final String placeholderMessage;

    public InteractiveZone(String name,
                           ZoneType zoneType,
                           Position position,
                           double width,
                           double height,
                           Color color,
                           String placeholderMessage) {
        super(name, position, width, height, color);
        this.zoneType = zoneType;
        this.placeholderMessage = placeholderMessage;
    }

    public ZoneType zoneType() {
        return zoneType;
    }

    @Override
    public String shortTypeLabel() {
        return zoneType.label();
    }

    @Override
    public String interact() {
        return placeholderMessage;
    }
}
