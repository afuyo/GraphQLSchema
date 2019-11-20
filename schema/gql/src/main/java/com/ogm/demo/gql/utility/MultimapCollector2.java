package com.ogm.demo.gql.utility;



import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class MultimapCollector2<T, K, V> implements Collector<T, MultiValuedMap<K, V>, MultiValuedMap<K, V>> {

    private final Function<T, K> keyGetter;
    private final Function<T, V> valueGetter;

    public MultimapCollector2(Function<T, K> keyGetter, Function<T, V> valueGetter) {
        this.keyGetter = keyGetter;
        this.valueGetter = valueGetter;
    }

    public static <T, K, V> MultimapCollector2<T, K, V> toMultimap(Function<T, K> keyGetter, Function<T, V> valueGetter) {
        return new MultimapCollector2<>(keyGetter, valueGetter);
    }

    public static <T, K, V> MultimapCollector2<T, K, T> toMultimap(Function<T, K> keyGetter) {
        return new MultimapCollector2<>(keyGetter, v -> v);
    }

    @Override
    public Supplier<MultiValuedMap<K, V>> supplier() { return ArrayListValuedHashMap::new;
    }

    @Override
    public BiConsumer<MultiValuedMap<K, V>, T> accumulator() {
        return (map, element) -> map.put(keyGetter.apply(element), valueGetter.apply(element));
    }

    @Override
    public BinaryOperator<MultiValuedMap<K, V>> combiner() {
        return (map1, map2) -> {
            map1.putAll(map2);
            return map1;
        };
    }

    @Override
    public Function<MultiValuedMap<K, V>, MultiValuedMap<K, V>> finisher() {
        return map -> map;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
    }
}
