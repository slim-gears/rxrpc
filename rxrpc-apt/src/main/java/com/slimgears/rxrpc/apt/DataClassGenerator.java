/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.slimgears.rxrpc.apt.data.PropertyInfo;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.internal.CodeGenerator;

public interface DataClassGenerator extends CodeGenerator<DataClassGenerator.Context> {
    @AutoValue
    abstract class Context extends CodeGenerator.Context {
        public abstract ImmutableList<PropertyInfo> properties();

        public static Builder builder() {
            return new AutoValue_DataClassGenerator_Context.Builder().configFromResource();
        }

        @AutoValue.Builder
        public interface Builder extends CodeGenerator.Context.Builder<Context, Builder> {
            ImmutableList.Builder<PropertyInfo> propertiesBuilder();

            default Builder property(String name, TypeInfo type) {
                return property(PropertyInfo.of(name, type));
            }

            default Builder property(PropertyInfo property) {
                propertiesBuilder().add(property);
                return this;
            }
        }
    }
}
