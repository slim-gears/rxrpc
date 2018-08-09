/**
 *
 */
package com.slimgears.rxrpc.apt.data;

import com.google.common.collect.ImmutableList;

import javax.lang.model.element.TypeParameterElement;
import java.util.Collection;
import java.util.stream.Stream;

public interface HasTypeParameters {
    ImmutableList<TypeParameterInfo> typeParams();

    interface Builder<B extends Builder<B>> {
        ImmutableList.Builder<TypeParameterInfo> typeParamsBuilder();

        default B typeParams(TypeInfo... types) {
            Stream.of(types)
                    .map(t -> TypeParameterInfo.of("", t))
                    .forEach(this::typeParam);
            //noinspection unchecked
            return (B)this;
        }

        default B typeParams(Collection<? extends TypeParameterElement> params) {
            params.stream().map(TypeParameterInfo::of).forEach(this::typeParam);
            //noinspection unchecked
            return (B)this;
        }

        default B typeParam(String name, TypeInfo type) {
            return typeParam(TypeParameterInfo.of(name, type));
        }

        default B typeParam(TypeParameterInfo param) {
            typeParamsBuilder().add(param);
            //noinspection unchecked
            return (B)this;
        }
    }
}
