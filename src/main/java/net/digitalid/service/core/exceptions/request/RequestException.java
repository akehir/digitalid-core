package net.digitalid.service.core.exceptions.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.Blockable;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.system.logger.Log;

/**
 * This exception indicates an error in the encoding or content of a packet.
 * 
 * @see RequestErrorCode
 */
@Immutable
public final class RequestException extends Exception implements Blockable {
    
    /**
     * Stores the semantic type {@code message.error.packet@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MESSAGE = SemanticType.map("message.error.packet@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code error.packet@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("error.packet@core.digitalid.net").load(TupleWrapper.XDF_TYPE, RequestErrorCode.TYPE, MESSAGE);
    
    
    /**
     * Stores the error of this exception.
     */
    private final @Nonnull RequestErrorCode error;
    
    /**
     * Stores whether this exception was thrown remotely.
     */
    private final boolean remote;
    
    /**
     * Creates a new packet exception with the given error and message.
     * 
     * @param error the code indicating the kind of error.
     * @param message a string explaining the exception.
     */
    public RequestException(@Nonnull RequestErrorCode error, @Nonnull String message) {
        this(error, message, null);
    }
    
    /**
     * Creates a new packet exception with the given error, message and cause.
     * 
     * @param error the code indicating the kind of error.
     * @param message a string explaining the exception.
     * @param cause the cause of the packet exception.
     */
    public RequestException(@Nonnull RequestErrorCode error, @Nonnull String message, @Nullable Throwable cause) {
        this(error, message, cause, false);
    }
    
    /**
     * Creates a new packet exception with the given error, message and cause.
     * 
     * @param error the code indicating the kind of error.
     * @param message a string explaining the exception.
     * @param cause the cause of the packet exception.
     * @param remote whether it was thrown remotely.
     */
    public RequestException(@Nonnull RequestErrorCode error, @Nonnull String message, @Nullable Throwable cause, boolean remote) {
        super("(" + error.getName() + ") " + message, cause);
        
        this.error = error;
        this.remote = remote;
        if (!remote) { Log.warning("A packet exception occurred.", this); }
    }
    
    /**
     * Creates a new packet exception from the given block.
     * 
     * @param block the block containing the packet error.
     * 
     * @return the packet exception created from the given block.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static RequestException create(@Nonnull Block block) throws InvalidEncodingException, InternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
        final @Nonnull RequestErrorCode error = RequestErrorCode.get(elements.getNonNullable(0));
        final @Nonnull String message = StringWrapper.decodeNonNullable(elements.getNonNullable(1));
        return new RequestException(error, "A host responded with a packet error. [" + message + "]", null, true);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(2);
        elements.set(0, error.toBlock());
        elements.set(1, StringWrapper.encodeNonNullable(MESSAGE, getMessage()));
        return TupleWrapper.encode(TYPE, elements.freeze());
    }
    
    
    /**
     * Returns the error of this exception.
     * 
     * @return the error of this exception.
     */
    @Pure
    public @Nonnull RequestErrorCode getError() {
        return error;
    }
    
    /**
     * Returns whether this exception was thrown remotely.
     * 
     * @return whether this exception was thrown remotely.
     */
    @Pure
    public boolean isRemote() {
        return remote;
    }
    
}
