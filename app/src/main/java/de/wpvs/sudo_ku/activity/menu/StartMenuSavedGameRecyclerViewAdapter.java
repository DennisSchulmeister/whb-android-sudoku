package de.wpvs.sudo_ku.activity.menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.wpvs.sudo_ku.databinding.StartGameListitemBinding;
import de.wpvs.sudo_ku.model.SavedGame;

/**
 * Adapter class to display SavedGame instances in a RecyclerView.
 */
public class StartMenuSavedGameRecyclerViewAdapter extends RecyclerView.Adapter<StartMenuSavedGameRecyclerViewAdapter.ViewHolder> {
    /**
     * ViewHolder class that is used by the RecyclerView to hold a reference to the view elements
     * of one list item. Since we are using view bindings, this is does only contain the binding
     * object for the corresponding list item instead of any view references.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final StartGameListitemBinding binding;

        /**
         * Constructor
         * @param binding View binding object
         */
        public ViewHolder(@NonNull StartGameListitemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * Callback interface for when an item was clicked
     */
    public static interface ClickListener {
        /**
         * Handle click on a saved game.
         * @param savedGame Clicked SavedGame entity.
         */
        void onItemClicked(SavedGame savedGame);
    }

    private List<SavedGame> savedGames = new ArrayList<>();
    private ClickListener clickListener;

    /**
     * Set or change the list of saved games to display. This needs to be called at least once
     * by the owner activity after creation of the adapter object. It can also be called via
     * a LiveData observer to automatically update the visible items once the list has changed.
     *
     * @param savedGames New list of saved games to display.
     */
    public void setSavedGames(List<SavedGame> savedGames) {
        this.savedGames = savedGames;
        this.notifyDataSetChanged();
    }

    /**
     * Set or change the callback object for clicked items.
     * @param clickListener Click listener callback
     */
    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * This method is required so that the RecyclerView knows how many list items it needs to show.
     * @returns the amount of items to display.
     */
    @Override
    public int getItemCount() {
        return this.savedGames.size();
    }

    /**
     * This method is required by the RecyclerView to create the ViewHoler instances and thus
     * the views that will be used to display the individual list items.
     *
     * @param parent ViewGroup to which the new views will belong (unused)
     * @param viewType Type of the new view (unused)
     * @returns a new ViewHolder instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        StartGameListitemBinding binding = StartGameListitemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        binding.getRoot().setOnClickListener(v -> {
            if (this.clickListener != null) {
                SavedGame savedGame = binding.getSavedGame();
                this.clickListener.onItemClicked(savedGame);
            }
        });

        return new ViewHolder(binding);
    }

    /**
     * This method is required by the RecyclerView to bind the given ViewHolder instance (and thus
     * the associated views) to a particular list item on screen.
     *
     * @param holder Any previously created ViewHolder instance
     * @param position List item number to display
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedGame savedGame = this.savedGames.get(position);

        holder.binding.setSavedGame(savedGame);

        switch (savedGame.getGameType()) {
            case NUMBER_QUIZ:
                holder.binding.setGameTypeNumberVisibility(View.VISIBLE);
                holder.binding.setGameTypeLetterVisibility(View.GONE);
                break;
            case LETTER_QUIZ:
                holder.binding.setGameTypeNumberVisibility(View.GONE);
                holder.binding.setGameTypeLetterVisibility(View.VISIBLE);
                break;
        }

        holder.binding.setBoardSize(savedGame.getSize() + "×" + savedGame.getSize());
        holder.binding.setElapsedTime((int) Math.floor(savedGame.getSeconds() / 60) + ":" + (savedGame.getSeconds() % 60));
        holder.binding.setProgress((int) (savedGame.getProgress() * 100) + "%");

        holder.binding.executePendingBindings();
    }
}
