package net.digitalid.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.storage.Storage;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.method.action.InternalAction;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.property.value.ValuePropertyInternalAction;
import net.digitalid.core.service.Service;

/**
 * Internal actions are used to synchronize properties across sites.
 * 
 * @see ValuePropertyInternalAction
 */
@Immutable
public abstract class PropertyInternalAction<E extends Entity, K, C extends Concept<E, K>, P extends SynchronizedProperty<E, K, C, ?, ?>> extends InternalAction {
    
    /* -------------------------------------------------- Property -------------------------------------------------- */
    
    /**
     * Returns the property that is modified by this internal action.
     */
    @Pure
    public abstract @Nonnull P getProperty();
    
    /* -------------------------------------------------- Handler -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull NonHostEntity getEntity() {
        return (NonHostEntity) getProperty().getConcept().getEntity();
    }
    
    @Pure
    @Override
    public @Nullable InternalIdentifier getProvidedSubject() {
        return getProperty().getConcept().getEntity().getIdentity().getAddress();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return getProperty().getTable().getActionType();
    }
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return getProperty().getTable().getParentModule().getService();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Synchronized the " + getProperty().getTable().getName() + " property of the " + getProperty().getTable().getParentModule().getName() + " concept of the identity " + getProperty().getConcept().getEntity().getIdentity().getAddress().getString() + ".";
    }
    
    /* -------------------------------------------------- Method -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getRecipient() {
        try {
            return getService().getRecipient(getEntity());
        } catch (@Nonnull DatabaseException exception) {
            throw new RuntimeException(exception); // TODO: How to handle this? Maybe use @Derive instead once exceptions can be indicated.
        }
    }
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Storage getStorage() {
        return getProperty().getTable();
    }
    
}