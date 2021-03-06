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
// TODO: Adapt to the new library.

//package net.digitalid.core.node.context;
//
//import java.sql.SQLException;
//
//import javax.annotation.Nonnull;
//
//import net.digitalid.core.node.contact.Contact;
//
//import net.digitalid.database.annotations.transaction.Committing;
//import net.digitalid.database.interfaces.Database;
//
//import net.digitalid.core.context.Context;
//import net.digitalid.core.context.Context;
//import net.digitalid.core.context.FreezableContacts;
//import net.digitalid.core.context.FreezableContacts;
//import net.digitalid.core.server.IdentitySetup;
//
//import org.junit.Assert;
//import org.junit.BeforeClass;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runners.MethodSorters;
//
///**
// * Unit testing of the {@link Context context} with its {@link Action actions}.
// */
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public final class ContextTest extends IdentitySetup {
//    
//    private static @Nonnull Context context;
//    
//    private static @Nonnull Contact contact;
//    
//    @BeforeClass
//    public static void setUpContext() {
//        print("setUpContext");
//        context = Context.getRoot(getRole());
//        contact = Contact.get(getRole(), getSubject());
//    }
//    
//    @Test
//    @Committing
//    public void _01_testEmptyContext() throws DatabaseException {
//        print("_01_testEmptyContext");
//        try {
//            Assert.assertTrue(context.getContacts().isEmpty());
//            Database.commit();
//        } catch (@Nonnull SQLException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @Test
//    @Committing
//    public void _02_testAddContact() throws DatabaseException {
//        print("_02_testAddContact");
//        try {
//            context.addContacts(new FreezableContacts(contact).freeze());
//        } catch (@Nonnull SQLException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @Test
//    @Committing
//    public void _03_testContainsContact() throws DatabaseException {
//        print("_03_testContainsContact");
//        try {
//            Assert.assertTrue(context.contains(contact));
//            Database.commit();
//        } catch (@Nonnull SQLException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @Test
//    @Committing
//    public void _04_testRemoveContact() throws DatabaseException {
//        print("_04_testRemoveContact");
//        try {
//            context.removeContacts(new FreezableContacts(contact).freeze());
//        } catch (@Nonnull SQLException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @Test
//    @Committing
//    public void _05_testEmptyContext() throws DatabaseException {
//        print("_05_testEmptyContext");
//        try {
//            Assert.assertTrue(context.getContacts().isEmpty());
//            Database.commit();
//        } catch (@Nonnull SQLException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//}
