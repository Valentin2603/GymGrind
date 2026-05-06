package gymgrind.model;

import javafx.scene.paint.Color;

public final class TrainingMachine extends GymObject {

    private final MachineType machineType;
    private final String placeholderMessage;

    public TrainingMachine(String name,
                           MachineType machineType,
                           Position position,
                           double width,
                           double height,
                           Color color,
                           String placeholderMessage) {
        super(name, position, width, height, color);
        this.machineType = machineType;
        this.placeholderMessage = placeholderMessage;
    }

    public MachineType machineType() {
        return machineType;
    }

    @Override
    public String shortTypeLabel() {
        return machineType.label();
    }

    @Override
    public String interact() {
        return placeholderMessage;
    }
}
