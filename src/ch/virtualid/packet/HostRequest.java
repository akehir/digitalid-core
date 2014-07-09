package ch.virtualid.packet;

import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.exceptions.FailedEncodingException;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This class compresses, signs and encrypts requests by hosts.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class HostRequest extends Request {
    
    /**
     * Determines how often the cached symmetric keys are rotated (currently once a year).
     */
    private static final long ROTATION = 1000 * 60 * 60 * 24 * 365;
    
    /**
     * Packs the given content with the given arguments signed by the given host.
     * 
     * @param contents the contents of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param signer the identifier of the signing host.
     * @require !contents.isEmpty() : "The list of contents is not empty.";
     * @require recipient.exists() : "The recipient has to exist.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * @ensure getSize() == contents.size() : "The size of this request equals the size of the contents.";
     */
    public HostRequest(@Nonnull List<SelfcontainedWrapper> contents, @Nonnull HostIdentifier recipient, @Nonnull Identifier subject, @Nonnull HostIdentifier signer) throws FailedEncodingException {
        super(contents, recipient, getSymmetricKey(recipient, ROTATION), subject, null, signer, null, null, null, false, null);
    }
    
}
