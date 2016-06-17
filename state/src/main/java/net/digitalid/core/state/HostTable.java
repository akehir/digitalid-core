package net.digitalid.core.state;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.declaration.Declaration;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.annotations.Loaded;

/**
 * This class models a database table that can be exported and imported on {@link Host hosts}.
 * 
 * @see ClientTable
 * @see SiteTable
 */
@Immutable
public abstract class HostTable extends HostTableImplementation<HostModule> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new host table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param declaration the declaration of the new table.
     * @param dumpType the dump type of the new host table.
     */
    protected HostTable(@Nonnull HostModule module, @Nonnull @Validated String name, @Nonnull Declaration declaration, @Nonnull @Loaded SemanticType dumpType) {
        super(module, name, declaration, dumpType);
        
        module.registerHostStorage(this);
    }
    
    /* -------------------------------------------------- Tables -------------------------------------------------- */
    
    @Pure
    @Override
    protected final boolean isTableFor(@Nonnull Site site) {
        return site instanceof Host;
    }
    
}