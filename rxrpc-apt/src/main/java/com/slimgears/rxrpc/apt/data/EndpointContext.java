/**
 *
 */
package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.slimgears.rxrpc.apt.TemplateUtils;
import com.slimgears.rxrpc.core.RxRpcEndpoint;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

@AutoValue
public abstract class EndpointContext {
    public abstract RxRpcEndpoint meta();
    public abstract ImmutableList<MethodInfo> methods();
    public abstract TypeElement sourceTypeElement();
    public abstract TemplateUtils utils();
    public abstract ProcessingEnvironment environment();
    public ClassInfo sourceClass() {
        return ClassInfo.of(sourceTypeElement());
    }

    public static Builder builder() {
        return new AutoValue_EndpointContext.Builder();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder meta(RxRpcEndpoint value);
        Builder sourceTypeElement(TypeElement value);
        Builder utils(TemplateUtils value);
        Builder environment(ProcessingEnvironment env);
        ImmutableList.Builder<MethodInfo> methodsBuilder();

        default Builder addMethod(MethodInfo method) {
            methodsBuilder().add(method);
            return this;
        }

        default Builder addMethods(Iterable<MethodInfo> methods) {
            methodsBuilder().addAll(methods);
            return this;
        }

        EndpointContext build();
    }

    public void writeSourceFile(TypeInfo targetClass, String code) {
        try {
            JavaFileObject sourceFile = environment().getFiler().createSourceFile(targetClass.name(), sourceTypeElement());
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(code);
            }
        } catch (IOException e) {
            System.err.println(code);
            throw new RuntimeException(e);
        }
    }
}
