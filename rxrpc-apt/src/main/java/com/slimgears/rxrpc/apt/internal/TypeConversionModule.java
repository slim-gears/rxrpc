/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.internal;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.Safe;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TypeConversionModule extends AbstractModule {
    @Override
    protected void configure() {
        convertToTypes(isArray(), arrayConverter(getProvider(Injector.class)));
        convertToTypes(is(Set.class), collectionConverter(getProvider(Injector.class), ImmutableSet::copyOf));
        convertToTypes(is(ImmutableSet.class), collectionConverter(getProvider(Injector.class), ImmutableSet::copyOf));
        convertToTypes(is(List.class), collectionConverter(getProvider(Injector.class), ImmutableList::copyOf));
        convertToTypes(is(ImmutableList.class), collectionConverter(getProvider(Injector.class), ImmutableList::copyOf));
        convertToTypes(is(Collection.class), collectionConverter(getProvider(Injector.class), ImmutableList::copyOf));
        convertToTypes(is(ImmutableCollection.class), collectionConverter(getProvider(Injector.class), ImmutableList::copyOf));
        convertToTypes(isOnly(TypeInfo.class), converter(TypeInfo::of));
        convertToTypes(isOnly(Pattern.class), converter(TypeConversionModule::fromWildcard));
        convertToTypes(isOnly(Path.class), converter(Paths::get));
        convertToTypes(isOnly(File.class), converter(File::new));
        convertToTypes(isOnly(URI.class), converter(URI::create));
        convertToTypes(isOnly(URL.class), converter(Safe.of(URL::new)));
    }

    private static Object convertArray(String value, TypeLiteral<?> toType) {
        return Stream.of(value.split(",")).map(String::trim).toArray(String[]::new);
    }

    interface CollectionFactory {
        <T> Collection<?> toCollection(Collection<? extends T> items);
    }

    private static TypeConverter collectionConverter(Provider<Injector> injector,
                                           CollectionFactory collectionFactory) {
        return (value, toType) -> {
            Type elementClass = ((ParameterizedType)toType.getType()).getActualTypeArguments()[0];
            TypeLiteral<?> elementType = TypeLiteral.get(elementClass);

            if (value.isEmpty()) {
                return collectionFactory.toCollection(Collections.emptyList());
            }

            String[] items = Stream.of(value.split(",")).map(String::trim).toArray(String[]::new);
            if (elementClass.equals(String.class)) {
                return collectionFactory.toCollection(Arrays.asList(items));
            }

            TypeConverter typeConverter = findConverter(elementType, injector.get());
            return collectionFactory.toCollection(Stream.of(items)
                    .map(str -> typeConverter.convert(str, elementType))
                    .collect(Collectors.toList()));
        };
    }

    private static TypeConverter arrayConverter(Provider<Injector> injector) {
        return (value, toType) -> {
            if (value == null) {
                return null;
            }

            Class<?> elementClass = toType.getRawType().getComponentType();
            Class<?> boxedElementClass = Primitives.wrap(elementClass);
            TypeLiteral<?> elementType = TypeLiteral.get(boxedElementClass);

            if (value.isEmpty()) {
                return Array.newInstance(elementClass, 0);
            }

            String[] items = Stream.of(value.split(",")).map(String::trim).toArray(String[]::new);

            if (elementClass.equals(String.class)) {
                return items;
            }

            TypeConverter typeConverter = findConverter(elementType, injector.get());
            return convertArray(items, elementClass, typeConverter);
        };
    }

    private static TypeConverter findConverter(TypeLiteral<?> toType, Injector injector) {
        return injector.getTypeConverterBindings()
                .stream()
                .filter(b -> b.getTypeMatcher().matches(toType))
                .map(TypeConverterBinding::getTypeConverter)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot covert string to " + toType.getRawType().getName()));
    }

    private static <T> Object convertArray(String[] items, Class<T> elementType, TypeConverter typeConverter) {
        TypeLiteral<?> elementTypeLiteral = TypeLiteral.get(elementType);
        //noinspection unchecked
        Object array = Array.newInstance(elementType, items.length);
        //noinspection unchecked
        IntStream.range(0, items.length)
                .forEach(i -> Array.set(array, i, typeConverter.convert(items[i], elementTypeLiteral)));
        return array;
    }

    public static Matcher<TypeLiteral<?>> is(Class<?> cls) {
        return new AbstractMatcher<TypeLiteral<?>>() {
            @Override
            public boolean matches(TypeLiteral<?> typeLiteral) {
                return typeLiteral.getRawType().equals(cls);
            }
        };
    }

    public static Matcher<TypeLiteral<?>> isArray() {
        return new AbstractMatcher<TypeLiteral<?>>() {
            @Override
            public boolean matches(TypeLiteral<?> typeLiteral) {
                return typeLiteral.getRawType().isArray();
            }
        };
    }

    public static TypeConverter converter(Function<String, ?> converter) {
        return (str, type) -> converter.apply(str);
    }

    public static Matcher<Object> isOnly(Class<?> cls) {
        return Matchers.only(TypeLiteral.get(cls));
    }

    private static Pattern fromWildcard(String wildcard) {
        return Pattern.compile(wildcard.replaceAll("\\*", ".*"));
    }
}
