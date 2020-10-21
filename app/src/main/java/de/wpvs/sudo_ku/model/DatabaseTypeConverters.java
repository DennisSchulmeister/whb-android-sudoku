package de.wpvs.sudo_ku.model;

import java.util.Date;

import androidx.room.TypeConverter;

/**
 * Utility class automatically used by the Room database to convert serialize and deserialize
 * complex data types during the OR-mapping. See e.g.: https://stackoverflow.com/a/58259703
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
     * Serialize a GameType enum into a string value.
     *
     * @param gameType GameType value to serialize
     * @return Name of the GameType value
     */
    @TypeConverter
    public String gameTypeToString(GameType gameType) {
        return gameType == null ? null : gameType.name();
    }

    /**
     * Deserialize a string back into a GameType enum.
     *
     * @param name Serialized name of the GameType
     * @return Deserialized GameType enum value
     */
    @TypeConverter
    public GameType stringToGameType(String name) {
        return name == null ? null : GameType.valueOf(name);
    }

    /**
     * Serialize a string array into a single string, using the zero character to split values.
     *
     * @param array String array
     * @return Serialized string
     */
    @TypeConverter
    public static String stringArrayToString(String[] array) {
        String joined = "";

        for (String value : array) {
            if (joined.isEmpty()) {
                joined = value;
            } else {
                joined += "\0" + value;
            }
        }

        return joined;
    }

    /**
     * Deserialize a string with zero characters back into a string array.
     *
     * @param joined Seralized string
     * @return Deserialized array
     */
    @TypeConverter
    public String[] stringToStringArray(String joined) {
        return joined.split("\0");
    }
}
