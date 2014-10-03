package ch.xdf;

import ch.virtualid.annotations.Exposed;
import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import javax.annotation.Nonnull;

/**
 * Wraps a block with the syntactic type {@code int64@xdf.ch} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Int64Wrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code int64@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("int64@xdf.ch").load(0);
    
    /**
     * The byte length of an int64.
     */
    public static final int LENGTH = 8;
    
    
    /**
     * Stores the value of this wrapper.
     */
    private final long value;
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     */
    public Int64Wrapper(@Nonnull SemanticType type, long value) {
        super(type);
        
        this.value = value;
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    public Int64Wrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
        
        long value = 0;
        for (int i = 0; i < LENGTH; i++) {
            value = (short) ((value << 8) | (block.getByte(i) & 0xff));
        }
        this.value = value;
    }
    
    /**
     * Returns the value of the wrapped block.
     * 
     * @return the value of the wrapped block.
     */
    @Pure
    public long getValue() {
        return value;
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        return LENGTH;
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        long value = this.value;
        for (int i = LENGTH - 1; i >= 0; i--) {  
            block.setByte(i, (byte) value);
            value >>>= 8;
        }
    }
    
}
