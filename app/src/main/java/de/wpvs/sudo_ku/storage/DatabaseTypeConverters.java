package de.wpvs.sudo_ku.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    public String gameTypeToString(GameEntity.GameType gameType) {
        return gameType == null ? null : gameType.name();
    }

    /**
     * Deserialize a string back into a GameType enum.
     *
     * @param name Serialized name of the GameType
     * @return Deserialized GameType enum value
     */
    @TypeConverter
    public GameEntity.GameType stringToGameType(String name) {
        return name == null ? null : GameEntity.GameType.valueOf(name);
    }

    /**
     * Serialize a string list into a single string, using the zero character to split values.
     *
     * @param list String list
     * @return Serialized string
     */
    @TypeConverter
    public static String stringListToString(List<String> list) {
        String joined = "";

        for (String value : list) {
            if (joined.isEmpty()) {
                joined = value;
            } else {
                joined += "\0" + value;
            }
        }

        return joined;
    }

    /**
     * Deserialize a string with zero characters back into a string list.
     *
     * @param joined Serialized string
     * @return Deserialized list
     */
    @TypeConverter
    public List<String> stringToStringList(String joined) {
        return new ArrayList<String>(Arrays.asList(joined.split("\0")));
    }




    /**
     * Serialize an integer list into a string, using the zero character to split values.
     *
     * @param list Integer list
     * @return Serialized string
     */
    @TypeConverter
    public static String integerListToString(List<Integer> list) {
        String joined = "";

        for (int value : list) {
            if (joined.isEmpty()) {
                joined += value;
            } else {
                joined += "\0" + value;
            }
        }

        return joined;
    }

    /**
     * Deserialize a string with zero characters back into an integer list.
     *
     * @param joined Serialized string
     * @return Deserialized list
     */
    @TypeConverter
    public List<Integer> stringToIntegerList(String joined) {
        List<Integer> list = new ArrayList<>();

        for (String value : joined.split("\0")) {
            list.add(Integer.parseInt(value));
        }

        return list;
    }
}
