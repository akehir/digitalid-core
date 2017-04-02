package net.digitalid.core.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;

import net.digitalid.core.client.role.Role;
import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.pack.Pack;

/**
 * This type models an entry in the role table.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class CacheEntry extends RootClass {
    
    @Pure
    @PrimaryKey
    public abstract @Nonnull Role getRequester();
    
    @Pure
    @PrimaryKey
    public abstract @Nonnull InternalIdentity getRequestee();
    
    @Pure
    @PrimaryKey
    public abstract @Nonnull @AttributeType SemanticType getAttributeType();
    
    @Pure
    @PrimaryKey
    public abstract boolean isFound();
    
    @Pure
    public abstract @Nonnull Time getExpirationTime();
    
    @Pure
    public abstract @Nullable Pack getAttributeValue();
    
    // TODO (as soon as replies can be converted)
//    @Pure
//    public abstract @Nullable Reply<?> getReply();
    
}
