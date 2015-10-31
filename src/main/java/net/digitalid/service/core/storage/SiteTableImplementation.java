package net.digitalid.service.core.storage;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class implements a database table from which one can get, add and remove an {@link Entity entity's} state.
 * 
 * @see SiteTable
 */
@Immutable
abstract class SiteTableImplementation<M extends DelegatingSiteStorageImplementation> extends HostTableImplementation<M> implements SiteStorage {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the state type of this table.
     */
    private final @Nonnull @Loaded SemanticType stateType;
    
    @Pure
    @Override
    public final @Nonnull @Loaded SemanticType getStateType() {
        return stateType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new site table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param dumpType the dump type of the new host table.
     * @param stateType the state type of the new site table.
     */
    protected SiteTableImplementation(@Nonnull M module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType dumpType, @Nonnull @Loaded SemanticType stateType) {
        super(module, name, dumpType);
        
        this.stateType = stateType;
        
        Storage.register(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public abstract @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws AbortException;
    
    @Locked
    @Override
    @NonCommitting
    public abstract void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException;
    
    @Locked
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + entity.getSite() + getName() + " WHERE entity = " + entity);
        } catch (@Nonnull SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
}
