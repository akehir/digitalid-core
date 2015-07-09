package net.digitalid.core.agent;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlyMap;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.SQLizable;

/**
 * This interface provides read-only access to {@link FreezableAgentPermissions agent permissions} and should <em>never</em> be cast away.
 * 
 * @invariant areValid() : "These agent permissions are always valid.";
 * 
 * @see FreezableAgentPermissions
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyAgentPermissions extends ReadOnlyMap<SemanticType, Boolean>, Blockable, SQLizable {
    
    /**
     * Returns whether these agent permissions are valid.
     * 
     * @return whether these agent permissions are valid.
     */
    @Pure
    public boolean areValid();
    
    /**
     * Returns whether these agent permissions contain a single permission.
     * 
     * @return whether these agent permissions contain a single permission.
     */
    @Pure
    public boolean isSingle();
    
    /**
     * Returns whether these agent permissions are empty or contain a single permission.
     * 
     * @return whether these agent permissions are empty or contain a single permission.
     */
    @Pure
    public boolean areEmptyOrSingle();
    
    /**
     * Returns whether an agent with these agent permissions can read the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether an agent with these agent permissions can read the given type.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public boolean canRead(@Nonnull SemanticType type);
    
    /**
     * Checks that an agent with these agent permissions can read the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public void checkCanRead(@Nonnull SemanticType type) throws PacketException;
    
    /**
     * Returns whether an agent with agent these permissions can write the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether an agent with agent these permissions can write the given type.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public boolean canWrite(@Nonnull SemanticType type);
    
    /**
     * Checks that an agent with these agent permissions can write the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public void checkCanWrite(@Nonnull SemanticType type) throws PacketException;
    
    /**
     * Returns whether these agent permissions cover the given agent permissions.
     * 
     * @param permissions the agent permissions that need to be covered.
     * 
     * @return whether these agent permissions cover the given agent permissions.
     */
    @Pure
    public boolean cover(@Nonnull ReadOnlyAgentPermissions permissions);
    
    /**
     * Checks that these agent permissions cover the given agent permissions and throws a {@link PacketException} if not.
     * 
     * @param permissions the agent permissions that need to be covered.
     */
    @Pure
    public void checkCover(@Nonnull ReadOnlyAgentPermissions permissions) throws PacketException;
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableAgentPermissions clone();
    
    
    /**
     * Returns all types of these permissions as numbers.
     * 
     * @return all types of these permissions as numbers.
     */
    @Pure
    public @Nonnull String allTypesToString();
    
    /**
     * Returns the write types of these permissions as numbers.
     * 
     * @return the write types of these permissions as numbers.
     */
    @Pure
    public @Nonnull String writeTypesToString();
    
    
    /**
     * Sets the parameters at the given start index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param startIndex the start index of the parameters to set.
     * 
     * @require areEmptyOrSingle() : "These permissions are empty or single.";
     */
    @NonCommitting
    public void setEmptyOrSingle(@Nonnull PreparedStatement preparedStatement, int startIndex) throws SQLException;
    
}
