package com.slimgears.rxrpc.apt.java;

import com.google.auto.service.AutoService;
import com.slimgears.apt.data.Environment;
import com.slimgears.rxrpc.apt.ModuleGenerator;
import com.slimgears.util.stream.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

@AutoService(ModuleGenerator.class)
public class JavaModuleGenerator implements ModuleGenerator {
    private final static Logger log = LoggerFactory.getLogger(JavaModuleGenerator.class);

    @Override
    public void generate(Context context) {
        context.modules().asMap().forEach(this::writeModule);
    }

    private void writeModule(String moduleName, Iterable<ModuleInfo> modules) {
        String filePath = "META-INF/rxrpc-modules/" + moduleName;
        log.info("Writing {}", filePath);
        Filer filer = Environment.instance().processingEnvironment().getFiler();
        try {
            FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", filePath);
            try (OutputStream stream = fileObject.openOutputStream();
                 PrintWriter printWriter = new PrintWriter(stream)) {
                Streams.fromIterable(modules)
                        .filter(moduleInfo -> moduleInfo.endpointMeta().generateServer())
                        .forEach(module -> {
                            String className = JavaEndpointGenerator.rxModuleFromEndpoint(module.endpointClass()).erasureName();
                            printWriter.println(className);
                            log.debug(className);
                        });
            }
        } catch (IOException e) {
            log.warn("Could not write file: {}", filePath);
        }
    }
}
