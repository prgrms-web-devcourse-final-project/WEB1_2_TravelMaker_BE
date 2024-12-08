package edu.example.wayfarer.util;

@FunctionalInterface
public interface BreakableBiConsumer<T, U> {
    boolean accept(T t, U u); // true 면 진행, false 면 종료
}
