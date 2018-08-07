/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.data.TypeInfo;

import java.util.Collection;
import java.util.TreeSet;

public class ImportTracker {
    private final Collection<String> imports = new TreeSet<>();
    private final String selfPackageName;

    public static ImportTracker create(String selfPackageName) {
        return new ImportTracker(selfPackageName);
    }

    private ImportTracker(String selfPackageName) {
        this.selfPackageName = selfPackageName;
    }

    public String[] imports() {
        return this.imports.toArray(new String[imports.size()]);
    }

    public String use(TypeInfo typeInfo) {
        return simplify(typeInfo).fullName();
    }

    public String use(String cls) {
        TypeInfo typeInfo = TypeInfoParser.parse(cls);
        return use(typeInfo);
    }

    private TypeInfo simplify(TypeInfo typeInfo) {
        String packageName = typeInfo.packageName();
        if (!packageName.isEmpty() && !packageName.equals(selfPackageName)) {
            imports.add(packageName + "." + typeInfo.simpleName());
        }
        TypeInfo.Builder builder = TypeInfo.builder().name(typeInfo.simpleName());
        typeInfo.typeParams().stream().map(this::simplify).forEach(builder::typeParam);
        return builder.build();
    }
}
