package net.digitalid.core.signature.host;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.exceptions.ExpiredSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureExceptionBuilder;

/**
 * This class signs the wrapped object as a host.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
@TODO(task = "I think the signing should not be part of the conversion. Otherwise, a signature (like a certificate) cannot be stored. Another solution might be to wrap signatures in packs so that their byte encoding can be accessed (or implement this here directly).", date = "2017-01-30", author = Author.KASPAR_ETTER)
public abstract class HostSignature<@Unspecifiable OBJECT> extends Signature<OBJECT> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * The converter for the generic object.
     */
    private final @Nonnull Converter<OBJECT, Void> objectConverter;
    
    /**
     * Constructs a host signature instance with the given object converter.
     */
    protected HostSignature(@Nonnull Converter<OBJECT, Void> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    /* -------------------------------------------------- Signer -------------------------------------------------- */
    
    /**
     * Returns the signer of this host signature.
     */
    @Pure
    public abstract @Nonnull InternalIdentifier getSigner();
    
    /* -------------------------------------------------- Signature Value -------------------------------------------------- */
    
    /**
     * Returns the signature value of the host signature, that is:
     * hash(time, subject, signer, object) ^ d % n;
     */
    @Pure
    public abstract @Nonnull BigInteger getSignatureValue();
    
    /* -------------------------------------------------- Hash -------------------------------------------------- */
    
    /**
     * Calculates the hash of the client signature content.
     */
    @Pure
    public @Nonnull BigInteger deriveHostSignatureContentHash() {
        return getContentHash(getTime(), getSubject(), objectConverter, getObject());
    }
    
    /**
     * Returns the hash of the host signature object.
     */
    @Pure
    @Derive("deriveHostSignatureContentHash()")
    public abstract @Nonnull BigInteger getHostSignatureContentHash();
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    @Override
    public void verifySignature() throws InvalidSignatureException, ExpiredSignatureException, RecoveryException {
        final @Nonnull PublicKey publicKey;
        try {
            publicKey = PublicKeyRetriever.retrieve(getSigner().getHostIdentifier(), TimeBuilder.build());
        } catch (@Nonnull ExternalException exception) {
            throw RecoveryExceptionBuilder.withMessage(Strings.format("Could not retrieve the public key of $.", getSigner().getHostIdentifier())).withCause(exception).build();
        }
        
        verifySignature(publicKey);
    }
    
    /**
     * Verifies the correctness of the host signature by using the given public key.
     * @throws InvalidSignatureException if the signature is not valid.
     */
    @Pure
    public void verifySignature(@Nonnull PublicKey publicKey) throws InvalidSignatureException {
        // TODO: do we not have to check whether the signature expired?
        final @Nonnull BigInteger computedHash = publicKey.getCompositeGroup().getElement(getSignatureValue()).pow(publicKey.getE()).getValue();
        if (!computedHash.equals(getHostSignatureContentHash())) {
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        }
    }
    
}

