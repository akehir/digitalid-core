package net.digitalid.core.conversion.wrappers.exceptions;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;

/**
 * Blocks that are larger than the maximum integer are not supported by this library due to the array limitations of Java.
 */
public final class UnsupportedBlockLengthException extends IOException {
    
    /**
     * Creates a new unsupported block length exception.
     */
    private UnsupportedBlockLengthException() {}
    
    /**
     * Returns a new unsupported block length exception.
     * 
     * @return a new unsupported block length exception.
     */
    @Pure
    public static @Nonnull UnsupportedBlockLengthException get() {
        return new UnsupportedBlockLengthException();
    }
    
}