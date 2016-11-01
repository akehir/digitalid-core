package net.digitalid.core.credential.utility;

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;

/**
 * This class models hashed or salted agent permissions.
 * 
 * @invariant (salt == null) == (permissions == null) : "The salt and the permissions are either both null or both non-null.";
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class HashedOrSaltedAgentPermissions extends RootClass {
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the salted permissions or null if the permissions are hidden.
     */
    @Pure
    public abstract @Nullable SaltedAgentPermissions getSaltedPermissions();
    
    /**
     * Returns whether the permissions are shown.
     */
    @Pure
    public boolean areShown() {
        return getSaltedPermissions() != null;
    }
    
    /**
     * Returns whether the permissions are hidden.
     */
    @Pure
    public boolean areHidden() {
        return getSaltedPermissions() == null;
    }
    
    /**
     * Returns the actual permissions.
     * 
     * @require areShown() : "The permissions are exposed.";
     */
    @Pure
    public @Nonnull ReadOnlyAgentPermissions getPermissions() {
        Require.that(areShown()).orThrow("The permissions have to be exposed.");
        
        return getSaltedPermissions().getPermissions();
    }
    
    /* -------------------------------------------------- Hash -------------------------------------------------- */
    
    /**
     * Returns the hash of the salted agent permissions.
     */
    @Pure
    protected abstract @Nullable BigInteger getStoredHash();
    
    @Pure
    protected @Nonnull BigInteger deriveHash() {
        try {
            return getStoredHash() != null ? getStoredHash() : new BigInteger(1, XDF.hash(getSaltedPermissions(), SaltedAgentPermissionsConverter.INSTANCE));
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception); // TODO: How to handle or propagate such exceptions?
        }
    }
    
    /**
     * Returns the hash of the salted agent permissions.
     */
    @Pure
    @Derive("deriveHash()")
    public abstract @Nonnull BigInteger getHash();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Validate.that((getSaltedPermissions() != null) != (getStoredHash() != null)).orThrow("Either the salted permissions or the hash is stored but not both.");
        super.validate();
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates new hashed or salted permissions with the given permissions.
     */
    @Pure
    public static @Nonnull HashedOrSaltedAgentPermissions with(@Nonnull @Frozen ReadOnlyAgentPermissions permissions, boolean exposed) throws ExternalException {
        if (exposed) {
            return new HashedOrSaltedAgentPermissionsSubclass(SaltedAgentPermissions.with(permissions), null);
        } else {
            return new HashedOrSaltedAgentPermissionsSubclass(null, new BigInteger(1, XDF.hash(SaltedAgentPermissions.with(permissions), SaltedAgentPermissionsConverter.INSTANCE)));
        }
    }
    
}
