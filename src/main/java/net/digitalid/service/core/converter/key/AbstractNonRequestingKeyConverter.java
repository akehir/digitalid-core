package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.concept.ConceptKeyConverter;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class allows to convert an object to its key and recover it again given its key (and an external object) without requests.
 * 
 * @param <O> the type of the objects that this converter can convert and recover, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are converted to and recovered from (with an external object).
 * 
 * @see ConceptKeyConverter
 * @see NonConvertingKeyConverter
 */
@Stateless
public abstract class AbstractNonRequestingKeyConverter<O, E, K> extends AbstractKeyConverter<O, E, K> {
    
    @Pure
    @Override
    public abstract @Nonnull O recover(@Nonnull E external, @Nonnull @Validated K key) throws InvalidEncodingException;
    
}
