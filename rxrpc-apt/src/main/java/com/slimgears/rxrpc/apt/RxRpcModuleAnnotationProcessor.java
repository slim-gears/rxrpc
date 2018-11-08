package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.apt.AbstractAnnotationProcessor;
import com.slimgears.apt.data.Environment;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.ServiceProviders;
import com.slimgears.rxrpc.core.RxRpcModule;
import com.slimgears.util.generic.Scope;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Set;

//@AutoService(Processor.class)
//@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcModule")
public class RxRpcModuleAnnotationProcessor extends AbstractAnnotationProcessor {
    private final Collection<ModuleGenerator> moduleGenerators = ServiceProviders.loadServices(ModuleGenerator.class);

    @Override
    protected boolean processAnnotation(TypeElement annotationType, RoundEnvironment roundEnv) {
//        ModuleGenerator.Context.Builder contextBuilder = ModuleGenerator.Context
//                .builder()
//                .processorClass(getClass())
//                .environment(processingEnv)
//                .sourceTypeElement(annotationType);
//
//        Scope.withScope(b -> b
//                .bind(ModuleGenerator.Context.Builder.class).toInstance(contextBuilder),
//                () -> super.processAnnotation(annotationType, roundEnv));
//
//        ModuleGenerator.Context context = contextBuilder.build();
//        moduleGenerators.forEach(g -> g.generate(context));
//
        return true;
    }

    @Override
    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
//        RxRpcModule annotation = typeElement.getAnnotation(RxRpcModule.class);
//        TypeInfo endpointClass = ElementUtils.typeFromAnnotation(annotation, RxRpcModule::endpointClass);
//        TypeInfo moduleClass = TypeInfo.of(typeElement);
//        Scope.resolve(ModuleGenerator.Context.Builder.class)
//                .addModule(annotation.name(), endpointClass, moduleClass);
        return true;
    }
}
