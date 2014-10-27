package ch.virtualid.identifier;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.EmailPerson;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * This class represents email identifiers.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class EmailIdentifier extends ExternalIdentifier implements Immutable {
    
    /**
     * The pattern that valid email identifiers have to match.
     */
    private static final Pattern pattern = Pattern.compile("email:[a-z0-9]+(?:[._-][a-z0-9]+)*@[a-z0-9]+(?:[.-][a-z0-9]+)*\\.[a-z][a-z]+");
    
    /**
     * Returns whether the given string is a valid email identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid email identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return ExternalIdentifier.isConforming(string) && pattern.matcher(string).matches();
    }
    
    
    /**
     * Creates an email identifier with the given string.
     * 
     * @param string the string of the email identifier.
     * 
     * @require isValid(string) : "The string is a valid email identifier.";
     */
    public EmailIdentifier(@Nonnull String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid email identifier.";
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return EmailPerson.IDENTIFIER;
    }
    
    
    @Pure
    @Override
    public @Nonnull EmailPerson getIdentity() throws SQLException, InvalidEncodingException {
        return Mapper.mapExternalIdentity(this).toEmailPerson();
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.EMAIL_PERSON;
    }
    
}
