package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.Category;
import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * This interface models a host identity.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public interface HostIdentity extends InternalIdentity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getAddress();
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nonnull Category getCategory() {
        return Category.HOST;
    }
    
}
