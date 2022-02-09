package com.slimgears.rxrpc.sample;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.slimgears.rxrpc.sample.SampleData;
import com.slimgears.rxrpc.sample.SampleEnum;
import java.util.List;
import java.util.Map;

public class SampleMapData {
    public Map<String, SampleData> sampleMap() {
        return null;
    }

    public ImmutableMap<String, SampleData> sampleImmutableMap() {
        return null;
    }

    public ImmutableMap<SampleEnum, ImmutableList<SampleEnum>> sampleEnumMap() { return null; }
}
