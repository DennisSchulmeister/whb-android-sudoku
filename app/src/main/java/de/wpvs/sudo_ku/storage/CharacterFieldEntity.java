package de.wpvs.sudo_ku.storage;

import java.util.ArrayList;
import java.util.List;

import androidx.room.Entity;

/**
 * Data transfer object for a field on the game board. This is part of the data model used to
 * persist and represent the game state.
 */
@Entity(tableName = "CharacterField", primaryKeys = {"gameUid", "xPos", "yPos"})
public class CharacterFieldEntity {
    public int gameUid = -1;
    public int xPos = -1;
    public int yPos = -1;

    public String character = "";
    public List<String> pencil = new ArrayList<>();
    public List<Integer> words = new ArrayList<>();
}
