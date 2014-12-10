package ch.xdf;

import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.setup.ServerSetup;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link EncryptionWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class EncryptionWrapperTest extends ServerSetup {
    
    @Test
    public void testWrapping() throws Exception {
        final @Nonnull SemanticType STRING = SemanticType.create("string@syntacts.com").load(StringWrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.create("encryption@syntacts.com").load(EncryptionWrapper.TYPE, STRING);
        
        final @Nonnull Block[] blocks = new Block[] {null, new StringWrapper(STRING, "This is a secret message.").toBlock()};
        final @Nullable SymmetricKey[] symmetricKeys = new SymmetricKey[] {null, new SymmetricKey()};
        
        for (int i = 1; i < 3; i++) {
//            System.out.println("\nRound " + i + ":\n");
            for (final @Nullable Block block : blocks) {
                for (final @Nullable SymmetricKey symmetricKey : symmetricKeys) {
//                    System.out.println("Block: " + block + "; Symmetric Key: " + symmetricKey);
                    
                    // From client to host:
                    @Nonnull Block cipherBlock = new EncryptionWrapper(TYPE, block, getRecipient(), symmetricKey).toBlock();
//                    System.out.println("–> From client to host: " + cipherBlock);
                    @Nonnull EncryptionWrapper encryption = new EncryptionWrapper(cipherBlock, null);
                    assertEquals(block, encryption.getElement());
                    assertEquals(getRecipient(), encryption.getRecipient());
                    assertEquals(symmetricKey, encryption.getSymmetricKey());
                    
                    // From host to client:
                    cipherBlock = new EncryptionWrapper(TYPE, block, null, symmetricKey).toBlock();
//                    System.out.println("–> From host to client:" + cipherBlock);
                    encryption = new EncryptionWrapper(cipherBlock, symmetricKey);
                    assertEquals(block, encryption.getElement());
                    assertEquals(null, encryption.getRecipient());
                    assertEquals(symmetricKey, encryption.getSymmetricKey());
                }
            }
        }
    }
    
}
