package net.digitalid.service.core.wrappers;

import net.digitalid.service.core.block.Block;

import net.digitalid.service.core.block.wrappers.CompressionWrapper;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link CompressionWrapper}.
 */
public final class CompressionWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@test.digitalid.net").load(StringWrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("compression@test.digitalid.net").load(CompressionWrapper.TYPE, STRING);
        final @Nonnull Block[] blocks = new Block[] {new StringWrapper(STRING, "").toBlock(), new StringWrapper(STRING, "This is a short string.").toBlock(), new StringWrapper(STRING, "This is a longer string in order to test different block lengths.").toBlock()};
        final @Nonnull byte[] algorithms = new byte[] {CompressionWrapper.NONE, CompressionWrapper.ZLIB};
        for (final @Nonnull Block block : blocks) {
            for (final byte algorithm : algorithms) {
//                System.out.println("Algorithm: " + algorithm + "; Uncompressed: " + block.getLength() + "; Compressed: " + new CompressionWrapper(TYPE, block, algorithm).toBlock().getLength());
                Assert.assertEquals(block, new CompressionWrapper(new CompressionWrapper(TYPE, block, algorithm).toBlock()).getNullableElement());
            }
        }
    }
    
}
