/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.slimgears.rxrpc.apt.data.MetaEndpointInfo;
import com.slimgears.rxrpc.apt.internal.CodeGenerator;
import com.slimgears.rxrpc.core.RxRpcGenerate;

public interface MetaEndpointGenerator extends CodeGenerator<MetaEndpointGenerator.Context> {
    @AutoValue
    abstract class Context extends CodeGenerator.Context {
        public abstract RxRpcGenerate meta();
        public abstract String moduleName();
        public abstract ImmutableList<MetaEndpointInfo> endpoints();

        public static Context.Builder builder() {
            return new AutoValue_MetaEndpointGenerator_Context.Builder();
        }

        @AutoValue.Builder
        public interface Builder extends CodeGenerator.Context.Builder<Context, Context.Builder> {
            Builder meta(RxRpcGenerate meta);
            Builder moduleName(String moduleName);
            ImmutableList.Builder<MetaEndpointInfo> endpointsBuilder();

            default Builder endpoint(MetaEndpointInfo endpoint) {
                endpointsBuilder().add(endpoint);
                return this;
            }
        }
    }
}
