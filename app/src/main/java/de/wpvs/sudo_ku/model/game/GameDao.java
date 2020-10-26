package de.wpvs.sudo_ku.model.game;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

/**
 * Data access object that defines all database queries for saved games.
 */
@Dao
public abstract class GameDao {
    @Transaction
    public void insert(GameState... gameStates) {
        for (GameState gameState : gameStates) {
            gameState.game.uid = this.insertGameEntity(gameState.game);

            for (CharacterFieldEntity characterFieldEntity : gameState.characterFields) {
                characterFieldEntity.gameUid = gameState.game.uid;
                this.insertCharacterField(characterFieldEntity);
            }

            for (WordEntity wordEntity : gameState.words) {
                wordEntity.gameUid = gameState.game.uid;
                this.insertWord(wordEntity);
            }
        }
    }

    @Transaction
    public void update(GameState... gameStates) {
        for (GameState gameState : gameStates) {
            this.delete(gameState.game.uid);
            this.insert(gameState);
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract long insertGameEntity(GameEntity gameEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insertCharacterField(CharacterFieldEntity characterFieldEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insertWord(WordEntity wordEntity);

    @Query("DELETE FROM Game WHERE uid = :uid")
    public abstract void delete(long uid);

    @Transaction
    @Query("SELECT * FROM Game WHERE uid = :uid")
    public abstract LiveData<GameState> selectSingleGameState(long uid);

    @Transaction
    @Query("SELECT * FROM Game WHERE uid = :uid")
    public abstract GameState selectSingleGameStateSynchronously(long uid);

    @Query("SELECT * FROM Game ORDER BY saveDate DESC")
    public abstract LiveData<List<GameEntity>> selectAllGameEntities();

    @Query("SELECT * FROM Game ORDER BY saveDate DESC")
    public abstract List<GameEntity> selectAllGameEntitiesSynchronously();

    @Query("SELECT uid FROM Game")
    public abstract LiveData<List<Long>> selectAllGameIds();

    @Query("SELECT uid FROM Game")
    public abstract List<Long> selectAllGameIdsSynchronously();

    @Query("SELECT COUNT(*) FROM Game")
    public abstract LiveData<Integer> selectGameCount();

    @Query("SELECT COUNT(*) FROM Game")
    public abstract Integer selectGameCountSynchronously();
}
