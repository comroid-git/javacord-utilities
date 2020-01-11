// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.libs;

import java.util.function.Function;
import java.util.function.BinaryOperator;
import java.util.function.BiConsumer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.Set;

public class CustomCollectors
{
    public static final Set<Collector.Characteristics> CH_ID;
    public static final Set<Collector.Characteristics> CH_NOID;
    static final Set<Collector.Characteristics> CH_CONCURRENT_ID;
    static final Set<Collector.Characteristics> CH_CONCURRENT_NOID;
    static final Set<Collector.Characteristics> CH_UNORDERED_ID;
    
    public static Collector<Object, ?, String> toConcatenatedString(final Object splitWith) {
        return new CustomCollectorImpl<Object, Object, String>((Supplier<?>)StringBuilder::new, (left, right) -> {
            left.append(right.toString());
            left.append(splitWith.toString());
        }, (left, right) -> {
            left.append(right.toString());
            left.append(splitWith.toString());
            return left;
        }, sb -> {
            if (sb.length() > 1) {
                return sb.substring(0, sb.length() - splitWith.toString().length());
            }
            else {
                return "";
            }
        }, CustomCollectors.CH_NOID);
    }
    
    public static Collector<List<String>, ?, ArrayList<String>> listMerge() {
        return new CustomCollectorImpl<List<String>, Object, ArrayList<String>>((Supplier<?>)ArrayList::new, ArrayList::addAll, (left, right) -> {
            left.addAll(right);
            return left;
        }, CustomCollectors.CH_ID);
    }
    
    public static Collector<Integer, Integer, Integer> sumInteger() {
        return new CustomCollectorImpl<Integer, Integer, Integer>(() -> 0, Integer::sum, Integer::sum, CustomCollectors.CH_ID);
    }
    
    static {
        CH_ID = Collections.unmodifiableSet((Set<? extends Collector.Characteristics>)EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
        CH_NOID = Collections.emptySet();
        CH_CONCURRENT_ID = Collections.unmodifiableSet((Set<? extends Collector.Characteristics>)EnumSet.of(Collector.Characteristics.CONCURRENT, Collector.Characteristics.UNORDERED, Collector.Characteristics.IDENTITY_FINISH));
        CH_CONCURRENT_NOID = Collections.unmodifiableSet((Set<? extends Collector.Characteristics>)EnumSet.of(Collector.Characteristics.CONCURRENT, Collector.Characteristics.UNORDERED));
        CH_UNORDERED_ID = Collections.unmodifiableSet((Set<? extends Collector.Characteristics>)EnumSet.of(Collector.Characteristics.UNORDERED, Collector.Characteristics.IDENTITY_FINISH));
    }
    
    public static class CustomCollectorImpl<T, A, R> implements Collector<T, A, R>
    {
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;
        
        public CustomCollectorImpl(final Supplier<A> supplier, final BiConsumer<A, T> accumulator, final BinaryOperator<A> combiner, final Function<A, R> finisher, final Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }
        
        public CustomCollectorImpl(final Supplier<A> supplier, final BiConsumer<A, T> accumulator, final BinaryOperator<A> combiner, final Set<Characteristics> characteristics) {
            this((Supplier<Object>)supplier, (BiConsumer<Object, T>)accumulator, (BinaryOperator<Object>)combiner, castingIdentity(), characteristics);
        }
        
        private static <I, R> Function<I, R> castingIdentity() {
            return (Function<I, R>)(i -> i);
        }
        
        @Override
        public BiConsumer<A, T> accumulator() {
            return this.accumulator;
        }
        
        @Override
        public Supplier<A> supplier() {
            return this.supplier;
        }
        
        @Override
        public BinaryOperator<A> combiner() {
            return this.combiner;
        }
        
        @Override
        public Function<A, R> finisher() {
            return this.finisher;
        }
        
        @Override
        public Set<Characteristics> characteristics() {
            return this.characteristics;
        }
    }
}
