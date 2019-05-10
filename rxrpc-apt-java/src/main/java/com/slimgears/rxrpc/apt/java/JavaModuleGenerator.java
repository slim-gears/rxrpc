package com.slimgears.rxrpc.apt.java;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.slimgears.apt.data.Environment;
import com.slimgears.apt.util.FileUtils;
import com.slimgears.rxrpc.apt.ModuleGenerator;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.util.stream.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@AutoService(ModuleGenerator.class)
public class JavaModuleGenerator implements ModuleGenerator {
    @Override
    public void generate(Context context) {
        context.modules().asMap()
                .forEach(this::writeModule);
        writeIndex(context.modules().values());
    }

    private void writeIndex(Iterable<ModuleInfo> modules) {
        String content = Streams.fromIterable(modules)
                .filter(moduleInfo -> moduleInfo.endpointMeta().generateServer())
                .map(module -> JavaEndpointGenerator.rxModuleFromEndpoint(module.endpointClass()).erasureName())
                .distinct()
                .collect(Collectors.joining(System.lineSeparator()));
        FileUtils.fileWriter(StandardLocation.CLASS_OUTPUT, "META-INF/services/" + EndpointRouter.Module.class.getName())
                .accept(content);
    }

    private void writeModule(String moduleName, Iterable<ModuleInfo> modules) {
        if (Strings.isNullOrEmpty(moduleName)) {
            return;
        }

        String content = Streams.fromIterable(modules)
                .filter(moduleInfo -> moduleInfo.endpointMeta().generateServer())
                .map(module -> JavaEndpointGenerator.rxModuleFromEndpoint(module.endpointClass()).erasureName())
                .distinct()
                .collect(Collectors.joining(System.lineSeparator()));
        FileUtils.fileWriter(StandardLocation.CLASS_OUTPUT, "META-INF/rxrpc-modules/" + moduleName).accept(content);
    }
}
