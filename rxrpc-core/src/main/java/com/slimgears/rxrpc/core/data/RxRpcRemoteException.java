package com.slimgears.rxrpc.core.data;

public class RxRpcRemoteException extends RuntimeException {
    private final ErrorInfo errorInfo;

    public RxRpcRemoteException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo.message(), cause);
        this.errorInfo = errorInfo;
    }

    public RxRpcRemoteException(ErrorInfo errorInfo) {
        this(errorInfo, null);
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
