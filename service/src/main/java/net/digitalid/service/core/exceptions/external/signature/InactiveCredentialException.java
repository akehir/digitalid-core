package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.cryptography.credential.Credential;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

/**
 * This exception is thrown when a credential is inactive.
 */
@Immutable
public class InactiveCredentialException extends InactiveAuthenticationException {
    
    /* -------------------------------------------------- Credential -------------------------------------------------- */
    
    /**
     * Stores the credential that is inactive.
     */
    private final @Nonnull Credential credential;
    
    /**
     * Returns the credential that is inactive.
     * 
     * @return the credential that is inactive.
     */
    @Pure
    public @Nonnull Credential getCredential() {
        return credential;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new inactive credential exception.
     * 
     * @param credential the credential that is inactive.
     */
    protected InactiveCredentialException(@Nonnull Credential credential) {
        this.credential = credential;
    }
    
    /**
     * Returns a new inactive credential exception.
     * 
     * @param credential the credential that is inactive.
     * 
     * @return a new inactive credential exception.
     */
    @Pure
    public static @Nonnull InactiveCredentialException get(@Nonnull Credential credential) {
        return new InactiveCredentialException(credential);
    }
    
}
