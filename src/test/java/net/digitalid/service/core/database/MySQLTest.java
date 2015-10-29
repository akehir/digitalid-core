package net.digitalid.service.core.database;

import java.io.IOException;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.configuration.MySQLConfiguration;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link MySQLConfiguration}.
 */
public final class MySQLTest extends DatabaseTest {
    
    @Pure
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    @Committing
    public static void configureDatabase() throws AbortException, IOException {
        Database.initialize(new MySQLConfiguration(true), false);
        createTables();
    }
    
}
