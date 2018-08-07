/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.data.TypeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

public class ImportTracker {
    private final Collection<String> imports = new TreeSet<>();

    public String[] imports() {
        return this.imports.toArray(new String[imports.size()]);
    }

    public String useClass(TypeInfo typeInfo) {
        return simplify(typeInfo).fullName();
    }

    public String useClass(String cls) {
        TypeInfo typeInfo = TypeInfoParser.parse(cls);
        return useClass(typeInfo);
    }

    private TypeInfo simplify(TypeInfo typeInfo) {
        imports.add(typeInfo.packageName() + "." + typeInfo.simpleName());
        TypeInfo.Builder builder = TypeInfo.builder().name(typeInfo.simpleName());
        typeInfo.typeParams().stream().map(this::simplify).forEach(builder::typeParam);
        return builder.build();
    }
}
