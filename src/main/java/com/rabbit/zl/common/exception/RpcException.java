package com.rabbit.zl.common.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * Rpc exception
 *
 * @author Vincent
 * Created  on 2017/11/12.
 */
@ToString(callSuper = true)
public final class RpcException extends RuntimeException{

    private static final long serialVersionUID = -4168884981656035910L;

    public static final byte UNKNOWN = 0;
    public static final byte SERVER_ERROR = 50;
    public static final byte SERVER_TIMEOUT = 51;

    @Getter private byte code;

    public RpcException(String message) {
        super(message);
    }

    public RpcException(byte code, String message) {
        super(message);
        this.code = code;
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
