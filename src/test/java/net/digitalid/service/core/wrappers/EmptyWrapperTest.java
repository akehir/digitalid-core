package net.digitalid.service.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.BooleanWrapper;
import net.digitalid.service.core.block.wrappers.EmptyWrapper;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 */
public class EmptyWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.map("empty@test.digitalid.net").load(EmptyWrapper.XDF_TYPE);
        new EmptyWrapper(new EmptyWrapper(TYPE).toBlock());
    }
    
}
