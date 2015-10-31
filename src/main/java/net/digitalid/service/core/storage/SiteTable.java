package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class models a database table that contains part of an {@link Entity entity's} state.
 */
@Immutable
public abstract class SiteTable extends SiteTableImplementation<SiteModule> {
    
    /**
     * Creates a new site table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param dumpType the dump type of the new host table.
     * @param stateType the state type of the new site table.
     */
    protected SiteTable(@Nonnull SiteModule module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType dumpType, @Nonnull @Loaded SemanticType stateType) {
        super(module, name, dumpType, stateType);
        
        module.registerSiteStorage(this);
    }
    
}
