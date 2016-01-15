package net.digitalid.service.core.converter.xdf;

import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.utility.validation.state.Stateless;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.conversion.Converter;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.Convertible;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

/**
 * Converts {@link Convertible} objects to and from XDF blocks.
 * 
 * @param <T> The type of the object to which an XDF block is converted to or from.
 */
@Stateless
public abstract class XDFConverter<T> extends Converter {
    
    /* -------------------------------------------------- Field Converter -------------------------------------------------- */
    
    /**
     * Recovers a nullable object form an XDF block for a field.
     */
    protected @Nullable Object recoverNullable(@Nullable Block block, @Nonnull Field field) throws InvalidEncodingException, RecoveryException, InternalException {
        final @Nonnull XDFConverter<?> fieldConverter = XDF.FORMAT.getConverter(field);
        final @Nonnull @NonNullableElements ConverterAnnotations annotations = getAnnotations(field);
        return fieldConverter.recoverNullable(block, field.getType(), annotations);
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Recovers a nullable object form a nullable XDF block.
     */
    public @Nullable T recoverNullable(@Nullable Block block, @Nonnull Class<?> type, @Nullable @NonNullableElements ConverterAnnotations annotations) throws InvalidEncodingException, RecoveryException, InternalException {
        if (annotations == null) {
            annotations = ConverterAnnotations.get();
        }
        return block == null ? null : recoverNonNullable(block, type, annotations);
    }
    
    /**
     * Converts a non-nullable object from a non-nullable XDF block.
     */ 
    protected abstract @Nonnull T recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull @NonNullableElements ConverterAnnotations annotations) throws InvalidEncodingException, RecoveryException, InternalException;
    
    /* -------------------------------------------------- Converting -------------------------------------------------- */
    
    /**
     * Converts a nullable object into a nullable XDF block.
     */ 
    public @Nullable Block convertNullable(@Nullable Object value, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nullable @NonNullableElements ConverterAnnotations annotations) throws InternalException, StoringException {
        if (annotations == null) {
            annotations = ConverterAnnotations.get();
        }
        return value == null ? null : convertNonNullable(value, type, fieldName, parentName, annotations);
    }

    /**
     * Converts a non-nullable object into a non-nullable XDF block.
     */
    public abstract @Nonnull Block convertNonNullable(@Nonnull Object value, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull @NonNullableElements ConverterAnnotations annotations) throws InternalException, StoringException;
    
    /* -------------------------------------------------- Semantic Type Generation -------------------------------------------------- */
    
    /**
     * Generates a semantic type identifier string on the field`s or type's name and the optional parent's name.
     */
    private @Nonnull String generateSemanticTypeIdentifier(@Nonnull String name, @Nullable String parentName) {
        if (parentName == null) {
            parentName = "";
        }
        if (parentName.length() > 0) {
            parentName = "." + parentName;
        }       
        return name + parentName + "@core.digitalid.net";
    }
    
    /**
     * Generates a semantic type based on the field`s or type's name and the optional parent's name.
     */
    protected @Nonnull SemanticType generateSemanticType(@Nonnull String name, @Nullable String parentName, @Nonnull SemanticType semanticBase) {
        return SemanticType.map(generateSemanticTypeIdentifier(name, parentName)).load(semanticBase);
    }
    
    /**
     * Generates a semantic type based on the field`s or type's name and the optional parent's name.
     */
    protected @Nonnull SemanticType generateSemanticType(@Nonnull String name, @Nullable String parentName, @Nonnull SyntacticType syntacticType) {
        return SemanticType.map(generateSemanticTypeIdentifier(name, parentName)).load(syntacticType);
    }
    
}
