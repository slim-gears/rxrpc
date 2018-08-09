/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.PropertyInfo;
import com.slimgears.rxrpc.apt.internal.AbstractAnnotationProcessor;
import com.slimgears.rxrpc.apt.util.JavaUtils;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.apt.util.TypeScriptUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcData")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(TypeScriptUtils.typeScriptOutputDirOption)
public class RxRpcDataAnnotationProcessor extends AbstractAnnotationProcessor<DataClassGenerator, DataClassGenerator.Context> {
    public RxRpcDataAnnotationProcessor() {
        super(DataClassGenerator.class);
    }

    @Override
    protected DataClassGenerator.Context createContext(TypeElement annotationType, TypeElement typeElement) {
        DataClassGenerator.Context.Builder builder = DataClassGenerator.Context.builder()
                .sourceTypeElement(typeElement)
                .environment(processingEnv);

        typeElement.getEnclosedElements()
                .stream()
                .map(PropertyInfo::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(builder::property);

        return builder.build();
    }
}
