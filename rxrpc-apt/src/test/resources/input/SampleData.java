package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcData;
import com.slimgears.rxrpc.sample.SampleEnum;

@RxRpcData
public class SampleData {
    public boolean isFinished() {
        return false;
    }
    public SampleEnum enumVal() {
        return SampleEnum.Val2;
    }
}
