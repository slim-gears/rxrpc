package com.slimgears.rxrpc.apt;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMultimap;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.internal.CodeGenerator;
import com.slimgears.rxrpc.core.RxRpcEndpoint;

public interface ModuleGenerator extends CodeGenerator<ModuleGenerator.Context> {
    @AutoValue
    abstract class ModuleInfo {
        public abstract String name();
        public abstract TypeInfo endpointClass();
        public abstract RxRpcEndpoint endpointMeta();

        public static ModuleInfo create(String name, TypeInfo endpointClass, RxRpcEndpoint endpointMeta) {
            return new AutoValue_ModuleGenerator_ModuleInfo(name, endpointClass, endpointMeta);
        }
    }

    @AutoValue
    abstract class Context extends CodeGenerator.Context {
        public abstract ImmutableMultimap<String, ModuleInfo> modules();

        public static Builder builder() {
            return new AutoValue_ModuleGenerator_Context.Builder();
        }

        @AutoValue.Builder
        public interface Builder extends CodeGenerator.Context.Builder<Context, Builder> {
            ImmutableMultimap.Builder<String, ModuleInfo> modulesBuilder();

            default void addModule(String moduleName, TypeInfo endpoint, RxRpcEndpoint endpointMeta) {
                modulesBuilder().put(moduleName, ModuleInfo.create(moduleName, endpoint, endpointMeta));
            }
        }
    }
}
