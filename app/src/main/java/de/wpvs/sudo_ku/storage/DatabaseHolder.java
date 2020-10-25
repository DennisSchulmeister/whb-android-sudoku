package de.wpvs.sudo_ku.storage;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import de.wpvs.sudo_ku.MyApplication;

/**
 * Main database class which acts as an entry point for all database access objects, which can
 * be used for database access.
 */
@Database(
        version = 7,
        entities = {
                CharacterFieldEntity.class,
                GameEntity.class,
                KnownWordEntity.class,
                WordEntity.class,
        })
@TypeConverters({DatabaseTypeConverters.class})
public abstract class DatabaseHolder extends RoomDatabase {
    private static final String DATABASE_NAME = "sudo-ku";
    private static DatabaseHolder singleton;

    /**
     * Create and return singleton instance of the database.
     *
     * @return Singleton instance of the database
     */
    public static DatabaseHolder getInstance() {
        if (singleton == null) {
            singleton = Room.databaseBuilder(MyApplication.getInstance(), DatabaseHolder.class, DATABASE_NAME).fallbackToDestructiveMigration().build();
        }

        return singleton;
    }

    /**
     * @return Data access object for saved games
     */
    public abstract GameDao gameDao();

    /**
     * @return Data access object for the list of known words
     */
    public abstract KnownWordDao knownWordDao();
}
