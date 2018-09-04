package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.ModuleGenerator;

import java.util.Collection;
import java.util.stream.Collectors;

@AutoService(ModuleGenerator.class)
public class TypeScriptModuleGenerator implements ModuleGenerator {
    @Override
    public void generate(Context context) {
        context.modules().asMap()
                .forEach((key, endpoints) -> generateNgModule(context, key, endpoints));
    }

    public void generateNgModule(Context context, String moduleName, Collection<ModuleInfo> endpoints) {
        String ngModuleName = toClassName(moduleName, "Module");
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
        Preconditions.checkArgument(!name.isEmpty());
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return name.endsWith(suffix) ? name : name + suffix;
    }
}
