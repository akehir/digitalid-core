/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.resolution.successor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.threading.Threading;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.ExternalIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identifier.NonHostIdentifier;

/**
 * This class models the successor of an {@link Identifier identifier}.
 * 
 * TODO: Support the export and import of all successors that belong to identifiers of a certain host.
 */
@Utility
public abstract class Successor {
    
    static {
        Require.that(Threading.isMainThread()).orThrow("This static block is called in the main thread.");
        
        // TODO: Use the new database API.
        
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_successor (identifier " + IdentifierImplementation.FORMAT + " NOT NULL, successor " + IdentifierImplementation.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", PRIMARY KEY (identifier), FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
//        } catch (@Nonnull SQLException exception) {
//            throw InitializationError.get("The database tables of the predecessors could not be created.", exception);
//        }
    }
    
    /**
     * Returns the successor of the given identifier as stored in the database.
     */
    @Pure
    @NonCommitting
    public static @Nullable InternalNonHostIdentifier get(@Nonnull NonHostIdentifier identifier) throws DatabaseException {
        // TODO: Use the new database API.
        
        throw new UnsupportedOperationException();
        
//        @Nonnull String query = "SELECT successor FROM general_successor WHERE identifier = " + identifier;
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            if (resultSet.next()) { return new InternalNonHostIdentifier(resultSet.getString(1)); }
//            else { return null; }
//        }
    }
    
    /**
     * Returns the successor of the given identifier as stored in the database or retrieved by a new request.
     * 
     * @param identifier the identifier whose successor is to be returned.
     * 
     * @return the successor of the given identifier as stored in the database or retrieved by a new request.
     */
    @NonCommitting
    @PureWithSideEffects
    public static @Nonnull InternalNonHostIdentifier getReloaded(@Nonnull NonHostIdentifier identifier) throws ExternalException {
        @Nullable InternalNonHostIdentifier successor = get(identifier);
        if (successor == null) {
            final @Nonnull Reply reply;
            if (identifier instanceof InternalNonHostIdentifier) {
                // TODO
//                final @Nonnull IdentityReply identityReply = new IdentityQuerySubclass((InternalNonHostIdentifier) identifier).send();
//                successor = identityReply.getSuccessor();
//                reply = identityReply;
                reply = null;
            } else {
                assert identifier instanceof ExternalIdentifier;
                // TODO: Load the verified successor from 'digitalid.net' or return null otherwise.
                throw new UnsupportedOperationException("The verification of email addresses is not supported yet.");
            }
            
            if (successor != null) { set(identifier, successor, reply); }
            else { throw RequestExceptionBuilder.withCode(RequestErrorCode.EXTERNAL).withMessage("The identity with the identifier " + identifier + " has not been relocated.").build(); }
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
    @PureWithSideEffects
    public static void set(@Nonnull NonHostIdentifier identifier, @Nonnull InternalNonHostIdentifier successor, @Nullable Reply reply) throws DatabaseException {
        // TODO: Use the new database API.
        
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("INSERT INTO general_successor (identifier, successor, reply) VALUES (" + identifier + ", " + successor + ", " + reply + ")");
//        }
    }
    
}
