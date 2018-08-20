/**
 *
 */
package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.core.RxRpcGenerate;

@AutoValue
public abstract class MetaEndpointInfo implements HasName {
    public abstract RxRpcGenerate.Endpoint meta();
    public abstract TypeInfo superType();
    public abstract TypeInfo targetType();

    public static Builder builder() {
        return new AutoValue_MetaEndpointInfo.Builder();
    }

    @AutoValue.Builder
    public interface Builder extends InfoBuilder<MetaEndpointInfo>, HasName.Builder<Builder> {
        Builder meta(RxRpcGenerate.Endpoint meta);
        Builder superType(TypeInfo superType);
        Builder targetType(TypeInfo targetType);
    }
}
