package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.ModuleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

@AutoService(ModuleGenerator.class)
public class TypeScriptModuleGenerator implements ModuleGenerator {
    private final static Logger log = LoggerFactory.getLogger(TypeScriptModuleGenerator.class);

    @Override
    public void generate(Context context) {
        context.modules().asMap()
                .forEach((key, endpoints) -> generateNgModule(context, key, endpoints));
    }

    public void generateNgModule(Context context, String moduleName, Collection<ModuleInfo> endpoints) {
        if (moduleName.isEmpty()) {
            return;
        }

        log.info("Generating module: {}", moduleName);

        String ngModuleName = toModuleClassName(moduleName);
        Collection<TypeInfo> typescriptEndpoints = endpoints.stream()
                .map(ModuleInfo::endpointClass)
                .map(GeneratedClassTracker.current()::resolveGeneratedEndpoint)
                .collect(Collectors.toList());

        NgModuleGenerator
                .create(context)
                .name(ngModuleName)
                .addEndpoints(typescriptEndpoints)
                .write();
    }

    private static String toClassName(String name, String suffix) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return name.endsWith(suffix) ? name : name + suffix;
    }

    public static String toModuleClassName(String moduleName) {
        return toClassName(moduleName, "Module");
    }
}
