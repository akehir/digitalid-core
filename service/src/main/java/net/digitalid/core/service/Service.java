/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.collection.ReadOnlyCollection;
import net.digitalid.utility.collections.map.FreezableLinkedHashMapBuilder;
import net.digitalid.utility.collections.map.FreezableMap;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.storage.Module;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This class models a service of the Digital ID protocol.
 */
@Immutable
@GenerateConverter
public abstract class Service extends RootClass {
    
    /* -------------------------------------------------- Services -------------------------------------------------- */
    
    /**
     * Maps the services that are installed on this unit from their type.
     */
    private static final @Nonnull @NonFrozen FreezableMap<@Nonnull SemanticType, @Nonnull Service> services = FreezableLinkedHashMapBuilder.build();
    
    /**
     * Returns the services installed on this unit.
     */
    @Pure
    public static @Nonnull @NonFrozen ReadOnlyCollection<@Nonnull Service> getServices() {
        return services.values();
    }
    
    /**
     * Returns the service with the given type or throws a {@link RequestException} if no such service is found.
     */
    @Pure
    @Recover
    public static @Nonnull Service getService(@Nonnull SemanticType type) throws RequestException {
        final @Nullable Service service = services.get(type);
        if (service == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.SERVICE).withMessage(Strings.format("No service with the type $ was found.", type.getAddress().getString())).build(); }
        return service;
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Returns the type of this service.
     */
    @Pure
    public abstract @Nonnull SemanticType getType();
    
    /* -------------------------------------------------- Title -------------------------------------------------- */
    
    /**
     * Returns the title of this service.
     */
    @Pure
    @NonRepresentative
    public abstract @Nonnull String getTitle();
    
    /* -------------------------------------------------- Version -------------------------------------------------- */
    
    /**
     * Returns the version of this service.
     */
    @Pure
    @NonRepresentative
    public abstract @Nonnull String getVersion();
    
    /**
     * Returns the title with the version of this service.
     */
    @Pure
    public @Nonnull String getTitleWithVersion() {
        return getTitle() + " (" + getVersion() + ")";
    }
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * Returns the module of this service.
     */
    @Pure
    @NonRepresentative
    public abstract @Nonnull Module getModule();
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    protected void initialize() {
        services.put(getType(), this);
        super.initialize();
    }
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    // TODO: Provide somewhere a default implementation of the following methods for non-core services.
    
    /**
     * Returns the recipient of internal methods for the given entity.
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull HostIdentifier getRecipient(@Nonnull NonHostEntity entity) throws DatabaseException;
//        final @Nullable AttributeValue attributeValue = Attribute.get(role, getType()).getValue();
//        if (attributeValue == null) { throw DatabaseException.get("The role " + role.getIdentity().getAddress() + " has no attribute of type " + getType().getAddress() + "."); }
//        try {
//            return HostIdentifier.XDF_CONVERTER.decodeNonNullable(None.OBJECT, attributeValue.getContent());
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw DatabaseException.get("The attribute of type " + getType().getAddress() + " of the role " + role.getIdentity().getAddress() + " does not encode a host identifier.", exception);
//        }
//    }
    
    /**
     * Returns the recipient of external methods for the given subject.
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull HostIdentifier getRecipient(@Nonnull InternalPerson subject, @Nullable NonHostEntity entity) throws ExternalException;
//        return HostIdentifier.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getFreshAttributeContent(subject, role, getType(), false));
//    }
    
}
