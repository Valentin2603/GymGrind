package gymgrind.model;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class ActiveSupplements {

    private final Set<SupplementType> active = EnumSet.noneOf(SupplementType.class);

    public void activate(SupplementType supplementType) {
        active.add(supplementType);
    }

    public boolean has(SupplementType supplementType) {
        return active.contains(supplementType);
    }

    public void consume(SupplementType supplementType) {
        active.remove(supplementType);
    }

    public void clear() {
        active.clear();
    }

    public String labels() {
        if (active.isEmpty()) {
            return "нет";
        }

        return active.stream()
                .map(SupplementType::label)
                .collect(Collectors.joining(", "));
    }
}
