package com.satispay.auth.satispayauthtest.exception;

/**
 * The SatispayException wraps all checked exception and enriches them with a custom error code.
 */
public class SatispayException extends Exception{

    private final ErrorCode code;

    public SatispayException(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return this.code;
    }

}
