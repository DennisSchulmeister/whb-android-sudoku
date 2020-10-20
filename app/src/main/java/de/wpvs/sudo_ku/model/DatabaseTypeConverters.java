package de.wpvs.sudo_ku.model;

import java.util.Date;

import androidx.room.TypeConverter;

/**
 * Utility class automatically used by the Room database to convert serialize and deserialize
 * complex datatypes during the OR-mapping. See e.g.: https://stackoverflow.com/a/58259703
 */
public class DatabaseTypeConverters {
    /**
     * Serialize a Date to an atomic long value.
     *
     * @param date Date-object used in an Entity
     * @returns Serialized long timestamp
     */
    @TypeConverter
    public static Long dateToLongTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * Deserialize an atomic long value back to a Date object.
     *
     * @param value Serialized long timestamp
     * @return Deserialized Date obejct
     */
    @TypeConverter
    public static Date longTimestampToDate(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Serialize a GameType value into a string.
     *
     * @param gameType GameType enum value
     * @return Serialized string value
     */
    @TypeConverter
    public static String gameTypeToString(GameType gameType) {
        return gameType == null ? null : gameType.name();
    }

    /**
     * Deserialize a string value into a GameType enum.
     *
     * @param value Serialized string value
     * @return Deserialized GameType enum
     */
    @TypeConverter
    public static GameType stringToGameType(String value) {
        return value == null ? null : GameType.valueOf(value);
    }
}
