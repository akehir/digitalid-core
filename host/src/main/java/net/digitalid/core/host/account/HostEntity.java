package net.digitalid.core.host.account;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.unit.CoreUnit;

/**
 * This interface models a host entity.
 * 
 * @see HostAccount
 */
@Immutable
@TODO(task = "Do we really want this interface as it serves no purpose other than naming consistency?", date = "2016-12-04", author = Author.KASPAR_ETTER)
public interface HostEntity<UNIT extends CoreUnit> extends Entity<UNIT> {
    
    @Pure
    @Override
    public @Nonnull HostIdentity getIdentity();
    
}
