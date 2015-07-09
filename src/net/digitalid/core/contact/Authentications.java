package net.digitalid.core.contact;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.BooleanWrapper;

/**
 * This class models the authentications of contacts as a set of attribute types.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Authentications extends AttributeTypeSet implements ReadOnlyAuthentications, Blockable {
    
    /**
     * Stores the semantic type {@code authentication.contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("authentication.contact@core.digitalid.net").load(AttributeTypeSet.TYPE);
    
    
    /**
     * Stores the semantic type {@code identity.based.authentication.contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTITY_BASED_TYPE = SemanticType.create("identity.based.authentication.contact@core.digitalid.net").load(new Category[] {Category.HOST}, Time.TROPICAL_YEAR, BooleanWrapper.TYPE);
    
    /**
     * Stores an empty set of authentications.
     */
    public static final @Nonnull ReadOnlyAuthentications NONE = new Authentications().freeze();
    
    /**
     * Stores an identity-based authentication.
     */
    public static final @Nonnull ReadOnlyAuthentications IDENTITY_BASED = new Authentications(IDENTITY_BASED_TYPE).freeze();
    
    
    /**
     * Creates an empty set of authentications.
     */
    public Authentications() {}
    
    /**
     * Creates new authentications with the given attribute type.
     * 
     * @param type the attribute type used for authentication.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure isSingle() : "The new authentications are single.";
     */
    public Authentications(@Nonnull SemanticType type) {
        super(type);
    }
    
    /**
     * Creates new authentications from the given authentications.
     * 
     * @param authentications the authentications to add to the new authentications.
     */
    public Authentications(@Nonnull ReadOnlyAuthentications authentications) {
        super(authentications);
    }
    
    /**
     * Creates new authentications from the given block.
     * 
     * @param block the block containing the authentications.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public Authentications(@Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    @Override
    public @Nonnull ReadOnlyAuthentications freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull Authentications clone() {
        return new Authentications(this);
    }
    
}
