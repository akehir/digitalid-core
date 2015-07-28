package net.digitalid.core.storable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.collections.ElementConverter;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.IterableConverter;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.database.Column;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * The factory allows to store and restore objects.
 * 
 * @see Storable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class NonHostConceptFactory<O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type that corresponds to the storable class.
     */
    private final @Nonnull @Loaded SemanticType type;
    
    /**
     * Returns the semantic type that corresponds to the storable class.
     * 
     * @return the semantic type that corresponds to the storable class.
     */
    @Pure
    public final @Nonnull @Loaded SemanticType getType() {
        return type;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes the given non-nullable object as a new block.
     * 
     * @param object the non-nullable object to encode as a block.
     * 
     * @return the given non-nullable object encoded as a new block.
     * 
     * @ensure return.getType().equals(getType()) : "The returned block has the indicated type.";
     */
    @Pure
    public abstract @Nonnull Block encodeNonNullable(@Nonnull O object);
    
    /**
     * Encodes the given nullable object as a new block.
     * 
     * @param object the nullable object to encode as a block.
     * 
     * @return the given nullable object encoded as a new block.
     * 
     * @ensure return == null || return.getType().equals(getType()) : "The returned block is either null or has the indicated type.";
     */
    @Pure
    public final @Nullable Block encodeNullable(@Nullable O object) {
        if (object != null) return encodeNonNullable(object);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the given non-nullable block.
     * 
     * @param entity the entity to which the block belongs.
     * @param block the non-nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the non-nullable block.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    @Pure
    public abstract @Nonnull O decodeNonNullable(@Nonnull NonHostEntity entity, @Nonnull Block block) throws InvalidEncodingException;
    
    /**
     * Decodes the given nullable block.
     * 
     * @param entity the entity to which the block belongs.
     * @param block the nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the nullable block.
     * 
     * @require block == null || block.getType().isBasedOn(getType()) : "The block is either null or based on the indicated type.";
     */
    @Pure
    public final @Nullable O decodeNullable(@Nonnull NonHostEntity entity, @Nullable Block block) throws InvalidEncodingException {
        if (block != null) return decodeNonNullable(entity, block);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Columns –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the columns used to store objects of the storable class in the database.
     */
    private final @Nonnull @Frozen @NonNullableElements ReadOnlyArray<Column> columns;
    
    /**
     * Returns the columns used to store objects of the storable class in the database.
     * 
     * @return the columns used to store objects of the storable class in the database.
     */
    @Pure
    public final @Nonnull @Frozen ReadOnlyArray<Column> getColumns() {
        return columns;
    }
    
    /**
     * Returns the number of columns used to store objects of the storable class in the database.
     * 
     * @return the number of columns used to store objects of the storable class in the database.
     */
    @Pure
    public final int getNumberOfColumns() {
        return columns.size();
    }
    
    /**
     * Stores the maximum length of the column names.
     */
    private final int maximumColumnLength;
    
    /**
     * Returns the maximum length of the column names.
     * 
     * @return the maximum length of the column names.
     */
    @Pure
    public final int getMaximumColumnLength() {
        return columns.size();
    }
    
    /**
     * Returns whether the given prefix is valid.
     * 
     * @param prefix the prefix to be checked.
     * 
     * @return whether the given prefix is valid.
     */
    @Pure
    public final boolean isValidPrefix(@Nonnull String prefix) {
        return prefix.isEmpty() || prefix.length() + maximumColumnLength < 22 && Database.getConfiguration().isValidIdentifier(prefix);
    }
    
    /**
     * Returns the columns as declaration with the given prefix.
     * 
     * @param prefix the prefix to prepend to all column names.
     * 
     * @return the columns as declaration with the given prefix.
     */
    @Pure
    public final @Nonnull String getDeclaration(final @Nonnull @Validated String prefix) {
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        return IterableConverter.toString(columns, new ElementConverter<Column>() { @Pure @Override public String toString(@Nullable Column column) { return prefix + "_" + String.valueOf(column); } });
    }
    
    /**
     * Returns the columns as declaration without a prefix.
     * 
     * @return the columns as declaration without a prefix.
     */
    @Pure
    public final @Nonnull String getDeclaration() {
        return IterableConverter.toString(columns);
    }
    
    /**
     * Returns the columns for selection with the given prefix.
     * 
     * @param prefix the prefix to prepend to all column names.
     * 
     * @return the columns for selection with the given prefix.
     */
    @Pure
    public final @Nonnull String getSelection(final @Nonnull @Validated String prefix) {
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        return IterableConverter.toString(columns, new ElementConverter<Column>() { @Pure @Override public String toString(@Nullable Column column) { return prefix + "_" + (column == null ? "null" : column.getName()); } });
    }
    
    /**
     * Returns the columns for selection without a prefix.
     * 
     * @return the columns for selection without a prefix.
     */
    @Pure
    public final @Nonnull String getSelection() {
        return getSelection("");
    }
    
    /**
     * Returns the foreign key constraints of the columns with the given prefix.
     * 
     * @param prefix the prefix that is to be prepended to all column names.
     * @param site the site at which the foreign key constraints are declared.
     * 
     * @return the foreign key constraints of the columns with the given prefix.
     * 
     * @ensure return.isEmpty() || return.startsWith(",") : "The returned string is either empty or starts with a comma.";
     */
    @NonCommitting
    public @Nonnull String getForeignKeys(@Nonnull @Validated String prefix, @Nonnull Site site) throws SQLException {
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        final @Nonnull StringBuilder string = new StringBuilder();
        for (final @Nonnull Column column : columns) {
            string.append(column.getForeignKey(prefix, site));
        }
        return string.toString();
    }
    
    /**
     * Returns the foreign key constraints of the columns without a prefix.
     * 
     * @param prefix the prefix that is to be prepended to all column names.
     * @param site the site at which the foreign key constraints are declared.
     * 
     * @return the foreign key constraints of the columns without a prefix.
     * 
     * @ensure return.isEmpty() || return.startsWith(",") : "The returned string is either empty or starts with a comma.";
     */
    @NonCommitting
    public final @Nonnull String getForeignKeys(@Nonnull Site site) throws SQLException {
        return getForeignKeys("", site);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to the given non-nullable object.
     * The number of parameters that are set is given by {@link #getNumberOfColumns()}.
     * 
     * @param object the non-nullable object which is to be stored in the database.
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    @NonCommitting
    public abstract void setNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to null.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    @NonCommitting
    public final void setNull(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        for (int i = 0; i < columns.size(); i++) {
            preparedStatement.setNull(parameterIndex + i, columns.getNonNullable(i).getType().getCode());
        }
    }
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to the given nullable object.
     * The number of parameters that are set is given by {@link #getNumberOfColumns()}.
     * 
     * @param object the nullable object which is to be stored in the database.
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    @NonCommitting
    public final void setNullable(@Nullable O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        if (object == null) setNull(preparedStatement, parameterIndex);
        else setNonNullable(object, preparedStatement, parameterIndex);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param entity the non-host entity to which the returned object belongs.
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public abstract @Nullable O getNullable(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
    
    /**
     * Returns a non-nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param entity the non-host entity to which the returned object belongs.
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a non-nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public final @Nonnull O getNonNullable(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable O object = getNullable(entity, resultSet, columnIndex);
        if (object == null) throw new SQLException("An object which should not be null was null.");
        return object;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-host concept factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the storable class.
     * @param columns the columns used to store objects of the storable class.
     */
    protected NonHostConceptFactory(@Nonnull @Loaded SemanticType type, @NonNullableElements Column... columns) {
        this.type = type;
        this.columns = new FreezableArray<>(columns).freeze();
        int maximumColumnLength = 0;
        for (final @Nonnull Column column : columns) {
            final int columnLength = column.getName().length();
            if (columnLength > maximumColumnLength) maximumColumnLength = columnLength;
        }
        this.maximumColumnLength = maximumColumnLength;
    }
    
}
