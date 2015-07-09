package net.digitalid.core.handler;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.client.AccountOpen;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.synchronizer.Audit;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * Actions affect the state of a digital identity and are thus always {@link Audit audited}.
 * The default is to sign them identity-based. If another behavior is desired, the method
 * {@link Method#send()} needs to be overridden. Actions are executed asynchronously.
 * 
 * @see InternalAction
 * @see ExternalAction
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class Action extends Method implements Auditable {
    
    /**
     * Creates an action that encodes the content of a packet for the given recipient about the given subject.
     * The entity is only null for {@link AccountOpen} which inherits directly from this class.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected Action(@Nullable NonHostEntity entity, @Nonnull InternalIdentifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject, recipient);
    }
    
    /**
     * Creates an action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasEntity() : "This method has an entity.";
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected Action(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) {
        super(entity, signature, recipient);
    }
    
    
    @Pure
    @Override
    public final boolean isLodged() {
        return true;
    }
    
    
    @Override
    @NonCommitting
    public abstract @Nullable ActionReply executeOnHost() throws PacketException, SQLException;
    
    
    /**
     * Executes this action on the client.
     * 
     * @throws SQLException if this handler cannot be executed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    @NonCommitting
    public abstract void executeOnClient() throws SQLException;
    
    /**
     * This method is executed after successful transmission.
     * 
     * @throws SQLException if this handler cannot be executed.
     */
    @NonCommitting
    public void executeOnSuccess() throws SQLException {}
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getAuditPermissions() {
        return FreezableAgentPermissions.NONE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return Restrictions.MIN;
    }
    
    @Pure
    @Override
    public @Nullable Agent getAuditAgent() {
        return null;
    }
    
    
    /**
     * Returns the module on which this action operates.
     * 
     * @return the module on which this action operates.
     */
    @Pure
    public abstract @Nonnull BothModule getModule();
    
    /**
     * Stores an empty list of modules.
     */
    private static final @Nonnull ReadOnlyList<BothModule> emptyList = new FreezableLinkedList<BothModule>().freeze();
    
    /**
     * Returns the modules that need to be reloaded and are thus suspended.
     * 
     * @return the modules that need to be reloaded and are thus suspended.
     */
    @Pure
    public @Nonnull ReadOnlyList<BothModule> suspendModules() {
        return emptyList;
    }
    
}
