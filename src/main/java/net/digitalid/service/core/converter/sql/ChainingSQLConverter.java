package net.digitalid.service.core.converter.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.annotations.reference.NonCapturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.ColumnIndex;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.ComposingSQLConverter;

/**
 * This class implements an SQL converter that is based on another SQL converter.
 * 
 * @param <O> the type of the objects that this converter can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other converter stores and restores (as a key for this converter's objects).
 * @param <D> the type of the external object that is needed to recover the key, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of the key, declare it as an {@link Object}.
 * 
 * @see XDFBasedSQLConverter
 */
@Immutable
public class ChainingSQLConverter<O, E, K, D> extends ComposingSQLConverter<O, E> {
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the key converter used to convert and recover the object.
     */
    private final @Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K, D> keyConverter;
    
    /**
     * Stores the SQL converter used to store and restore the object's key.
     */
    private final @Nonnull AbstractSQLConverter<K, ? super D> SQLConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new chaining SQL converter with the given converters.
     * 
     * @param keyConverter the key converter used to convert and recover the object.
     * @param SQLConverter the SQL converter used to store and restore the object's key.
     */
    protected ChainingSQLConverter(@Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull AbstractSQLConverter<K, ? super D> SQLConverter) {
        super(FreezablePair.get(SQLConverter, false).freeze());
        
        this.keyConverter = keyConverter;
        this.SQLConverter = SQLConverter;
    }
    
    /**
     * Creates a new chaining SQL converter with the given converters.
     * 
     * @param keyConverter the key converter used to convert and recover the object.
     * @param SQLConverter the SQL converter used to store and restore the object's key.
     * 
     * @return a new chaining SQL converter with the given converters.
     */
    @Pure
    public static @Nonnull <O, E, K, D> ChainingSQLConverter<O, E, K, D> get(@Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull AbstractSQLConverter<K, ? super D> SQLConverter) {
        return new ChainingSQLConverter<>(keyConverter, SQLConverter);
    }
    
    /* -------------------------------------------------- Storing (with Statement) -------------------------------------------------- */
    
    @Override
    public final void getValues(@Nonnull O object, @NonCapturable @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull ColumnIndex index) {
        SQLConverter.getValues(keyConverter.convert(object), values, index);
    }
    
    /* -------------------------------------------------- Storing (with PreparedStatement) -------------------------------------------------- */
    
    @Override
    @NonCommitting
    public final void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, @Nonnull ColumnIndex parameterIndex) throws SQLException {
        SQLConverter.storeNonNullable(keyConverter.convert(object), preparedStatement, parameterIndex);
    }
    
    /* -------------------------------------------------- Restoring -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O restoreNullable(@Nonnull E external, @Nonnull ResultSet resultSet, @Nonnull ColumnIndex columnIndex) throws SQLException {
        final @Nullable K key = SQLConverter.restoreNullable(keyConverter.decompose(external), resultSet, columnIndex);
        if (key == null) { return null; }
        try {
            if (!keyConverter.isValid(key)) { throw new InvalidEncodingException("The restored key '" + key + "' is invalid."); }
            return keyConverter.recover(external, key);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException(exception);
        }
    }
    
}
