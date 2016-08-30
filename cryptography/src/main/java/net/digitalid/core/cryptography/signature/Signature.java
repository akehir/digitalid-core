package net.digitalid.core.cryptography.signature;

import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.time.Time;

import net.digitalid.core.identification.identifier.InternalIdentifier;

/**
 *
 */
public abstract class Signature<T> {
    
    @Pure
    public abstract @Nullable InternalIdentifier getSubject();
    
    @Pure
    public abstract @Nullable Time getTime();
    
    @Pure
    public abstract @Nullable T getElement();
    
    // TODO: Do we need to add the Audit?!
    
}