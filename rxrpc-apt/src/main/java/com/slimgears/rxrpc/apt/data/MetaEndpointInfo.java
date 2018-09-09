/**
 *
 */
package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.slimgears.apt.data.HasName;
import com.slimgears.apt.data.InfoBuilder;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.rxrpc.core.RxRpcGenerate;

import java.util.HashMap;
import java.util.Map;

@AutoValue
public abstract class MetaEndpointInfo implements HasName {
    public abstract RxRpcGenerate.Endpoint meta();
    public abstract TypeInfo superType();
    public abstract TypeInfo targetType();
    public abstract ImmutableMap<String, String> options();

    public static Builder builder() {
        return new AutoValue_MetaEndpointInfo.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder implements InfoBuilder<MetaEndpointInfo>, HasName.Builder<Builder> {
        private final Map<String, String> options = new HashMap<>();

        public abstract Builder meta(RxRpcGenerate.Endpoint meta);
        public abstract Builder superType(TypeInfo superType);
        public abstract Builder targetType(TypeInfo targetType);
        protected abstract Builder options(ImmutableMap<String, String> options);
        protected abstract MetaEndpointInfo buildInternal();

        public Builder addOptions(Map<String, String> options) {
            this.options.putAll(options);
            return this;
        }

        @Override
        public MetaEndpointInfo build() {
            options(ImmutableMap.copyOf(options));
            return buildInternal();
        }
    }
}
