/**
 *
 */
package com.slimgears.rxrpc.apt.internal;

import com.google.common.collect.ImmutableList;
import com.slimgears.apt.data.Environment;
import com.slimgears.apt.data.MethodInfo;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.util.stream.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public interface CodeGenerator<C extends CodeGenerator.Context> {
    void generate(C context);

    default String[] getSupportedOptions() {
        return Optional
                .ofNullable(this.getClass().getAnnotation(SupportedOptions.class))
                .map(SupportedOptions::value)
                .orElseGet(() -> new String[0]);
    }

    @SuppressWarnings("UnstableApiUsage")
    abstract class Context {
        private final Logger log = LoggerFactory.getLogger(getClass());

        public abstract ProcessingEnvironment environment();
        public abstract TypeElement sourceTypeElement();
        public abstract TypeInfo processorClass();
        public boolean isInterface() {
            return ElementUtils.isInterface(sourceTypeElement());
        }
        public TemplateUtils utils() { return TemplateUtils.instance; }
        public TypeInfo sourceClass() {
            return TypeInfo.of(sourceTypeElement());
        }
        public DeclaredType sourceDeclaredType() {
            return ElementUtils.toDeclaredType(sourceTypeElement());
        }
        public Logger log() { return log; }

        public String option(String option) {
            return Environment.instance().properties().get(option);
        }
        public Iterable<MethodInfo> sourceMethods() {
            return sourceTypeElement().getEnclosedElements()
                    .stream()
                    .flatMap(Streams.ofType(ExecutableElement.class))
                    .map(MethodInfo::of)
                    .collect(ImmutableList.toImmutableList());
        }

        public boolean hasOption(String option) {
            return Optional
                    .ofNullable(Environment.instance().properties().get(option))
                    .map(Boolean::valueOf)
                    .orElse(false);
        }

        public interface Builder<C extends Context, B extends Builder<C, B>> {
            B sourceTypeElement(TypeElement value);
            B environment(ProcessingEnvironment env);
            B processorClass(TypeInfo processorType);

            default B processorClass(Class<? extends Processor> cls) {
                return processorClass(TypeInfo.of(cls));
            }

            C build();
        }
    }
}
