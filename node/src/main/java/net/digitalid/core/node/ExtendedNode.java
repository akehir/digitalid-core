package net.digitalid.core.node;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.node.context.Context;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.property.set.SetPropertyRequiredAuthorization;
import net.digitalid.core.property.set.SetPropertyRequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Node;
import net.digitalid.core.restrictions.RestrictionsBuilder;

/**
 * Description.
 */
@Immutable
@GenerateConverter
public abstract class ExtendedNode extends Node {
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the permissions.
     */
    public static final @Nonnull SetPropertyRequiredAuthorization<NonHostEntity, Long, Node, SemanticType> PERMISSIONS_AUTHORIZATION = SetPropertyRequiredAuthorizationBuilder.<NonHostEntity, Long, Node, SemanticType>withRequiredPermissionsToExecuteMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, false).freeze()).withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withWriteToNode(true).withNode(concept).build()).withRequiredPermissionsToSeeMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, false).freeze()).withRequiredRestrictionsToSeeMethod((concept, value) -> RestrictionsBuilder.withNode(concept).build()).build();
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the authentications.
     */
    public static final @Nonnull SetPropertyRequiredAuthorization<NonHostEntity, Long, Node, SemanticType> AUTHENTICATIONS_AUTHORIZATION = SetPropertyRequiredAuthorizationBuilder.<NonHostEntity, Long, Node, SemanticType>withRequiredPermissionsToExecuteMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, false).freeze()).withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withWriteToNode(true).withNode(concept).build()).withRequiredPermissionsToSeeMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, false).freeze()).withRequiredRestrictionsToSeeMethod((concept, value) -> RestrictionsBuilder.withNode(concept).build()).build();
    
    /* -------------------------------------------------- Supercontexts -------------------------------------------------- */
    
    /**
     * Returns the direct supercontexts of this node.
     */
    @Pure
    @NonCommitting
    public @Nonnull @NonFrozen ReadOnlySet<Context> getSupercontexts() throws DatabaseException {
        throw new RuntimeException("TODO");
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the node with the given key.
     */
    @Pure
    @Recover
    static @Nonnull ExtendedNode of(@Nonnull NonHostEntity entity, long key) {
        // TODO: Make it injectable? (Use the key to determine whether it is a context or a contact (either with ranges or even vs. uneven)?)
        return null;
    }
    
}