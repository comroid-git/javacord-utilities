package org.comroid.javacord.util.model.command;

import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

public interface SelfFuzzyComparable<Self extends SelfFuzzyComparable<Self>> {
    Self withFuzzyMatchingThreshold(@Nullable Function<Long, Integer> fuzzyMatchingThresholdFunction);

    Optional<? extends Function<Long, Integer>> getFuzzyMatchingThreshold();

    default Self removeFuzzyMatchingThreshold() {
        return withFuzzyMatchingThreshold(null);
    }
}
