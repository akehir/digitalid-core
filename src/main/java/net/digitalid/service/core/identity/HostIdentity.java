package net.digitalid.service.core.identity;

import net.digitalid.service.core.identity.resolution.Mapper;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.system.errors.InitializationError;

/**
 * This class models a host identity.
 */
@Immutable
public final class HostIdentity extends IdentityClass implements InternalIdentity {
    
    /**
     * Stores the semantic type {@code host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("host@core.digitalid.net").load(Identity.IDENTIFIER);
    
    
    /**
     * Creates a new host identity with the given identifier.
     * 
     * @param identifier the identifier of the new host identity.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     */
    private static @Nonnull HostIdentity create(@Nonnull HostIdentifier identifier) {
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        try {
            return Mapper.mapHostIdentity(identifier);
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The host identity with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Stores the host identity of {@code digitalid.net}.
     */
    public static final @Nonnull HostIdentity DIGITALID = HostIdentity.create(HostIdentifier.DIGITALID);
    
    
    /**
     * Stores the address of this host identity.
     */
    private final @Nonnull HostIdentifier address;
    
    /**
     * Creates a new host identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the address of the new host identity.
     */
    HostIdentity(long number, @Nonnull HostIdentifier address) {
        super(number);
        
        this.address = address;
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getAddress() {
        return address;
    }
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.HOST;
    }
    
    @Pure
    @Override
    @NonCommitting
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws AbortException {
        Mapper.unmap(this);
        throw exception;
    }
    
}
