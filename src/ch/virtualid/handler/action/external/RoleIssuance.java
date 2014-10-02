package ch.virtualid.handler.action.external;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.Method;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.FailedRequestException;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import ch.xdf.exceptions.InvalidSignatureException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Issues the given role to the given subject.
 * 
 * TODO: Adapt this class from the certificate issuance class.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.1
 */
public final class RoleIssuance extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code issuance.role@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("issuance.role@virtualid.ch").load(TupleWrapper.TYPE); // TODO
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the attribute that is certified.
     * 
     * @invariant attribute.getType().isAttributeFor(subject.getIdentity().getCategory()) : "The block is an attribute for the subject.";
     */
    private final @Nonnull Block attribute;
    
    /**
     * Stores the certificate that is issued.
     * 
     * @invariant certificate.getSigner() instanceof NonHostIdentifier : "The certificate is signed by a non-host.";
     */
    private final @Nonnull HostSignatureWrapper certificate;
    
    /**
     * Creates an external action to issue the given certificate to the given subject.
     * 
     * @param account the account to which this handler belongs.
     * @param subject the subject of this external action.
     * @param certificate the certificate to issue.
     * 
     * @require account.getIdentity() instanceof Person : "The account belongs to a person.";
     * @require attribute.getType().isAttributeFor(subject.getCategory()) : "The block is an attribute for the subject.";
     */
    public RoleIssuance(@Nonnull Account account, @Nonnull Identity subject, @Nonnull Block attribute) {
        super(account, subject);
        
        assert account.getIdentity() instanceof Person : "The account belongs to a person.";
        assert attribute.getType().isAttributeFor(subject.getCategory()) : "The block is an attribute for the subject.";
        
        this.attribute = attribute;
        this.certificate = new HostSignatureWrapper(TYPE, attribute, subject.getAddress(), account.getIdentity().getAddress());
        this.auditPermissions = new AgentPermissions(attribute.getType(), false).freeze();
    }
    
    /**
     * Creates an external action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    private RoleIssuance(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException, SQLException, InvalidDeclarationException, FailedRequestException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull SignatureWrapper certificate = SignatureWrapper.decodeUnverified(block, null);
        if (!(certificate instanceof HostSignatureWrapper)) throw new InvalidEncodingException("The block has to be signed by a host.");
        this.certificate = (HostSignatureWrapper) certificate;
        if (!(this.certificate.getSigner() instanceof NonHostIdentifier)) throw new InvalidEncodingException("The certificate has to be signed by a non-host.");
        
        this.attribute = new SelfcontainedWrapper(certificate.getElementNotNull()).getElement();
        if (!attribute.getType().isAttributeFor(getSubject().getIdentity().getCategory())) throw new InvalidEncodingException("The block is an attribute for the subject.");
        
        this.auditPermissions = new AgentPermissions(attribute.getType(), false).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return certificate.toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Issues a certificate for " + attribute.getType().getAddress() + ".";
    }
    
    
    /**
     * Executes this action on both hosts and clients.
     */
    private void executeOnBoth() throws SQLException {
        // TODO: Replace the attribute with the certificate if they match. Otherwise, this should be reported as a packet error (at least on host).
    }
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert getSignature() != null : "The signature of this handler is not null.";
        
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (!(signature instanceof HostSignatureWrapper)) throw new PacketException(PacketError.AUTHORIZATION);
        
        try {
            certificate.verify();
        } catch (@Nonnull InvalidEncodingException | InvalidSignatureException exception) {
            throw new PacketException(PacketError.REQUEST, exception);
        }
        
        // TODO: Check other things like whether the signer has the necessary delegation.
        
        executeOnBoth();
        return null;
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        assert isOnClient() : "This method is called on a client.";
        
        // TODO: I'm not sure how far the checks should go on the client.
        
        executeOnBoth();
    }
    
    
    /**
     * Stores the audit permissions for this action.
     */
    private final @Nonnull ReadonlyAgentPermissions auditPermissions;
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return auditPermissions;
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    protected static final class Factory extends Method.Factory {
        
        static { Method.add(new Factory()); }
        
        @Pure
        @Override
        public @Nonnull SemanticType getType() {
            return TYPE;
        }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException, FailedRequestException {
            return new RoleIssuance(entity, signature, recipient, block);
        }
        
    }
    
}
