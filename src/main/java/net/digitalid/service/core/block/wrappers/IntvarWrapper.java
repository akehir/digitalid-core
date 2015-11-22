package net.digitalid.service.core.block.wrappers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.Encoding;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueSQLConverter;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueXDFConverter;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.entity.annotations.Matching;
import net.digitalid.service.core.exceptions.external.encoding.InvalidBlockLengthException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidBlockOffsetException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.math.NonNegative;
import net.digitalid.utility.annotations.reference.NonCapturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.annotations.size.NonEmpty;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.declaration.ColumnDeclaration;
import net.digitalid.utility.database.declaration.SQLType;

/**
 * This class wraps a {@code long} for encoding and decoding a block of the syntactic type {@code intvar@core.digitalid.net}.
 */
@Immutable
public final class IntvarWrapper extends ValueWrapper<IntvarWrapper> {
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores the maximum value an intvar can have.
     */
    public static final long MAX_VALUE = 4_611_686_018_427_387_903l;
    
    /**
     * Stores the value of this wrapper.
     * 
     * @invariant value <= MAX_VALUE : "The first two bits are zero.";
     */
    private final @NonNegative long value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     * 
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    public @NonNegative long getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param value the value of the new wrapper.
     * 
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    private IntvarWrapper(@Nonnull @Loaded @BasedOn("intvar@core.digitalid.net") SemanticType type, @NonNegative long value) {
        super(type);
        
        assert value >= 0 : "The value is non-negative.";
        assert value <= MAX_VALUE : "The first two bits have to be zero.";
        
        this.value = value;
    }
    
    /* -------------------------------------------------- Decode Length -------------------------------------------------- */
    
    /**
     * Decodes the length of the intvar as indicated in the first two bits of the byte at the given offset.
     * 
     * @param block the block containing the intvar.
     * @param offset the offset of the intvar in the block.
     * 
     * @return the length of the intvar.
     * 
     * @ensure return == 1 || return == 2 || return == 4 || return == 8 : "The result is either 1, 2, 4 or 8.";
     */
    @Pure
    public static int decodeLength(@Nonnull Block block, @NonNegative int offset) throws InvalidEncodingException {
        assert offset >= 0 : "The offset is not negative.";
        
        if (offset >= block.getLength()) { throw InvalidBlockOffsetException.get(offset, 0, block); }
        
        return 1 << ((block.getByte(offset) & 0xFF) >>> 6);
    }
    
    /**
     * Decodes the length of the intvar as indicated in the first two bits of the first byte in the given byte array.
     * 
     * @param bytes the byte array containing the intvar at index 0.
     * 
     * @return the length of the intvar.
     * 
     * @ensure return == 1 || return == 2 || return == 4 || return == 8 : "The result is either 1, 2, 4 or 8.";
     */
    @Pure
    static int decodeLength(@Nonnull @NonEmpty byte[] bytes) throws InvalidEncodingException {
        assert bytes.length > 0 : "The byte array is not empty.";
        
        return 1 << ((bytes[0] & 0xFF) >>> 6);
    }
    
    /* -------------------------------------------------- Decode Value -------------------------------------------------- */
    
    /**
     * Decodes the value of the intvar that is stored in the indicated section of the given block.
     * 
     * @param block the block containing the value.
     * @param offset the offset of the intvar in the block.
     * @param length the length of the intvar in the block.
     * 
     * @return the decoded value of the intvar.
     * 
     * @require length == decodeLength(block, offset) : "The length is correct.";
     * 
     * @ensure determineLength(return) == length : "The length of the return value as an intvar matches the given length.";
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    public static @NonNegative long decodeValue(@Nonnull Block block, @NonNegative int offset, int length) throws InvalidEncodingException {
        assert offset >= 0 : "The offset is not negative.";
        assert length == decodeLength(block, offset) : "The length is correct.";
        
        if (offset + length > block.getLength()) { throw InvalidBlockOffsetException.get(offset, length, block); }
        
        long result = block.getByte(offset) & 0x3F;
        for (int i = 1; i < length; i++) {
            result = (result << 8) | (block.getByte(offset + i) & 0xFF);
        }
        
        if (determineLength(result) != length) { throw InvalidBlockLengthException.get(determineLength(result), length); }
        
        return result;
    }
    
    /**
     * Decodes the value of the intvar that is stored in the first bytes of the given byte array.
     * 
     * @param bytes the byte array containing the intvar at index 0.
     * @param length the length of the intvar in the given byte array.
     * 
     * @return the decoded value of the intvar.
     * 
     * @require bytes.length >= length : "The byte array is big enough.";
     * @require length == decodeLength(bytes) : "The length is correct.";
     * 
     * @ensure determineLength(return) == length : "The length of the return value as an intvar matches the given length.";
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    static @NonNegative long decodeValue(@Nonnull byte[] bytes, int length) throws InvalidEncodingException {
        assert bytes.length >= length : "The byte array is big enough.";
        assert length == decodeLength(bytes) : "The length is correct.";
        
        long result = bytes[0] & 0x3F;
        for (int i = 1; i < length; i++) {
            result = (result << 8) | (bytes[i] & 0xFF);
        }
        
        if (determineLength(result) != length) { throw InvalidBlockLengthException.get(determineLength(result), length); }
        
        return result;
    }
    
    /* -------------------------------------------------- Static Encoding -------------------------------------------------- */
    
    /**
     * Determines the length of the given value when encoded as an intvar.
     * 
     * @param value the value to be encoded as an intvar.
     * 
     * @return the length of the given value when encoded as an intvar.
     * 
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    @Pure
    public static int determineLength(@NonNegative long value) {
        assert value >= 0 : "The value is not negative.";
        assert value <= MAX_VALUE : "The first two bits have to be zero.";
        
        if (value >= 1_073_741_824) { // 2^30
            return 8;
        } else if (value >= 16_384) { // 2^14
            return 4;
        } else if (value >= 64) { // 2^6
            return 2;
        } else {
            return 1;
        }
    }
    
    /**
     * Encodes the given value into the indicated section of the block.
     * 
     * @param block the block into which the value is encoded.
     * @param offset the offset of the indicated section in the block.
     * @param length the length of the indicated section in the block.
     * @param value the value to be encoded as an intvar.
     * 
     * @require offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
     * @require length == determineLength(value) : "The length of the indicated section in the block has to match the length of the encoded value.";
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    public static void encode(@Nonnull @Encoding Block block, @NonNegative int offset, int length, @NonNegative long value) {
        assert offset >= 0 : "The offset is not negative.";
        assert offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
        assert length == determineLength(value) : "The length of the indicated section in the block has to match the length of the encoded value.";
        assert value >= 0 : "The value is not negative.";
        assert value <= MAX_VALUE : "The first two bits have to be zero.";
        
        long shifter = value;
        for (int i = length - 1; i >= 1; i--) {  
            block.setByte(offset + i, (byte) shifter);
            shifter >>>= 8;
        }
        
        block.setByte(offset, (byte) (shifter | (Integer.numberOfTrailingZeros(length) << 6)));
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    @Pure
    @Override
    public int determineLength() {
        return determineLength(value);
    }
    
    @Pure
    @Override
    public void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        encode(block, 0, block.getLength(), value);
    }
    
    /* -------------------------------------------------- Syntactic Type -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code intvar@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType XDF_TYPE = SyntacticType.map("intvar@core.digitalid.net").load(0);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return XDF_TYPE;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this wrapper.
     */
    @Immutable
    public static final class XDFConverter extends AbstractWrapper.NonRequestingXDFConverter<IntvarWrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @BasedOn("intvar@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull IntvarWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("intvar@core.digitalid.net") Block block) throws InvalidEncodingException {
            final int length = block.getLength();
            
            if (length != decodeLength(block, 0)) { throw InvalidBlockLengthException.get(decodeLength(block, 0), length); }
            
            final long value = decodeValue(block, 0, length);
            return new IntvarWrapper(block.getType(), value);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code semantic.intvar@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.intvar@core.digitalid.net").load(XDF_TYPE);
    
    /**
     * Stores a static XDF converter for performance reasons.
     */
    private static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter(SEMANTIC);
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new block containing the given value.
     * 
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("intvar@core.digitalid.net") SemanticType type, @NonNegative long value) {
        return XDF_CONVERTER.encodeNonNullable(new IntvarWrapper(type, value));
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     * 
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    public static @NonNegative long decode(@Nonnull @NonEncoding @BasedOn("intvar@core.digitalid.net") Block block) throws InvalidEncodingException {
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).value;
    }
    
    /* -------------------------------------------------- SQL Utility -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
    /**
     * Stores the given value at the given index in the given array.
     * 
     * @param value the value which is to be stored in the values array.
     * @param values a mutable array in which the value is to be stored.
     * @param index the array index at which the value is to be stored.
     */
    public static void store(long value, @NonCapturable @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
        values.set(index.getAndIncrementValue(), String.valueOf(value));
    }
    
    /**
     * Stores the given value at the given index in the given prepared statement.
     * 
     * @param value the value which is to be stored in the given prepared statement.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the statement index at which the value is to be stored.
     */
    @NonCommitting
    public static void store(long value, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex.getAndIncrementValue(), value);
    }
    
    /**
     * Returns the value from the given column of the given result set.
     * 
     * @param resultSet the set from which the value is to be retrieved.
     * @param columnIndex the index from which the value is to be retrieved.
     * 
     * @return the value from the given column of the given result set.
     */
    @Pure
    @NonCommitting
    public static long restore(@Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws SQLException {
        return resultSet.getLong(columnIndex.getAndIncrementValue());
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL type of this wrapper.
     */
    public static final @Nonnull SQLType SQL_TYPE = SQLType.BIGINT;
    
    /**
     * The SQL converter for this wrapper.
     */
    @Immutable
    public static final class SQLConverter extends AbstractWrapper.SQLConverter<IntvarWrapper> {
        
        /**
         * Creates a new SQL converter with the given column declaration.
         *
         * @param declaration the declaration used to store instances of the wrapper.
         */
        private SQLConverter(@Nonnull @Matching ColumnDeclaration declaration) {
            super(declaration, SEMANTIC);
            
            assert declaration.getType() == SQL_TYPE : "The declaration matches the SQL type of the wrapper.";
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull IntvarWrapper wrapper, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws SQLException {
            store(wrapper.value, preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable IntvarWrapper restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws SQLException {
            final long value = restore(resultSet, columnIndex);
            if (resultSet.wasNull()) { return null; }
            if (value < 0 || value > MAX_VALUE) { throw new SQLException("The value " + value + " does not fit into an intvar."); }
            else { return new IntvarWrapper(getType(), value); }
        }
        
    }
    
    /**
     * Stores the default declaration of this wrapper.
     */
    private static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("value", SQL_TYPE);
    
    @Pure
    @Override
    public @Nonnull SQLConverter getSQLConverter() {
        return new SQLConverter(DECLARATION);
    }
    
    /* -------------------------------------------------- Wrapper -------------------------------------------------- */
    
    /**
     * The wrapper for this wrapper.
     */
    @Immutable
    public static class Wrapper extends ValueWrapper.Wrapper<Long, IntvarWrapper> {
        
        @Pure
        @Override
        protected boolean isValid(@Nonnull Long value) {
            return value >= 0 && value <= MAX_VALUE;
        }
        
        @Pure
        @Override
        protected @Nonnull IntvarWrapper wrap(@Nonnull SemanticType type, @Nonnull @Validated Long value) {
            assert isValid(value) : "The value is valid.";
            
            return new IntvarWrapper(type, value);
        }
        
        @Pure
        @Override
        protected @Nonnull @Validated Long unwrap(@Nonnull IntvarWrapper wrapper) {
            return wrapper.value;
        }
        
    }
    
    /**
     * Stores the wrapper of this wrapper.
     */
    public static final @Nonnull Wrapper WRAPPER = new Wrapper();
    
    /* -------------------------------------------------- Value Converters -------------------------------------------------- */
      
    /**
     * Returns the value XDF converter of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value XDF converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueXDFConverter<Long, IntvarWrapper> getValueXDFConverter(@Nonnull @BasedOn("intvar@core.digitalid.net") SemanticType type) {
        return new ValueXDFConverter<>(WRAPPER, new XDFConverter(type));
    }
    
    /**
     * Returns the value SQL converter of this wrapper.
     * 
     * @param declaration the declaration of the converter.
     *
     * @return the value SQL converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueSQLConverter<Long, IntvarWrapper> getValueSQLConverter(@Nonnull @Matching ColumnDeclaration declaration) {
        return new ValueSQLConverter<>(WRAPPER, new SQLConverter(declaration));
    }
    
    /**
     * Returns the value converters of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * @param declaration the declaration of the converter.
     *
     * @return the value converters of this wrapper.
     */
    @Pure
    public static @Nonnull NonRequestingConverters<Long, Object> getValueConverters(@Nonnull @BasedOn("intvar@core.digitalid.net") SemanticType type, @Nonnull @Matching ColumnDeclaration declaration) {
        return NonRequestingConverters.get(getValueXDFConverter(type), getValueSQLConverter(declaration));
    }
    
}
