/**
 *
 */
package com.slimgears.rxrpc.apt.internal;

import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import java.util.Map;
import java.util.Optional;

public interface CodeGenerator<C extends CodeGenerator.Context> {
    void generate(C context);

    abstract class Context {
        private final Logger log = LoggerFactory.getLogger(getClass());

        public abstract ProcessingEnvironment environment();
        public abstract TypeElement sourceTypeElement();
        public abstract TypeInfo processorClass();
        public TemplateUtils utils() { return TemplateUtils.instance; }
        public TypeInfo sourceClass() {
            return TypeInfo.of(sourceTypeElement());
        }
        public Logger log() { return log; }
        public Map<String, String> options() {
            return environment().getOptions();
        }

        public Optional<String> option(String option) {
            return Optional.ofNullable(options().get(option));
        }

        public String option(String option, String defaultVal) {
            return option(option).orElse(defaultVal);
        }

        public boolean hasOption(String option) {
            return option(option).isPresent();
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
