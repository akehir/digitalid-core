package net.digitalid.service.core.entity;

import net.digitalid.utility.database.site.Site;

import net.digitalid.service.core.factory.encoding.Encodable;
import javax.annotation.Nonnull;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.handler.Handler;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.utility.database.storing.Storable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * An entity captures the {@link Site site} and the {@link Identity identity} of a {@link Concept concept} or {@link Handler handler}.
 * 
 * @see EntityClass
 * @see HostEntity
 * @see NonHostEntity
 */
@Immutable
public interface Entity<E extends Entity<E>> extends Encodable<E, Object>, Storable<E, Object> {
    
    /**
     * Stores the aspect of the observed entity being created.
     */
    public static final @Nonnull Aspect CREATED = new Aspect(EntityClass.class, "created");
    
    /**
     * Stores the aspect of the observed entity being deleted.
     */
    public static final @Nonnull Aspect DELETED = new Aspect(EntityClass.class, "deleted");
    
    
    /**
     * Returns the site of this entity.
     * 
     * @return the site of this entity.
     */
    @Pure
    public @Nonnull Site getSite();
    
    /**
     * Returns the identity of this entity.
     * 
     * @return the identity of this entity.
     */
    @Pure
    public @Nonnull InternalIdentity getIdentity();
    
    /**
     * Returns the number that references this entity in the database.
     * 
     * @return the number that references this entity in the database.
     */
    @Pure
    public long getNumber();
    
    
    /**
     * Returns this entity as a {@link HostEntity}.
     * 
     * @return this entity as a {@link HostEntity}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link HostEntity}.
     */
    @Pure
    public @Nonnull HostEntity toHostEntity() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link NonHostEntity}.
     * 
     * @return this entity as a {@link NonHostEntity}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link NonHostEntity}.
     */
    @Pure
    public @Nonnull NonHostEntity toNonHostEntity() throws InvalidEncodingException;
    
    /**
     * Returns this entity as an {@link Account}.
     * 
     * @return this entity as an {@link Account}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link Account}.
     */
    @Pure
    public @Nonnull Account toAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link HostAccount}.
     * 
     * @return this entity as a {@link HostAccount}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link HostAccount}.
     */
    @Pure
    public @Nonnull HostAccount toHostAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link NonHostAccount}.
     * 
     * @return this entity as a {@link NonHostAccount}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link NonHostAccount}.
     */
    @Pure
    public @Nonnull NonHostAccount toNonHostAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link Role}.
     * 
     * @return this entity as a {@link Role}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link Role}.
     */
    @Pure
    public @Nonnull Role toRole() throws InvalidEncodingException;
    
}
