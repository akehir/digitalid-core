package net.digitalid.core.client;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.conversion.None;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.cache.Cache;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.wrappers.value.integer.IntegerWrapper;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.IdentifierImplementation;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.IdentityImplementation;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.resolution.Mapper;
import net.digitalid.core.server.Server;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Blockable;
import net.digitalid.service.core.cryptography.Element;
import net.digitalid.service.core.cryptography.Exponent;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.database.SQLizable;

/**
 * This class models the commitment of a client.
 * 
 * @see SecretCommitment
 */
@Immutable
public class Commitment implements Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code host.commitment.client@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType HOST = SemanticType.map("host.commitment.client@core.digitalid.net").load(HostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code time.commitment.client@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TIME = SemanticType.map("time.commitment.client@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code value.commitment.client@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE = SemanticType.map("value.commitment.client@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code commitment.client@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("commitment.client@core.digitalid.net").load(TupleWrapper.XDF_TYPE, HOST, TIME, VALUE);
    
    
    /**
     * Stores the host at which this commitment was made.
     */
    private final @Nonnull HostIdentity host;
    
    /**
     * Stores the time at which this commitment was made.
     */
    private final @Nonnull Time time;
    
    /**
     * Stores the value of this commitment.
     */
    private final @Nonnull Element value;
    
    /**
     * Stores the public key of this commitment.
     */
    private final @Nonnull PublicKey publicKey;
    
    /**
     * Creates a new commitment with the given host, time and value.
     * 
     * @param host the host at which this commitment was made.
     * @param time the time at which this commitment was made.
     * @param value the value of this commitment.
     */
    @NonCommitting
    public Commitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull BigInteger value) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        this.host = host;
        this.time = time;
        this.publicKey = Cache.getPublicKeyChain(host).getKey(time);
        this.value = publicKey.getCompositeGroup().getElement(value);
    }
    
    /**
     * Creates a new commitment with the given host, time, value and public key.
     * 
     * @param host the host at which this commitment was made.
     * @param time the time at which this commitment was made.
     * @param value the value of this commitment.
     * @param publicKey the public key of this commitment.
     */
    Commitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull Element value, @Nonnull PublicKey publicKey) {
        this.host = host;
        this.time = time;
        this.value = value;
        this.publicKey = publicKey;
    }
    
    /**
     * Creates a new commitment from the given block.
     * 
     * @param block the block containing the commitment.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Locked
    @NonCommitting
    public Commitment(@Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        Require.that(block.getType().isBasedOn(TYPE)).orThrow("The block is based on the indicated type.");
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(3);
        final @Nonnull HostIdentifier identifier = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(0)).castTo(HostIdentifier.class);
        this.host = identifier.getIdentity();
        this.time = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(1));
        this.publicKey = (Server.hasHost(identifier) ? Server.getHost(identifier).getPublicKeyChain() : Cache.getPublicKeyChain(host)).getKey(time);
        this.value = publicKey.getCompositeGroup().getElement(IntegerWrapper.decodeNonNullable(elements.getNonNullable(2)));
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(3);
        elements.set(0, host.toBlock(HOST));
        elements.set(1, time.toBlock().setType(TIME));
        elements.set(2, value.toBlock().setType(VALUE));
        return TupleWrapper.encode(TYPE, elements.freeze());
    }
    
    
    /**
     * Returns the host at which this commitment was made.
     * 
     * @return the host at which this commitment was made.
     */
    @Pure
    public final @Nonnull HostIdentity getHost() {
        return host;
    }
    
    /**
     * Returns the time at which this commitment was made.
     * 
     * @return the time at which this commitment was made.
     */
    @Pure
    public final @Nonnull Time getTime() {
        return time;
    }
    
    /**
     * Returns the value of this commitment.
     * 
     * @return the value of this commitment.
     */
    @Pure
    public final @Nonnull Element getValue() {
        return value;
    }
    
    /**
     * Returns the public key of this commitment.
     * 
     * @return the public key of this commitment.
     */
    @Pure
    public final @Nonnull PublicKey getPublicKey() {
        return publicKey;
    }
    
    
    /**
     * Adds the given secret to this commitment.
     * 
     * @param secret the secret to be added.
     * 
     * @return the new secret commitment.
     */
    @Pure
    public final @Nonnull SecretCommitment addSecret(@Nonnull Exponent secret) throws RequestException {
        return new SecretCommitment(host, time, value, publicKey, secret);
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof Commitment)) { return false; }
        final @Nonnull Commitment other = (Commitment) object;
        return host.equals(other.host) && time.equals(other.time) && value.equals(other.value);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 97 * hash + host.hashCode();
        hash = 97 * hash + time.hashCode();
        hash = 97 * hash + value.hashCode();
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "[Host: " + host.getAddress() + ", Time: " + time.asDate() + "]";
    }
    
    
    /**
     * Stores the columns used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "host " + Mapper.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, value " + Block.FORMAT + " NOT NULL";
    
    /**
     * Stores the columns used to retrieve instances of this class from the database.
     */
    public static final @Nonnull String COLUMNS = "host, time, value";
    
    /**
     * Stores the condition used to retrieve instances of this class from the database.
     */
    public static final @Nonnull String CONDITION = "host = ? AND time = ? AND value = ?";
    
    /**
     * Stores the string used to update instances of this class in the database.
     */
    public static final @Nonnull String UPDATE = "host = ?, time = ?, value = ?";
    
    /**
     * Stores the foreign key constraints used by instances of this class.
     */
    public static final @Nonnull String REFERENCE = "FOREIGN KEY (host) " + Mapper.REFERENCE;
    
    /**
     * Returns the given columns of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param startIndex the start index of the columns containing the data.
     * 
     * @return the given columns of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Commitment get(@Nonnull ResultSet resultSet, int startIndex) throws DatabaseException {
        try {
            final @Nonnull HostIdentity host = IdentityImplementation.getNotNull(resultSet, startIndex + 0).castTo(HostIdentity.class);
            final @Nonnull Time time = Time.get(resultSet, startIndex + 1);
            final @Nonnull BigInteger value = IntegerWrapper.decodeNonNullable(Block.getNotNull(Element.TYPE, resultSet, startIndex + 2));
            return new Commitment(host, time, value);
        } catch (@Nonnull IOException | RequestException | ExternalException exception) {
            throw new SQLException("A problem occurred while retrieving a commitment.", exception);
        }
     }
    
    /**
     * Sets the parameters at the given start index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param startIndex the start index of the parameters to set.
     */
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, int startIndex) throws DatabaseException {
        host.set(preparedStatement, startIndex + 0);
        time.set(preparedStatement, startIndex + 1);
        value.toBlock().set(preparedStatement, startIndex + 2);
    }
    
}