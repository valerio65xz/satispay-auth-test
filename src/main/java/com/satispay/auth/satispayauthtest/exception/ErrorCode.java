package com.satispay.auth.satispayauthtest.exception;

/**
 * The ErrorCode of SatispayException. It has just 3 values:
 * <ul>
 *   <li>DIGEST_EXCEPTION: an Exception was thrown by MessageDigest</li>
 *   <li>HTTP_EXCEPTION: an Exception was thrown from http request creation process</li>
 *   <li>SIGNATURE_EXCEPTION: an Exception was thrown from signature process</li>
 * </ul>
 */
public enum ErrorCode {

    DIGEST_EXCEPTION,
    HTTP_EXCEPTION,
    SIGNATURE_EXCEPTION,

}
