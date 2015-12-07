package net.digitalid.service.core.concepts.settings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.service.core.CoreService;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.ShortString;
import net.digitalid.service.core.block.wrappers.value.EmptyWrapper;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concept.ConceptIndex;
import net.digitalid.service.core.concept.ConceptSetup;
import net.digitalid.service.core.concept.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.service.core.concept.property.nonnullable.NonNullableConceptPropertySetup;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.property.RequiredAuthorization;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class models a password of a digital identity.
 */
public final class Settings extends Concept<Settings, NonHostEntity, Object> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new password that belongs to the given entity.
     * 
     * @param entity the entity to which the password belongs.
     */
    private Settings(@Nonnull NonHostEntity entity) {
        super(entity, None.OBJECT, SETUP);
    }
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * The factory for this class.
     */
    @Immutable
    private static final class Factory extends Concept.Factory<Settings, NonHostEntity, Object> {
        
        @Pure
        @Override
        public @Nonnull Settings create(@Nonnull NonHostEntity entity, @Nonnull Object key) {
            return new Settings(entity);
        }
        
    }
    
    /* -------------------------------------------------- ConceptIndex -------------------------------------------------- */
    
    /**
     * Stores the index of this concept.
     */
    private static final @Nonnull ConceptIndex<Settings, NonHostEntity, Object> INDEX = ConceptIndex.get(new Factory());
    
    /**
     * Returns a potentially cached password that might not yet exist in the database.
     * 
     * @param entity the entity to which the password belongs.
     * 
     * @return a new or existing password with the given entity.
     * 
     * @require !(entity instanceof Role) || ((Role) entity).isNative() : "If the entity is a role, it is native.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Settings get(@Nonnull NonHostEntity entity) {
        assert !(entity instanceof Role) || ((Role) entity).isNative() : "If the entity is a role, it is native.";
        
        return INDEX.get(entity, None.OBJECT);
    }
    
    /* -------------------------------------------------- Setup -------------------------------------------------- */
    
    /**
     * Stores the setup of this concept.
     */
    public static final @Nonnull ConceptSetup<Settings, NonHostEntity, Object> SETUP = ConceptSetup.get(CoreService.SERVICE, "settings", INDEX, EmptyWrapper.getValueConverters(EmptyWrapper.SEMANTIC), NonHostEntity.CONVERTERS);
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    /**
     * Stores the required authorization to set the property and see its changes.
     */
    public static final @Nonnull RequiredAuthorization<Settings> REQUIRED_AUTHORIZATION = new RequiredAuthorization<Settings>() {
        
        @Pure
        @Override
        public @Nonnull String getStateFilter(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) {
            return Database.toBoolean(restrictions.isClient());
        }
        
        @Pure
        @Override
        public @Nonnull ReadOnlyAgentPermissions getRequiredPermissions(@Nonnull Settings password) {
            return FreezableAgentPermissions.GENERAL_WRITE; // TODO
        }
        
    };
    
    /* -------------------------------------------------- Value Validator -------------------------------------------------- */
    
    /**
     * Stores the value validator of the password property.
     */
    // TODO: No longer necessary for the new class ShortString.
    public static final @Nonnull ValueValidator<String> VALUE_VALIDATOR = new ValueValidator<String>() {
        @Pure
        @Override
        public boolean isValid(@Nonnull String value) {
            return value.length() <= 50;
        }
    };
    
    /* -------------------------------------------------- Value Property -------------------------------------------------- */
    
    /**
     * Stores the setup of the password property.
     */
    private static final @Nonnull NonNullableConceptPropertySetup<ShortString, Settings, NonHostEntity> VALUE_PROPERTY_SETUP = NonNullableConceptPropertySetup.get(SETUP, "password", StringWrapper.getValueConverters(StringWrapper.SEMANTIC), REQUIRED_AUTHORIZATION, VALUE_VALIDATOR, "");
    
    /**
     * Stores the password of these settings.
     */
    public final @Nonnull NonNullableConceptProperty<ShortString, Settings, NonHostEntity> password = NonNullableConceptProperty.get(VALUE_PROPERTY_SETUP, this);
    
}
