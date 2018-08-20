package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.sample.SampleEnum;

import javax.annotation.Nullable;
import java.util.Optional;

public class SampleOptionalData {
    public Optional<SampleEnum> optionalEnumVal() {
        return Optional.of(SampleEnum.Val1);
    }

    @Nullable public SampleEnum nullableEnumVal() {
        return SampleEnum.Val2;
    }
}
