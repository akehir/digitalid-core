package ch.virtualid.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.PublicKeyChain;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.BlockableObject;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the commitment of a client.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public class Commitment extends BlockableObject implements Immutable {
    
    /**
     * Stores the semantic type {@code commitment.client@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("commitment.client@virtualid.ch").load(TupleWrapper.TYPE, HostIdentity.IDENTIFIER, Time.TYPE, Element.TYPE);
    
    
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
    public Commitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull BigInteger value) throws InvalidEncodingException {
        this.host = host;
        this.time = time;
        this.publicKey = new PublicKeyChain(Client.getAttributeNotNullUnwrapped(host, PublicKeyChain.TYPE)).getKey(time);
        this.value = publicKey.getCompositeGroup().getElement(value);
    }
    
    /**
     * Creates a new commitment from the given block.
     * 
     * @param block the block containing the commitment.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    public Commitment(@Nonnull Block block) throws SQLException, InvalidEncodingException, FailedIdentityException {
        super(block);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.host = new HostIdentifier(elements.getNotNull(0)).getIdentity();
        this.time = new Time(elements.getNotNull(1));
        this.publicKey = new PublicKeyChain(Client.getAttributeNotNullUnwrapped(host, PublicKeyChain.TYPE)).getKey(time);
        this.value = publicKey.getCompositeGroup().getElement(new IntegerWrapper(elements.getNotNull(2)).getValue());
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected final @Nonnull Block encode() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(3);
        elements.set(0, host.getAddress().toBlock());
        elements.set(1, time.toBlock());
        elements.set(2, value.toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
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
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Commitment)) return false;
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
    
}
