package net.digitalid.service.core.identity.resolution;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.utility.database.exceptions.DatabaseException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.internal.InternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identifier.ExternalIdentifier;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identifier.IdentifierImplementation;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identifier.NonHostIdentifier;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.system.errors.InitializationError;

/**
 * This class models the successor of an {@link Identifier identifier}.
 * 
 * TODO: Support the export and import of all successors that belong to identifiers of a certain host.
 */
public final class Successor {
    
    static {
        assert Database.isMainThread() : "This static block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_successor (identifier " + IdentifierImplementation.FORMAT + " NOT NULL, successor " + IdentifierImplementation.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", PRIMARY KEY (identifier), FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The database tables of the predecessors could not be created.", exception);
        }
    }
    
    /**
     * Returns the successor of the given identifier as stored in the database.
     * 
     * @param identifier the identifier whose successor is to be returned.
     * 
     * @return the successor of the given identifier as stored in the database.
     */
    @NonCommitting
    public static @Nullable InternalNonHostIdentifier get(@Nonnull NonHostIdentifier identifier) throws DatabaseException {
        @Nonnull String query = "SELECT successor FROM general_successor WHERE identifier = " + identifier;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) { return new InternalNonHostIdentifier(resultSet.getString(1)); }
            else { return null; }
        }
    }
    
    /**
     * Returns the successor of the given identifier as stored in the database or retrieved by a new request.
     * 
     * @param identifier the identifier whose successor is to be returned.
     * 
     * @return the successor of the given identifier as stored in the database or retrieved by a new request.
     */
    @NonCommitting
    public static @Nonnull InternalNonHostIdentifier getReloaded(@Nonnull NonHostIdentifier identifier) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        @Nullable InternalNonHostIdentifier successor = get(identifier);
        if (successor == null) {
            final @Nonnull Reply reply;
            if (identifier instanceof InternalNonHostIdentifier) {
                final @Nonnull IdentityReply identityReply = new IdentityQuery((InternalNonHostIdentifier) identifier).sendNotNull();
                successor = identityReply.getSuccessor();
                reply = identityReply;
            } else {
                assert identifier instanceof ExternalIdentifier;
                // TODO: Load the verified successor from 'digitalid.net' or return null otherwise.
                throw new UnsupportedOperationException("The verification of email addresses is not supported yet.");
            }
            
            if (successor != null) { set(identifier, successor, reply); }
            else { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The identity with the identifier " + identifier + " has not been relocated."); }
        }
        return successor;
    }
    
    /**
     * Sets the successor of the given identifier to the given value.
     * Only commit the transaction if the successor has been verified.
     * 
     * @param identifier the identifier whose successor is to be set.
     * @param successor the successor to be set for the given identifier.
     * @param reply the reply stating that the given identifier has the given successor.
     */
    @NonCommitting
    public static void set(@Nonnull NonHostIdentifier identifier, @Nonnull InternalNonHostIdentifier successor, @Nullable Reply reply) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO general_successor (identifier, successor, reply) VALUES (" + identifier + ", " + successor + ", " + reply + ")");
        }
    }
    
}
