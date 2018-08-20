/**
 *
 */
package com.slimgears.rxrpc.apt.internal;

import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public interface CodeGenerator<C extends CodeGenerator.Context> {
    void generate(C context);

    abstract class Context {
        private final Logger log = LoggerFactory.getLogger(getClass());
        private final static String configFileResourcePath = "/config.properties";


        public abstract ProcessingEnvironment environment();
        public abstract TypeElement sourceTypeElement();
        public abstract TypeInfo processorClass();
        public abstract Properties configProperties();
        public TemplateUtils utils() { return TemplateUtils.instance; }
        public TypeInfo sourceClass() {
            return TypeInfo.of(sourceTypeElement());
        }
        public DeclaredType sourceDeclaredType() {
            return ElementUtils.toDeclaredType(sourceTypeElement());
        }
        public Logger log() { return log; }
        public Map<String, String> options() {
            return environment().getOptions();
        }

        public String option(String option) {
            return Optional.ofNullable(options().get(option)).orElseGet(() -> configProperties().getProperty(option));
        }

        public boolean hasOption(String option) {
            return options().containsKey(option);
        }

        public interface Builder<C extends Context, B extends Builder<C, B>> {
            B sourceTypeElement(TypeElement value);
            B environment(ProcessingEnvironment env);
            B processorClass(TypeInfo processorType);
            B configProperties(Properties properties);

            default B processorClass(Class<? extends Processor> cls) {
                return processorClass(TypeInfo.of(cls));
            }
            default B configFromResource() {
                try (InputStream stream = getClass().getResourceAsStream(configFileResourcePath)) {
                    if (stream != null) {
                        Properties properties = new Properties();
                        properties.load(stream);
                        return configProperties(properties);
                    }
                } catch (IOException e) {
                    // load from System properties
                }
                return configProperties(System.getProperties());
            }

            C build();
        }
    }
}
