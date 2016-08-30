package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link StringWrapper}.
 */
public final class StringWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("string@test.digitalid.net").load(StringWrapper.XDF_TYPE);
        final @Nonnull String[] strings = new String[] {"", "äöüéè", "This is a short string.", "This is a longer string in order to test different string lengths."};
        for (final @Nonnull String string : strings) {
//            System.out.println(string);
            Assert.assertEquals(string, StringWrapper.encodeNonNullable(StringWrapper.decodeNonNullable(TYPE, string)));
        }
    }
    
}