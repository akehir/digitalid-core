package net.digitalid.service.core.identifier;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.service.core.identity.MobilePerson;
import net.digitalid.service.core.identity.Person;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models mobile identifiers.
 */
@Immutable
public final class MobileIdentifier extends ExternalIdentifier {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validity –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The pattern that valid mobile identifiers have to match.
     */
    private static final Pattern pattern = Pattern.compile("mobile:[0-9]{8,15}");
    
    /**
     * Returns whether the given string is a valid mobile identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid mobile identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return ExternalIdentifier.isConforming(string) && pattern.matcher(string).matches();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a mobile identifier with the given string.
     * 
     * @param string the string of the mobile identifier.
     */
    private MobileIdentifier(@Nonnull @Validated String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid mobile identifier.";
    }
    
    /**
     * Returns a mobile identifier with the given string.
     * 
     * @param string the string of the mobile identifier.
     * 
     * @return a mobile identifier with the given string.
     */
    @Pure
    public static @Nonnull MobileIdentifier get(@Nonnull @Validated String string) {
        return new MobileIdentifier(string);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Mapping –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Person getIdentity() throws AbortException, PacketException, ExternalException, NetworkException {
        return Mapper.getIdentity(this).toPerson();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Category –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.MOBILE_PERSON;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    private static final @Nonnull Caster<MobileIdentifier> CASTER = new Caster<MobileIdentifier>() {
        @Pure
        @Override
        protected @Nonnull MobileIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.toMobileIdentifier();
        }
    };
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory<MobileIdentifier> ENCODING_FACTORY = new EncodingFactory<>(MobilePerson.IDENTIFIER, CASTER);
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull StoringFactory<MobileIdentifier> STORING_FACTORY = new StoringFactory<>(CASTER);
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Factories<MobileIdentifier, Object> FACTORIES = Factories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
