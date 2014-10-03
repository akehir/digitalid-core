package ch.virtualid.packet;

import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.virtualid.exceptions.external.FailedEncodingException;
import java.math.BigInteger;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class compresses, signs and encrypts requests with credentials.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class CredentialsRequest extends Request {
    
    /**
     * Packs the given content with the given arguments signed with the given credentials.
     * 
     * @param contents the contents of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param credentials the credentials with which the content is signed.
     * @param certificates the certificates that are appended to an identity-based authentication or null.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param commitment the value b' or null if the credentials are not to be shortened.
     * 
     * @require !contents.isEmpty() : "The list of contents is not empty.";
     * @require CredentialsSignatureWrapper.validCredentials(credentials) : "The credentials have to be valid.";
     * @require CredentialsSignatureWrapper.certificatesAreValid(certificates, credentials) : "The certificates have to be valid (given the given credentials).";
     * 
     * @ensure getSize() == contents.size() : "The size of this request equals the size of the contents.";
     */
    public CredentialsRequest(@Nonnull ReadonlyList<SelfcontainedWrapper> contents, @Nonnull HostIdentifier recipient, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull List<Credential> credentials, @Nullable List<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger commitment) throws FailedEncodingException {
        super(contents, recipient, new SymmetricKey(), subject, audit, null, null, credentials, certificates, lodged, commitment);
    }
    
}
