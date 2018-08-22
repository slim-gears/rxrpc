/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.apt.internal.CodeGenerator;

public interface CodeGenerationFinalizer extends CodeGenerator<CodeGenerationFinalizer.Context> {
    @AutoValue
    abstract class Context extends CodeGenerator.Context {
        public static Builder builder() {
            return new AutoValue_CodeGenerationFinalizer_Context.Builder();
        }

        @AutoValue.Builder
        public interface Builder extends CodeGenerator.Context.Builder<Context, Builder> {

        }
    }
}
