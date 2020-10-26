package de.wpvs.sudo_ku.activity.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.wpvs.sudo_ku.R;

/**
 *
 */
public class GameControlsFragment extends Fragment {
    /**
     * Callback to inflate the view hierarchy. To prevent crashes new views can be created here,
     * but not yet searched with findViewById(). Initialisation of the views after creation must
     * thus happen in onViewCreated().
     *
     * @param inflater UI inflater
     * @param container Parent container
     * @param savedInstanceState Saved instance state
     * @return New root view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.game_controls_fragment, container, false);
    }

    /**
     * Callback for further initialisation of the views, once they are completely created.
     *
     * @param view Root view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
