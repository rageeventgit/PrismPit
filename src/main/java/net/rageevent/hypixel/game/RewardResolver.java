package net.rageevent.hypixel.game;

import lombok.RequiredArgsConstructor;
import net.rageevent.hypixel.game.combat.OperationType;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class RewardResolver<T extends Number> {

    private final Supplier<T> base;

    public double calculate(List<Modifier<T>> modifiers){
        double coefficient = 1d;
        double flats = 0d;
        double percents = 0d;
        for (Modifier<T> modifier : modifiers) {
            final double value = modifier.value().doubleValue();
            switch (modifier.operationType){
                case MULTIPLIER -> coefficient *= value;
                case FLAT -> flats += value;
                case PERCENT -> percents += value;
            }
        }
        return (base.get().doubleValue() + flats) * (1.0 + (percents / 100.0)) * coefficient;
    }
    public record Modifier<T extends Number>(OperationType operationType, T value) {

        public static <N extends Number> Modifier<N> percent(N value) {
            return new Modifier<>(OperationType.PERCENT,value);
        }
        public static <N extends Number> Modifier<N> flat(N value) {
            return new Modifier<>(OperationType.FLAT,value);
        }
        public static <N extends Number> Modifier<N> multiplier(N value) {
            return new Modifier<>(OperationType.MULTIPLIER,value);
        }
    }
}
