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
// TODO: Remove this class once the database module can handle all of this.
//
//package net.digitalid.core.property.value;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.collections.freezable.FreezableList;
//import net.digitalid.utility.collections.list.FreezableLinkedList;
//import net.digitalid.utility.collections.list.ReadOnlyList;
//import net.digitalid.utility.collections.readonly.ReadOnlyArray;
//import net.digitalid.utility.collections.tuples.FreezablePair;
//import net.digitalid.utility.collections.tuples.ReadOnlyPair;
//import net.digitalid.utility.conversion.None;
//import net.digitalid.utility.freezable.annotations.Frozen;
//import net.digitalid.utility.exceptions.ExternalException;
//import net.digitalid.utility.system.thread.annotations.MainThread;
//import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
//import net.digitalid.utility.validation.annotations.state.Validated;
//import net.digitalid.utility.validation.annotations.type.Immutable;
//
//import net.digitalid.database.annotations.transaction.Locked;
//import net.digitalid.database.annotations.transaction.NonCommitting;
//import net.digitalid.database.core.exceptions.DatabaseException;
//import net.digitalid.database.core.table.Site;
//import net.digitalid.database.interfaces.Database;
//
//import net.digitalid.core.concept.Concept;
//import net.digitalid.core.conversion.Block;
//import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
//import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
//import net.digitalid.core.property.ConceptPropertyTable;
//
//import net.digitalid.service.core.auxiliary.Time;
//import net.digitalid.service.core.concepts.agent.Agent;
//import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
//import net.digitalid.service.core.concepts.agent.Restrictions;
//import net.digitalid.service.core.converter.Converters;
//import net.digitalid.service.core.entity.Entity;
//import net.digitalid.service.core.property.ReadOnlyProperty;
//import net.digitalid.service.core.site.host.Host;
//
///**
// * This class models a database table that stores a non-nullable {@link ReadOnlyProperty property} of a {@link Concept concept}.
// */
//@Immutable
//public final class OutdatedValuePropertyTableImplementation<V, C extends Concept<C, E, ?>, E extends Entity> extends ConceptPropertyTable<V, C, E> {
//    
//    /* -------------------------------------------------- Constructor -------------------------------------------------- */
//    
//    /**
//     * Creates a new non-nullable property table with the given configuration from the property setup.
//     * 
//     * @param propertySetup
//     */
//    @MainThread
//    OutdatedValuePropertyTableImplementation(@Nonnull NonNullableConceptPropertySetup<V, C, E> propertySetup) {
//        super(propertySetup);
//    }
//    
//    /* -------------------------------------------------- ClientTable -------------------------------------------------- */
//    
//    @Locked
//    @Override
//    @NonCommitting
//    public void createTables(@Nonnull Site site) throws DatabaseException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            Converters<E, Site> entityConverters = getPropertyFactory().getConceptSetup().getEntityConverters();
//            ConceptConverters<C, E> conceptConverters = getPropertyFactory().getConceptSetup().getConceptConverters();
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + getName(site) + " (" + entityConverters.getSQLConverter().getDeclaration() + ", " + conceptConverters.getSQLConverter().getDeclaration() + ", " + Time.SQL_CONVERTER.getDeclaration() + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getDeclaration() + ", PRIMARY KEY (" + entityConverters.getSQLConverter().getSelection() + ", " + conceptConverters.getSQLConverter().getSelection() + ")" + entityConverters.getSQLConverter().getForeignKeys(site) + conceptConverters.getSQLConverter().getForeignKeys(site) + getPropertyFactory().getValueConverters().getSQLConverter().getForeignKeys(site) + ")");
//            Database.onInsertIgnore(statement, getName(site), entityConverters.getSQLConverter().getSelection(), conceptConverters.getSQLConverter().getSelection()); // TODO: There is a problem when the entity or the concept uses more than one column because the onInsertIgnore-method expects the arguments differently.
//            // TODO: Shouldn't we detect here whether we need to call Mapper.addReference?
//        } catch (@Nonnull SQLException exception) {
//            throw DatabaseException.get(exception);
//        }
//    }
//    
//    @Locked
//    @Override
//    @NonCommitting
//    public void deleteTables(@Nonnull Site site) throws DatabaseException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            Database.onInsertNotIgnore(statement, getName(site));
//            statement.executeUpdate("DROP TABLE IF EXISTS " + getName(site));
//        } catch (@Nonnull SQLException exception) {
//            throw DatabaseException.get(exception);
//        }
//    }
//    
//    /* -------------------------------------------------- HostTable -------------------------------------------------- */
//    
//    @Pure
//    @Locked
//    @Override
//    @NonCommitting
//    public @Nonnull Block exportAll(@Nonnull Host host) throws DatabaseException {
//        Converters<E, Site> entityConverters = getPropertyFactory().getConceptSetup().getEntityConverters();
//        ConceptConverters<C, E> conceptConverters = getPropertyFactory().getConceptSetup().getConceptConverters();
//        final @Nonnull String SQL = "SELECT " + entityConverters.getSQLConverter().getSelection() + ", " + conceptConverters.getSQLConverter().getSelection() + ", " + Time.SQL_CONVERTER.getSelection() + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getSelection() + " FROM " + getName(host);
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
//            final @Nonnull FreezableList<Block> entries = FreezableLinkedList.get();
//            while (resultSet.next()) {
//                int startIndex = 0;
//                final @Nonnull E entity = entityConverters.getSQLConverter().restoreNonNullable(host, resultSet, startIndex);
//                startIndex += entityConverters.getSQLConverter().getNumberOfColumns();
//                final @Nonnull C concept = conceptConverters.getSQLConverter().restoreNonNullable(entity, resultSet, startIndex);
//                startIndex += conceptConverters.getSQLConverter().getNumberOfColumns();
//                final @Nonnull Time time = Time.SQL_CONVERTER.restoreNonNullable(None.OBJECT, resultSet, startIndex);
//                startIndex += Time.SQL_CONVERTER.getNumberOfColumns();
//                final @Nonnull V value = getPropertyFactory().getValueConverters().getSQLConverter().restoreNonNullable(entity, resultSet, startIndex);
//                entries.add(TupleWrapper.encode(getDumpType().getParameters().getNonNullable(0), entity, concept, time, getPropertyFactory().getValueConverters().getXDFConverter().encodeNonNullable(value)));
//            }
//            return ListWrapper.encode(getDumpType(), entries.freeze());
//        } catch (SQLException exception) {
//            throw DatabaseException.get(exception);
//        }
//    }
//    
//    @Locked
//    @Override
//    @NonCommitting
//    public void importAll(@Nonnull Host host, @Nonnull Block block) throws ExternalException {
//        Require.that(block.getType().isBasedOn(getDumpType())).orThrow("The block is based on the dump type of this data collection.");
//        
//        Converters<E, Site> entityConverters = getPropertyFactory().getConceptSetup().getEntityConverters();
//        ConceptConverters<C, E> conceptConverters = getPropertyFactory().getConceptSetup().getConceptConverters();
//        final @Nonnull String SQL = "INSERT INTO " + getName(host) + " (" + entityConverters.getSQLConverter().getSelection() + ", " + conceptConverters.getSQLConverter().getSelection() + ", " + Time.SQL_CONVERTER.getSelection() + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getSelection() + ") VALUES (" + entityConverters.getSQLConverter().getInsertForPreparedStatement() + ", " + conceptConverters.getSQLConverter().getInsertForPreparedStatement() + ", " + Time.SQL_CONVERTER.getInsertForPreparedStatement() + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getInsertForPreparedStatement() + ")";
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//            final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
//            for (final @Nonnull Block entry : entries) {
//                final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
//                int startIndex = 0;
//                
//                final @Nonnull E entity = entityConverters.getXDFConverter().decodeNonNullable(host, elements.getNonNullable(0));
//                entityConverters.getSQLConverter().storeNonNullable(entity, preparedStatement, startIndex);
//                startIndex += entityConverters.getSQLConverter().getNumberOfColumns();
//                
//                final @Nonnull C concept = conceptConverters.getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(1));
//                conceptConverters.getSQLConverter().storeNonNullable(concept, preparedStatement, startIndex);
//                startIndex += conceptConverters.getSQLConverter().getNumberOfColumns();
//                
//                final @Nonnull Time time = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(2));
//                Time.SQL_CONVERTER.storeNonNullable(time, preparedStatement, startIndex);
//                startIndex += Time.SQL_CONVERTER.getNumberOfColumns();
//                
//                final @Nonnull V value = getPropertyFactory().getValueConverters().getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(3));
//                getPropertyFactory().getValueConverters().getSQLConverter().storeNonNullable(value, preparedStatement, startIndex);
//                
//                preparedStatement.addBatch();
//            }
//            preparedStatement.executeBatch();
//        } catch (SQLException exception) {
//            throw DatabaseException.get(exception);
//        }
//    }
//    
//    /* -------------------------------------------------- StateTable -------------------------------------------------- */
//    
//    @Pure
//    @Locked
//    @Override
//    @NonCommitting
//    public @Nonnull Block getState(@Nonnull E entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
//        Converters<E, Site> entityConverters = getPropertyFactory().getConceptSetup().getEntityConverters();
//        ConceptConverters<C, E> conceptConverters = getPropertyFactory().getConceptSetup().getConceptConverters();
//        // TODO: String SQL = select(getConceptConverters(), Time.CONVERTERS, getPropertyFactory().getValueConverters()).from(entity).where(factory, object).and().and().toSQL();
//        // TODO: Instead of conceptConverters.getSQLConverter().storeNonNullable and conceptConverters.getSQLConverter().restoreNonNullable, one could define a store and restore method that also takes a GeneralConverters as a parameter.
//        final @Nonnull String SQL = "SELECT " + conceptConverters.getSQLConverter().getSelection() + ", " + Time.SQL_CONVERTER.getSelection() + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getSelection() + " FROM " + getName(entity.getSite()) + " WHERE " + entityConverters.getSQLConverter().getConditionForStatement(entity) + " AND " + getPropertyFactory().getRequiredAuthorization().getStateFilter(permissions, restrictions, agent);
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
//            final @Nonnull FreezableList<Block> entries = FreezableLinkedList.get();
//            while (resultSet.next()) {
//                int startIndex = 0;
//                final @Nonnull C concept = conceptConverters.getSQLConverter().restoreNonNullable(entity, resultSet, startIndex);
//                startIndex += conceptConverters.getSQLConverter().getNumberOfColumns();
//                final @Nonnull Time time = Time.SQL_CONVERTER.restoreNonNullable(None.OBJECT, resultSet, startIndex);
//                startIndex += Time.SQL_CONVERTER.getNumberOfColumns();
//                final @Nonnull V value = getPropertyFactory().getValueConverters().getSQLConverter().restoreNonNullable(entity, resultSet, startIndex);
//                entries.add(TupleWrapper.encode(getStateType().getParameters().getNonNullable(0), concept, time, getPropertyFactory().getValueConverters().getXDFConverter().encodeNonNullable(value)));
//            }
//            return ListWrapper.encode(getStateType(), entries.freeze());
//        } catch (SQLException exception) {
//            throw DatabaseException.get(exception);
//        }
//    }
//    
//    @Locked
//    @Override
//    @NonCommitting
//    public void addState(@Nonnull E entity, @Nonnull Block block) throws ExternalException {
//        Require.that(block.getType().isBasedOn(getStateType())).orThrow("The block is based on the state type of this data collection.");
//        
//        Converters<E, Site> entityConverters = getPropertyFactory().getConceptSetup().getEntityConverters();
//        ConceptConverters<C, E> conceptConverters = getPropertyFactory().getConceptSetup().getConceptConverters();
//        final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + getName(entity.getSite()) + " (" + entityConverters.getSQLConverter().getSelection() + ", " + conceptConverters.getSQLConverter().getSelection() + ", " + Time.SQL_CONVERTER.getSelection() + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getSelection() + ") VALUES (" + entityConverters.getSQLConverter().getInsertForPreparedStatement() + ", " + conceptConverters.getSQLConverter().getInsertForPreparedStatement() + ", " + Time.SQL_CONVERTER.getInsertForPreparedStatement() + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getInsertForPreparedStatement() + ")";
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//            final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
//            for (final @Nonnull Block entry : entries) {
//                final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
//                int startIndex = 0;
//                
//                entityConverters.getSQLConverter().storeNonNullable(entity, preparedStatement, startIndex);
//                startIndex += entityConverters.getSQLConverter().getNumberOfColumns();
//                
//                final @Nonnull C concept = conceptConverters.getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(0));
//                conceptConverters.getSQLConverter().storeNonNullable(concept, preparedStatement, startIndex);
//                startIndex += conceptConverters.getSQLConverter().getNumberOfColumns();
//                
//                final @Nonnull Time time = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(1));
//                Time.SQL_CONVERTER.storeNonNullable(time, preparedStatement, startIndex);
//                startIndex += Time.SQL_CONVERTER.getNumberOfColumns();
//                
//                final @Nonnull V value = getPropertyFactory().getValueConverters().getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(2));
//                getPropertyFactory().getValueConverters().getSQLConverter().storeNonNullable(value, preparedStatement, startIndex);
//                
//                preparedStatement.addBatch();
//            }
//            preparedStatement.executeBatch();
//        } catch (SQLException exception) {
//            throw DatabaseException.get(exception);
//        }
//        
//        getPropertyFactory().getConceptSetup().getConceptIndex().reset(entity, this);
//    }
//    
//    /* -------------------------------------------------- Methods -------------------------------------------------- */
//    
//    @Pure
//    @Nonnull @NonNullableElements @Frozen ReadOnlyPair<Time, V> load(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull NonNullableConceptPropertySetup<V, C, E> propertySetup) throws DatabaseException {
//        Converters<E, Site> entityConverters = getPropertyFactory().getConceptSetup().getEntityConverters();
//        ConceptConverters<C, E> conceptConverters = getPropertyFactory().getConceptSetup().getConceptConverters();
//        final @Nonnull E entity = property.getConcept().getEntity();
//        final @Nonnull String SQL = "SELECT " + Time.SQL_CONVERTER.getSelection() + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getSelection() + " FROM " + getName(entity.getSite()) + " WHERE " + entityConverters.getSQLConverter().getConditionForStatement(entity) + " AND " + conceptConverters.getSQLConverter().getConditionForStatement(property.getConcept());
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
//            if (resultSet.next()) {
//                int startIndex = 0;
//                final @Nonnull Time time = Time.SQL_CONVERTER.restoreNonNullable(None.OBJECT, resultSet, startIndex);
//                startIndex += Time.SQL_CONVERTER.getNumberOfColumns();
//                final @Nonnull V value = getPropertyFactory().getValueConverters().getSQLConverter().restoreNonNullable(entity, resultSet, startIndex);
//                if (!property.getValueValidator().isValid(value)) { throw new SQLException("The value of the given property is invalid."); }
//                return FreezablePair.get(time, value).freeze();
//            } else {
//                // TODO: What about changes
//                return FreezablePair.get(Time.getCurrent(), propertySetup.getDefaultValue()).freeze();
//            }
//        } catch (SQLException exception) {
//            throw DatabaseException.get(exception);
//        }
//    }
//    
//    void replace(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) throws DatabaseException {
//        Converters<E, Site> entityConverters = getPropertyFactory().getConceptSetup().getEntityConverters();
//        ConceptConverters<C, E> conceptConverters = getPropertyFactory().getConceptSetup().getConceptConverters();
//        final @Nonnull E entity = property.getConcept().getEntity();
//        final @Nonnull String SQL = "UPDATE " + getName(entity.getSite()) + " SET " + Time.SQL_CONVERTER.getUpdateForStatement(newTime) + ", " + getPropertyFactory().getValueConverters().getSQLConverter().getUpdateForPreparedStatement() + " WHERE " + entityConverters.getSQLConverter().getConditionForStatement(entity) + " AND " + conceptConverters.getSQLConverter().getConditionForStatement(property.getConcept()) + " AND " + Time.SQL_CONVERTER.getConditionForStatement(oldTime) + " AND " + getPropertyFactory().getValueConverters().getSQLConverter().getConditionForPreparedStatement();
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//            int startIndex = 0;
//            getPropertyFactory().getValueConverters().getSQLConverter().storeNonNullable(newValue, preparedStatement, startIndex);
//            startIndex += getPropertyFactory().getValueConverters().getSQLConverter().getNumberOfColumns();
//            getPropertyFactory().getValueConverters().getSQLConverter().storeNonNullable(oldValue, preparedStatement, startIndex);
//            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The value of the given property could not be replaced."); }
//        } catch (SQLException exception) {
//            throw DatabaseException.get(exception);
//        }
//    }
//    
//}
