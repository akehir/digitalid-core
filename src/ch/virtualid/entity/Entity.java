package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Concept;
import ch.virtualid.concept.Instance;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.identity.Identity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.server.Host;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * An entity captures the {@link Site site} and the {@link Identity identity} of a {@link Concept concept}.
 * 
 * @see Account
 * @see Role
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Entity extends Instance implements Immutable, SQLizable {
    
    /**
     * Stores the aspect of the observed entity being created.
     */
    public static final @Nonnull Aspect CREATED = new Aspect(Entity.class, "created");
    
    /**
     * Stores the aspect of the observed role being removed.
     */
    public static final @Nonnull Aspect REMOVED = new Aspect(Entity.class, "removed");
    
    
    /**
     * Stores the data type used to reference instances of this class.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    
    /**
     * Returns the site of this entity.
     * 
     * @return the site of this entity.
     */
    @Pure
    public abstract @Nonnull Site getSite();
    
    /**
     * Returns the identity of this entity.
     * 
     * @return the identity of this entity.
     */
    @Pure
    public abstract @Nonnull Identity getIdentity();
    
    /**
     * Returns the number that references this entity in the database.
     * 
     * @return the number that references this entity in the database.
     */
    @Pure
    public abstract long getNumber();
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param site the site that accommodates the returned entity.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Entity get(@Nonnull Site site, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        if (site instanceof Host) {
            return Account.get((Host) site, resultSet, columnIndex);
        } else if (site instanceof Client) {
            return Role.get((Client) site, resultSet, columnIndex);
        } else {
            throw new ShouldNeverHappenError("A site is either a host or a client.");
        }
    }
    
    @Override
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, getNumber());
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(getNumber());
    }
    
}
