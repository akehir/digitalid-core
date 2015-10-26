package net.digitalid.service.core.cryptography;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.spec.IvParameterSpec;
import net.digitalid.service.core.annotations.BasedOn;
import net.digitalid.service.core.encoding.Encodable;
import net.digitalid.service.core.encoding.NonRequestingEncodingFactory;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.BytesWrapper;
import net.digitalid.service.core.wrappers.EncryptionWrapper;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;
import net.digitalid.utility.database.storing.AbstractStoringFactory;
import net.digitalid.utility.database.storing.Storable;

/**
 * The random initialization vector ensures that multiple {@link EncryptionWrapper encryptions} of the same {@link Block block} are different.
 */
@Immutable
public final class InitializationVector extends IvParameterSpec implements Encodable<InitializationVector, Object>, Storable<InitializationVector, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code initialization.vector@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("initialization.vector@core.digitalid.net").load(BytesWrapper.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Generator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns an array of 16 random bytes.
     * 
     * @return an array of 16 random bytes.
     * 
     * @ensure return.length == 16 : "The array contains 16 bytes.";
     */
    @Pure
    private static byte[] getRandomBytes() {
        final @Nonnull byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new initialization vector with the given bytes.
     * 
     * @param bytes the bytes of the new initialization vector.
     * 
     * @require bytes.length == 16 : "The array contains 16 bytes.";
     */
    private InitializationVector(@Nonnull byte[] bytes) {
        super(bytes);
        
        assert bytes.length == 16 : "The array contains 16 bytes.";
    }
    
    /**
     * Returns a new initialization vector with the given bytes.
     * 
     * @param bytes the bytes of the new initialization vector.
     * 
     * @return a new initialization vector with the given bytes.
     * 
     * @require bytes.length == 16 : "The array contains 16 bytes.";
     */
    @Pure
    public static @Nonnull InitializationVector get(@Nonnull byte[] bytes) {
        return new InitializationVector(bytes);
    }
    
    /**
     * Returns a random initialization vector.
     * 
     * @return a random initialization vector.
     */
    @Pure
    public static @Nonnull InitializationVector getRandom() {
        return get(getRandomBytes());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends NonRequestingEncodingFactory<InitializationVector,Object> {
        
        /**
         * Creates a new encoding factory with the given type.
         */
        private EncodingFactory() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull InitializationVector vector) {
            return BytesWrapper.encodeNonNullable(TYPE, vector.getIV());
        }
        
        @Pure
        @Override
        public @Nonnull InitializationVector decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("initialization.vector@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
            
            if (block.getLength() != 17) throw new InvalidEncodingException("An initialization vector has to be 16 bytes long.");
            return new InitializationVector(BytesWrapper.decodeNonNullable(block));
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    public static final @Nonnull EncodingFactory ENCODING_FACTORY = new EncodingFactory();
    
    @Pure
    @Override
    public @Nonnull EncodingFactory getEncodingFactory() {
        return ENCODING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for this class.
     */
    @Immutable
    public static final class StoringFactory extends AbstractStoringFactory<InitializationVector, Object> {
        
        /**
         * Creates a new storing factory.
         */
        private StoringFactory() {
            super(Column.get("vector", SQLType.VECTOR));
        }
        
        @Pure
        @Override
        public @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull InitializationVector vector) {
            return FreezableArray.getNonNullable(Block.toString(vector.getIV()));
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull InitializationVector vector, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setBytes(parameterIndex, vector.getIV());
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable InitializationVector restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable byte[] bytes = resultSet.getBytes(columnIndex);
            return bytes == null ? null : new InitializationVector(bytes);
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    public static final @Nonnull StoringFactory STORING_FACTORY = new StoringFactory();
    
    @Pure
    @Override
    public @Nonnull StoringFactory getStoringFactory() {
        return STORING_FACTORY;
    }
    
}
