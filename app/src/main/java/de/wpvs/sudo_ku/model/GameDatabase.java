package de.wpvs.sudo_ku.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Main database class which acts as an entry point for all database access objects, which can
 * be used for database access.
 */
@Database(entities = {SavedGame.class}, version = 1)
@TypeConverters({DatabaseTypeConverters.class})
public abstract class GameDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "game_database";
    private static GameDatabase singleton;

    /**
     * Create and return singleton instance of the database.
     * @param context The current activity context, from which the database is accessed.
     * @return Singleton instance of the database
     */
    public static GameDatabase getInstance(Context context) {
        if (singleton == null) {
            singleton = Room.databaseBuilder(context, GameDatabase.class, DATABASE_NAME).build();
        }

        return singleton;
    }

    /**
     * @return DAO for saved games
     */
    public abstract  SavedGameDAO savedGameDAO();
}
