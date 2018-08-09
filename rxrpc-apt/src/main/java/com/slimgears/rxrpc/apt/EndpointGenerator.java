/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.slimgears.rxrpc.apt.data.MethodInfo;
import com.slimgears.rxrpc.apt.internal.CodeGenerator;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.core.RxRpcEndpoint;

public interface EndpointGenerator extends CodeGenerator<EndpointGenerator.Context> {
    @AutoValue
    abstract class Context extends CodeGenerator.Context {
        public abstract RxRpcEndpoint meta();
        public abstract ImmutableList<MethodInfo> methods();

        public static Builder builder() {
            return new AutoValue_EndpointGenerator_Context.Builder();
        }

        @AutoValue.Builder
        public interface Builder extends CodeGenerator.Context.Builder<Context, Builder> {
            Builder meta(RxRpcEndpoint value);
            ImmutableList.Builder<MethodInfo> methodsBuilder();

            default Builder addMethod(MethodInfo method) {
                methodsBuilder().add(method);
                return this;
            }

            default Builder addMethods(Iterable<MethodInfo> methods) {
                methodsBuilder().addAll(methods);
                return this;
            }
        }
    }
}
