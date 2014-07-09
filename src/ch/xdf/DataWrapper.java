package ch.xdf;

import ch.virtualid.annotation.Capturable;
import ch.virtualid.annotation.Captured;
import ch.virtualid.annotation.Exposed;
import ch.virtualid.annotation.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code data@xdf.ch} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class DataWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code data@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("data@xdf.ch").load(0);
    
    
    /**
     * Stores the data of this wrapper.
     */
    private final @Nullable byte[] data;
    
    /**
     * Encodes the given data into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param data the data to encode into the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     */
    public DataWrapper(@Nonnull SemanticType type, @Captured @Nonnull byte[] data) {
        super(type);
        
        this.data = data;
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    public DataWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        this.data = null;
    }
    
    /**
     * Returns the data of the wrapped block.
     * 
     * @return the data of the wrapped block.
     */
    @Pure
    public @Capturable @Nonnull byte[] getData() {
        if (data != null) return data.clone();
        else return toBlock().getBytes(1);
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        if (data != null) return data.length + 1;
        else return toBlock().getLength();
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        if (data != null) block.setBytes(1, data);
    }
    
}
