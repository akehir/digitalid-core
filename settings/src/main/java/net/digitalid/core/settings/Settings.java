package net.digitalid.core.settings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.auxiliary.None;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.Database;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.concept.ConceptIndex;
import net.digitalid.core.concept.ConceptIndexBuilder;
import net.digitalid.core.concept.ConceptSetup;
import net.digitalid.core.concept.ConceptSetupBuilder;
import net.digitalid.core.concept.annotations.GenerateProperty;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.core.property.nonnullable.NonNullableConceptPropertySetup;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.service.Service;

/**
 * This class models the settings of a digital identity.
 */
@Immutable
@GenerateSubclass
@GenerateConverter // TODO: How can we get the entity to be externally provided?
public abstract class Settings extends Concept<NonHostEntity, Object> {
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    private static final @Nonnull ConceptIndex<Settings, NonHostEntity, Object> INDEX = ConceptIndexBuilder.withFactory((entity, key) -> new SettingsSubclass(entity)).build();
    
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
    @Recover
    @NonCommitting
    public static @Nonnull Settings of(@Nonnull NonHostEntity entity) {
        Require.that(!(entity instanceof Role) || ((Role) entity).isNative()).orThrow("If the entity is a role, it is native.");
        
        return INDEX.get(entity, None.OBJECT);
    }
    
    /* -------------------------------------------------- Setup -------------------------------------------------- */
    
    /**
     * Stores the setup of this concept.
     */
    public static final @Nonnull ConceptSetup<Settings, NonHostEntity, Object> SETUP = ConceptSetupBuilder.withService(Service.CORE).withConceptName("settings").withConceptIndex(INDEX).build(); //.get(CoreService.SERVICE, "settings", INDEX, EmptyWrapper.getValueConverters(EmptyWrapper.SEMANTIC), NonHostEntity.CONVERTERS);
    
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
    @Pure
    @GenerateProperty
    public @Nonnull NonNullableConceptProperty<@MaxSize(50) String, Settings, NonHostEntity> password() { return null; };// = NonNullableConceptProperty.get(VALUE_PROPERTY_SETUP, this);
    
}
