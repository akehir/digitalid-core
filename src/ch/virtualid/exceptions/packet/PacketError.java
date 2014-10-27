package ch.virtualid.exceptions.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.Int8Wrapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class enumerates the various packet errors.
 * 
 * @see PacketException
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public enum PacketError implements Blockable, Immutable, SQLizable {
    
    /**
     * The error code for an internal problem.
     */
    INTERNAL(0),
    
    /**
     * The error code for an external problem.
     */
    EXTERNAL(1),
    
    /**
     * The error code for an invalid packet.
     */
    PACKET(2),
    
    /**
     * The error code for an invalid encryption.
     */
    ENCRYPTION(3),
    
    /**
     * The error code for invalid elements.
     */
    ELEMENTS(4),
    
    /**
     * The error code for an invalid signature.
     */
    SIGNATURE(5),
    
    /**
     * The error code for an invalid compression.
     */
    COMPRESSION(6),
    
    /**
     * The error code for an invalid content.
     */
    CONTENT(7),
    
    /**
     * The error code for an invalid method type.
     */
    METHOD(8),
    
    /**
     * The error code for an invalid reply type.
     */
    REPLY(9),
    
    /**
     * The error code for an invalid identifier as subject.
     */
    IDENTIFIER(10),
    
    /**
     * The error code for an insufficient authorization.
     */
    AUTHORIZATION(11),
    
    /**
     * The error code for a required key rotation.
     */
    KEYROTATION(12),
    
    /**
     * The error code for a relocated identity.
     */
    RELOCATION(13),
    
    /**
     * The error code for an outdated action.
     */
    OUTDATED(14),
    
    /**
     * The error code for an invalid service.
     */
    SERVICE(14),
    
    /**
     * The error code for a replayed packet.
     */
    REPLAY(15);
    
    
    /**
     * Returns whether the given value is a valid packet error.
     *
     * @param value the value to check.
     * 
     * @return whether the given value is a valid packet error.
     */
    @Pure
    public static boolean isValid(byte value) {
        return value >= 0 && value <= 15;
    }
    
    /**
     * Returns the packet error encoded by the given value.
     * 
     * @param value the value encoding the packet error.
     * 
     * @return the packet error encoded by the given value.
     * 
     * @require isValid(value) : "The value is a valid packet error.";
     */
    @Pure
    public static @Nonnull PacketError get(byte value) {
        assert isValid(value) : "The value is a valid packet error.";
        
        for (final @Nonnull PacketError error : values()) {
            if (error.value == value) return error;
        }
        
        throw new ShouldNeverHappenError("The value '" + value + "' does not encode a packet error.");
    }
    
    
    /**
     * Stores the semantic type {@code code.error.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("code.error.packet@virtualid.ch").load(Int8Wrapper.TYPE);
    
    /**
     * Returns the packet error encoded by the given block.
     * 
     * @param block the block containing the packet error.
     * 
     * @return the packet error encoded by the given block.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static @Nonnull PacketError get(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final byte value = new Int8Wrapper(block).getValue();
        if (!isValid(value)) throw new InvalidEncodingException("The value '" + value + "' does not encode a packet error.");
        return get(value);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new Int8Wrapper(TYPE, value).toBlock();
    }
    
    
    /**
     * Stores the byte representation of this packet error.
     * 
     * @invariant isValid(value) : "The value is a valid packet error.";
     */
    private final byte value;
    
    /**
     * Creates a new packet error with the given value.
     * 
     * @param value the value encoding the packet error.
     */
    private PacketError(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the byte representation of this packet error.
     * 
     * @return the byte representation of this packet error.
     * 
     * @ensure isValid(value) : "The value is a valid packet error.";
     */
    @Pure
    public byte getValue() {
        return value;
    }
    
    /**
     * Returns the name of this packet error.
     * 
     * @return the name of this packet error.
     */
    @Pure
    public @Nonnull String getName() {
        final @Nonnull String string = name().toLowerCase();
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Database.getConfiguration().TINYINT();
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull PacketError get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull byte value = resultSet.getByte(columnIndex);
        if (!isValid(value)) throw new SQLException("'" + value + "' is not a valid packet error.");
        return get(value);
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setByte(parameterIndex, value);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
}
