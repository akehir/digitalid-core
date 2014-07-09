package ch.virtualid.database;

import ch.virtualid.annotation.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Concept;
import ch.virtualid.exception.ShouldNeverHappenError;
import ch.virtualid.identity.Identity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.server.Host;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

import todo;

/**
 * An entity captures the {@link Site site} and the {@link Identity identity} of a {@link Concept concept}.
 * 
 * @see HostEntity
 * @see ClientEntity
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
 */
public abstract class Entity implements Immutable, SQLizable {
    
    /**
     * Stores the data type used to reference instances of this class.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    
    /**
     * Returns the site of this entity.
     * 
     * @return the site of this entity.
     */
    public abstract @Nonnull Site getSite();
    
    /**
     * Returns the identity of this entity.
     * 
     * @return the identity of this entity.
     */
    public abstract @Nonnull Identity getIdentity();
    
    /**
     * Returns the number that references this entity in the database.
     * 
     * @return the number that references this entity in the database.
     */
    public abstract long getNumber();
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Entity get(@Nonnull Site site, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        if (site instanceof Client) {
            // TODO
        } else if (site instanceof Host) {
            
        } else {
            throw new ShouldNeverHappenError("A site is either a client or a host.");
        }
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, getNumber());
    }
    
    @Override
    public abstract @Nonnull String toString();
    
}
