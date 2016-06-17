package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;

/**
 * This exception is thrown when a signature is invalid.
 * 
 * @see InvalidHostSignatureException
 * @see InvalidClientSignatureException
 * @see InvalidCredentialsSignatureException
 */
@Immutable
public abstract class InvalidSignatureException extends SignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid signature exception.
     * 
     * @param signature the signature that is invalid.
     */
    protected InvalidSignatureException(@Nonnull SignatureWrapper signature) {
        super(signature);
    }
    
}