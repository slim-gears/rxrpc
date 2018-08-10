package com.slimgears.rxrpc.apt.data;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface TypeConverter {
    TypeConverter empty = create(t -> false, t -> t);

    boolean canConvert(TypeInfo typeInfo);
    TypeInfo convert(TypeInfo typeInfo);

    default TypeConverter combineWith(TypeConverter converter) {
        return create(
                type -> canConvert(type) || converter.canConvert(type),
                type -> canConvert(type)
                        ? convert(type)
                        : converter.convert(type));
    }

    default TypeConverter combineWith(TypeConverter... converters) {
        return combineWith(ofMultiple(converters));
    }

    static TypeConverter ofMultiple(TypeConverter... converters) {
        return Stream.of(converters).reduce(TypeConverter::combineWith).orElse(empty);
    }

    static TypeConverter create(Predicate<TypeInfo> predicate, Function<TypeInfo, TypeInfo> converter) {
        return new TypeConverter() {
            @Override
            public boolean canConvert(TypeInfo typeInfo) {
                return predicate.test(typeInfo);
            }

            @Override
            public TypeInfo convert(TypeInfo typeInfo) {
                return (canConvert(typeInfo))
                        ? converter.apply(typeInfo)
                        : typeInfo;
            }
        };
    }
}
