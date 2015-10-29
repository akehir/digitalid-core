package net.digitalid.service.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.IntegerWrapper;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.factory.encoding.NonRequestingEncodingFactory;
import net.digitalid.service.core.factory.storing.BlockBasedStoringFactory;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.storing.AbstractStoringFactory;

/**
 * An exponent is a number that raises elements of an arbitrary group.
 */
@Immutable
public final class Exponent extends Number<Exponent> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code exponent.group@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("exponent.group@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new exponent with the given value.
     * 
     * @param value the value of the new exponent.
     */
    private Exponent(@Nonnull BigInteger value) {
        super(value);
    }
    
    /**
     * Creates a new exponent with the given value.
     * 
     * @param value the value of the new exponent.
     * 
     * @return a new exponent with the given value.
     */
    @Pure
    public static @Nonnull Exponent get(@Nonnull BigInteger value) {
        return new Exponent(value);
    }
    
    /**
     * Creates a new exponent from the given block.
     * 
     * @param block the block that encodes the value of the new exponent.
     * 
     * @return a new exponent from the given block.
     */
    @Pure
    public static @Nonnull Exponent get(@Nonnull @BasedOn("exponent.group@core.digitalid.net") Block block) throws InvalidEncodingException {
        return new Exponent(IntegerWrapper.decodeNonNullable(block));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Operations –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Adds the given exponent to this exponent.
     * 
     * @param exponent the exponent to be added.
     * 
     * @return the sum of this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent add(@Nonnull Exponent exponent) {
        return new Exponent(getValue().add(exponent.getValue()));
    }
    
    /**
     * Subtracts the given exponent from this exponent.
     * 
     * @param exponent the exponent to be subtracted.
     * 
     * @return the difference between this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent subtract(@Nonnull Exponent exponent) {
        return new Exponent(getValue().subtract(exponent.getValue()));
    }
    
    /**
     * Multiplies this exponent with the given exponent.
     * 
     * @param exponent the exponent to be multiplied.
     * 
     * @return the product of this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent multiply(@Nonnull Exponent exponent) {
        return new Exponent(getValue().multiply(exponent.getValue()));
    }
    
    /**
     * Inverses this exponent in the given group.
     * 
     * @param group a group with known order.
     * 
     * @return the multiplicative inverse of this exponent.
     * 
     * @require group.getOrder().gcd(getValue()).compareTo(BigInteger.ONE) == 0 : "The exponent is relatively prime to the group order.";
     */
    @Pure
    public @Nonnull Exponent inverse(@Nonnull GroupWithKnownOrder group) {
        assert group.getOrder().gcd(getValue()).compareTo(BigInteger.ONE) == 0 : "The exponent is relatively prime to the group order.";
        
        return new Exponent(getValue().modInverse(group.getOrder()));
    }
    
    /**
     * Returns the next (or the same) relatively prime exponent.
     * 
     * @param group a group with known order.
     * 
     * @return the next (or the same) relatively prime exponent.
     */
    @Pure
    public @Nonnull Exponent getNextRelativePrime(@Nonnull GroupWithKnownOrder group) {
        @Nonnull BigInteger next = getValue();
        while (next.gcd(group.getOrder()).compareTo(BigInteger.ONE) == 1) next = next.add(BigInteger.ONE);
        return new Exponent(next);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends NonRequestingEncodingFactory<Exponent, Object> {
        
        /**
         * Creates a new encoding factory.
         */
        private EncodingFactory() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull Exponent exponent) {
            return IntegerWrapper.encodeNonNullable(TYPE, exponent.getValue());
        }
        
        @Pure
        @Override
        public @Nonnull Exponent decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("exponent.group@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
            
            return get(block);
        }
        
    }
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory ENCODING_FACTORY = new EncodingFactory();
    
    @Pure
    @Override
    public @Nonnull EncodingFactory getEncodingFactory() {
        return ENCODING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull AbstractStoringFactory<Exponent, Object> STORING_FACTORY = BlockBasedStoringFactory.get(ENCODING_FACTORY);
    
    @Pure
    @Override
    public @Nonnull AbstractStoringFactory<Exponent, Object> getStoringFactory() {
        return STORING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Factories<Exponent, Object> FACTORIES = Factories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
