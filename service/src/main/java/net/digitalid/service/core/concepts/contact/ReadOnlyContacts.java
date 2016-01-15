package net.digitalid.service.core.concepts.contact;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.Blockable;
import net.digitalid.service.core.database.SQLizable;
import net.digitalid.utility.validation.reference.Capturable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.collections.readonly.ReadOnlySet;

/**
 * This interface provides read-only access to {@link FreezableContacts contacts} and should <em>never</em> be cast away.
 * 
 * @see FreezableContacts
 */
public interface ReadOnlyContacts extends ReadOnlySet<Contact>, Blockable, SQLizable {
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableContacts clone();
    
}
