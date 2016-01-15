package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

/**
 * This exception is thrown when no handler could not be found a reply type.
 */
@Immutable
public class InvalidReplyTypeException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Reply Type -------------------------------------------------- */
    
    /**
     * Stores the reply type for which no handler could be found.
     */
    private final @Nonnull SemanticType replyType;
    
    /**
     * Returns the reply type for which no handler could be found.
     * 
     * @return the reply type for which no handler could be found.
     */
    @Pure
    public final @Nonnull SemanticType getReplyType() {
        return replyType;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid reply type exception with the given reply type.
     * 
     * @param replyType the reply type for which no handler could be found.
     */
    protected InvalidReplyTypeException(@Nonnull SemanticType replyType) {
        super("No handler could be found for the reply type " + replyType.getAddress() + ".");
        
        this.replyType = replyType;
    }
    
    /**
     * Returns a new invalid reply type exception with the given reply type.
     * 
     * @param replyType the reply type for which no handler could be found.
     * 
     * @return a new invalid reply type exception with the given reply type.
     */
    @Pure
    public static @Nonnull InvalidReplyTypeException get(@Nonnull SemanticType replyType) {
        return new InvalidReplyTypeException(replyType);
    }
    
}
