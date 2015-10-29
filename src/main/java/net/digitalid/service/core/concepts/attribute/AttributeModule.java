package net.digitalid.service.core.concepts.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.BooleanWrapper;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.dataservice.Service;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.EntityClass;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.expression.PassiveExpression;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.IdentityClass;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.site.Site;

/**
 * This class provides database access to the {@link Attribute attributes} of the core service.
 */
@Stateless
public final class AttributeModule implements StateModule {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module Initialization –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores an instance of this module.
     */
    public static final AttributeModule MODULE = new AttributeModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Table Creation and Deletion –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "attribute_value (entity " + EntityClass.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, published BOOLEAN NOT NULL, value " + AttributeValue.FORMAT + " NOT NULL, PRIMARY KEY (entity, type, published), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "attribute_visibility (entity " + EntityClass.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, visibility " + PassiveExpression.FORMAT + ", PRIMARY KEY (entity, type), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "attribute_visibility");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "attribute_value");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module Export and Import –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code entry.value.attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE_MODULE_ENTRY = SemanticType.map("entry.value.attribute.module@core.digitalid.net").load(TupleWrapper.TYPE, Identity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, Attribute.PUBLISHED, AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code table.value.attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE_MODULE_TABLE = SemanticType.map("table.value.attribute.module@core.digitalid.net").load(ListWrapper.TYPE, VALUE_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.visibility.attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VISIBILITY_MODULE_ENTRY = SemanticType.map("entry.visibility.attribute.module@core.digitalid.net").load(TupleWrapper.TYPE, Identity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code table.visibility.attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VISIBILITY_MODULE_TABLE = SemanticType.map("table.visibility.attribute.module@core.digitalid.net").load(ListWrapper.TYPE, VISIBILITY_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("attribute.module@core.digitalid.net").load(TupleWrapper.TYPE, VALUE_MODULE_TABLE, VISIBILITY_MODULE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws AbortException {
        final @Nonnull FreezableArray<Block> tables = new FreezableArray<>(2);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, type, published, value FROM " + host + "attribute_value")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 2);
                    final boolean published = resultSet.getBoolean(3);
                    final @Nonnull Block value = Block.getNotNull(AttributeValue.TYPE, resultSet, 4);
                    entries.add(new TupleWrapper(VALUE_MODULE_ENTRY, identity, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new BooleanWrapper(Attribute.PUBLISHED, published), value.toBlockable()).toBlock());
                }
                tables.set(0, new ListWrapper(VALUE_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, type, visibility FROM " + host + "attribute_visibility")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 2);
                    final @Nonnull String visibility = resultSet.getString(3);
                    entries.add(new TupleWrapper(VISIBILITY_MODULE_ENTRY, identity, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new StringWrapper(PassiveExpression.TYPE, visibility)).toBlock());
                }
                tables.set(1, new ListWrapper(VISIBILITY_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
        }
        return new TupleWrapper(MODULE_FORMAT, tables.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadOnlyArray<Block> tables = new TupleWrapper(block).getNonNullableElements(2);
        final @Nonnull String prefix = "INSERT INTO " + host;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(tables.getNonNullable(0)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(entry).getNonNullableElements(4);
                IdentityClass.create(elements.getNonNullable(0)).toInternalIdentity().set(preparedStatement, 1);
                IdentityClass.create(elements.getNonNullable(1)).toSemanticType().checkIsAttributeType().set(preparedStatement, 2);
                preparedStatement.setBoolean(3, new BooleanWrapper(elements.getNonNullable(2)).getValue());
                elements.getNonNullable(3).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(tables.getNonNullable(1)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(entry).getNonNullableElements(3);
                IdentityClass.create(elements.getNonNullable(0)).toInternalIdentity().set(preparedStatement, 1);
                IdentityClass.create(elements.getNonNullable(1)).toSemanticType().checkIsAttributeType().set(preparedStatement, 2);
                preparedStatement.setString(2, new StringWrapper(elements.getNonNullable(2)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Getter and Setter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code entry.value.attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE_STATE_ENTRY = SemanticType.map("entry.value.attribute.state@core.digitalid.net").load(TupleWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, Attribute.PUBLISHED, AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code table.value.attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE_STATE_TABLE = SemanticType.map("table.value.attribute.state@core.digitalid.net").load(ListWrapper.TYPE, VALUE_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.visibility.attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VISIBILITY_STATE_ENTRY = SemanticType.map("entry.visibility.attribute.state@core.digitalid.net").load(TupleWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code table.visibility.attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VISIBILITY_STATE_TABLE = SemanticType.map("table.visibility.attribute.state@core.digitalid.net").load(ListWrapper.TYPE, VISIBILITY_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("attribute.state@core.digitalid.net").load(TupleWrapper.TYPE, VALUE_STATE_TABLE, VISIBILITY_STATE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws AbortException {
        final @Nonnull Site site = entity.getSite();
        final @Nonnull FreezableArray<Block> tables = new FreezableArray<>(2);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT type, published, value FROM " + site + "attribute_value WHERE entity = " + entity + permissions.allTypesToString())) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 1);
                    final boolean published = resultSet.getBoolean(2);
                    final @Nonnull Block value = Block.getNotNull(AttributeValue.TYPE, resultSet, 3);
                    entries.add(new TupleWrapper(VALUE_STATE_ENTRY, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new BooleanWrapper(Attribute.PUBLISHED, published), value.toBlockable()).toBlock());
                }
                tables.set(0, new ListWrapper(VALUE_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT type, visibility FROM " + site + "attribute_visibility WHERE entity = " + entity + permissions.writeTypesToString())) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 1);
                    final @Nonnull String visibility = resultSet.getString(2);
                    entries.add(new TupleWrapper(VISIBILITY_STATE_ENTRY, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new StringWrapper(PassiveExpression.TYPE, visibility)).toBlock());
                }
                tables.set(1, new ListWrapper(VISIBILITY_STATE_TABLE, entries.freeze()).toBlock());
            }
            
        }
        return new TupleWrapper(STATE_FORMAT, tables.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertIgnore(statement, site + "attribute_value", "entity", "type", "published");
            Database.onInsertIgnore(statement, site + "attribute_visibility", "entity", "type");
        }
        
        final @Nonnull ReadOnlyArray<Block> tables = new TupleWrapper(block).getNonNullableElements(2);
        final @Nonnull String prefix = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + site;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(tables.getNonNullable(0)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(entry).getNonNullableElements(3);
                IdentityClass.create(elements.getNonNullable(0)).toSemanticType().checkIsAttributeFor(entity).set(preparedStatement, 2);
                preparedStatement.setBoolean(3, new BooleanWrapper(elements.getNonNullable(1)).getValue());
                elements.getNonNullable(2).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(tables.getNonNullable(1)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(entry).getNonNullableElements(2);
                IdentityClass.create(elements.getNonNullable(0)).toSemanticType().checkIsAttributeFor(entity).set(preparedStatement, 2);
                preparedStatement.setString(3, new StringWrapper(elements.getNonNullable(1)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + "attribute_value");
            Database.onInsertNotIgnore(statement, site + "attribute_visibility");
        }
        
        Attribute.reset(entity);
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws AbortException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "attribute_visibility WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "attribute_value WHERE entity = " + entity);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Attribute Retrieval –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns all the attributes of the given entity.
     * 
     * @param entity the entity whose attributes are to be returned.
     * 
     * @return all the attributes of the given entity.
     * 
     * @ensure return.!isFrozen() : "The returned attributes are not frozen.";
     */
    @Pure
    @NonCommitting
    static @Capturable @Nonnull FreezableSet<Attribute> getAll(@Nonnull Entity entity) throws AbortException {
        final @Nonnull String SQL = "SELECT DISTINCT type FROM " + entity.getSite() + "attribute_value WHERE entity = " + entity;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableSet<Attribute> attributes = new FreezableLinkedHashSet<>();
            while (resultSet.next()) attributes.add(Attribute.get(entity, IdentityClass.getNotNull(resultSet, 1).toSemanticType().checkIsAttributeFor(entity)));
            return attributes;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Attribute Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the value of the given attribute or null if no value is found.
     * 
     * @param attribute the attribute whose value is to be returned.
     * @param published whether the attribute is already published.
     * 
     * @return the value of the given attribute or null if no value is found.
     * 
     * @ensure return == null || return.isVerified() && return.matches(attribute) : "The returned value is null or verified and matches the given attribute.";
     */
    @Pure
    @NonCommitting
    static @Nullable AttributeValue getValue(@Nonnull Attribute attribute, boolean published) throws AbortException {
        final @Nonnull String SQL = "SELECT value FROM " + attribute.getEntity().getSite() + "attribute_value WHERE entity = " + attribute.getEntity() + " AND type = " + attribute.getType() + " AND published = " + Database.toBoolean(published);
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return AttributeValue.get(resultSet, 1).checkMatches(attribute);
            else return null;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Inserts the given value for the given attribute.
     * 
     * @param attribute the attribute for which the value is to be inserted.
     * @param published whether the published value is to be inserted.
     * @param value the value which is to be inserted for the attribute.
     * 
     * @require value.isVerified() && value.matches(attribute) : "The value is verified and matches the given attribute.";
     */
    @NonCommitting
    static void insertValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue value) throws AbortException {
        assert value.isVerified() && value.matches(attribute) : "The value is verified and matches the given attribute.";
        
        final @Nonnull String SQL = "INSERT INTO " + attribute.getEntity().getSite() + "attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            preparedStatement.setBoolean(3, published);
            value.set(preparedStatement, 4);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Deletes the given value from the given attribute.
     * 
     * @param attribute the attribute whose value is to be deleted.
     * @param published whether the published value is to be deleted.
     * @param value the value which is to be deleted from the attribute.
     * 
     * @require value.isVerified() && value.matches(attribute) : "The value is verified and matches the given attribute.";
     */
    static void deleteValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue value) throws AbortException {
        assert value.isVerified() && value.matches(attribute) : "The value is verified and matches the given attribute.";
        
        final @Nonnull String SQL = "DELETE FROM " + attribute.getEntity().getSite() + "attribute_value WHERE entity = ? AND type = ? AND published = ? AND value = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            preparedStatement.setBoolean(3, published);
            value.set(preparedStatement, 4);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The value of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be deleted.");
        }
    }
    
    /**
     * Replaces the value of the given attribute.
     * 
     * @param attribute the attribute whose value is to be replaced.
     * @param published whether to replace the published or unpublished value.
     * @param oldValue the old value to be replaced by the new value.
     * @param newValue the new value by which the old value is replaced.
     * 
     * @require oldValue.isVerified() && oldValue.matches(attribute) : "The old value is verified and matches the given attribute.";
     * @require newValue.isVerified() && newValue.matches(attribute) : "The new value is verified and matches the given attribute.";
     */
    @NonCommitting
    static void replaceValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue oldValue, @Nonnull AttributeValue newValue) throws AbortException {
        assert oldValue.isVerified() && oldValue.matches(attribute) : "The old value is verified and matches the given attribute.";
        assert newValue.isVerified() && newValue.matches(attribute) : "The new value is verified and matches the given attribute.";
        
        final @Nonnull String SQL = "UPDATE " + attribute.getEntity().getSite() + "attribute_value SET value = ? WHERE entity = ? AND type = ? AND published = ? AND value = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newValue.set(preparedStatement, 1);
            attribute.getEntity().set(preparedStatement, 2);
            attribute.getType().set(preparedStatement, 3);
            preparedStatement.setBoolean(4, published);
            oldValue.set(preparedStatement, 5);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The value of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be replaced.");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Attribute Visibility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the visibility of the given attribute or null if no visibility is found.
     * 
     * @param attribute the attribute whose visibility is to be returned.
     * 
     * @return the visibility of the attribute with the given type of the given entity or null if no such visibility is available.
     * 
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
     * 
     * @ensure return == null || return.getEntity().equals(attribute.getEntity()) : "The returned visibility is null or belongs to the entity of the given attribute.";
     */
    @Pure
    @NonCommitting
    static @Nullable PassiveExpression getVisibility(@Nonnull Attribute attribute) throws AbortException {
        assert attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
        
        final @Nonnull String SQL = "SELECT visibility FROM " + attribute.getEntity().getSite() + "attribute_visibility WHERE entity = " + attribute.getEntity() + " AND type = " + attribute.getType();
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return PassiveExpression.get((NonHostEntity) attribute.getEntity(), resultSet, 1);
            else return null;
        }
    }
    
    /**
     * Inserts the given visibility for the given attribute.
     * 
     * @param attribute the attribute for which the visibility is to be inserted.
     * @param visibility the visibility which is to be inserted for the attribute.
     * 
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
     * @require visibility.getEntity().equals(attribute.getEntity()) : "The visibility and the attribute belong to the same entity.";
     */
    @NonCommitting
    static void insertVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression visibility) throws AbortException {
        assert attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
        assert visibility.getEntity().equals(attribute.getEntity()) : "The visibility and the attribute belong to the same entity.";
        
        final @Nonnull String SQL = "INSERT INTO " + attribute.getEntity().getSite() + "attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            visibility.set(preparedStatement, 3);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Deletes the given visibility from the given attribute.
     * 
     * @param attribute the attribute whose visibility is to be deleted.
     * @param visibility the visibility which is to be deleted from the attribute.
     * 
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
     * @require visibility.getEntity().equals(attribute.getEntity()) : "The visibility and the attribute belong to the same entity.";
     */
    @NonCommitting
    static void deleteVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression visibility) throws AbortException {
        assert attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
        assert visibility.getEntity().equals(attribute.getEntity()) : "The visibility and the attribute belong to the same entity.";
        
        final @Nonnull String SQL = "DELETE FROM " + attribute.getEntity().getSite() + "attribute_visibility WHERE entity = ? AND type = ? AND visibility = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            visibility.set(preparedStatement, 3);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The visibility of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be deleted.");
        }
    }
    
    /**
     * Replaces the visibility of the given attribute.
     * 
     * @param attribute the attribute whose visibility is to be replaced.
     * @param oldVisibility the old visibility to be replaced by the new visibility.
     * @param newVisibility the new visibility by which the old visibility is replaced.
     * 
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
     * @require oldVisibility.getEntity().equals(attribute.getEntity()) : "The old visibility and the attribute belong to the same entity.";
     * @require newVisibility.getEntity().equals(attribute.getEntity()) : "The new visibility and the attribute belong to the same entity.";
     */
    @NonCommitting
    static void replaceVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression oldVisibility, @Nonnull PassiveExpression newVisibility) throws AbortException {
        assert attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
        assert oldVisibility.getEntity().equals(attribute.getEntity()) : "The old visibility and the attribute belong to the same entity.";
        assert newVisibility.getEntity().equals(attribute.getEntity()) : "The new visibility and the attribute belong to the same entity.";
        
        final @Nonnull String SQL = "UPDATE " + attribute.getEntity().getSite() + "attribute_visibility SET visibility = ? WHERE entity = ? AND type = ? AND visibility = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newVisibility.set(preparedStatement, 1);
            attribute.getEntity().set(preparedStatement, 2);
            attribute.getType().set(preparedStatement, 3);
            oldVisibility.set(preparedStatement, 4);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The visibility of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be replaced.");
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
