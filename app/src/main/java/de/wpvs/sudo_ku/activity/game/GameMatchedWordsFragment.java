package de.wpvs.sudo_ku.activity.game;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.NavigationUtils;
import de.wpvs.sudo_ku.model.game.CharacterFieldEntity;
import de.wpvs.sudo_ku.model.game.GameLogic;
import de.wpvs.sudo_ku.model.game.GameState;
import de.wpvs.sudo_ku.model.game.WordEntity;

/**
 * View fragment with a list of matched words. Passively watches the game state to display a list
 * all already matched words in the letter game. Also handles some UI interactions like clicking
 * a word to open its description.
 */
public class GameMatchedWordsFragment extends Fragment implements GameStateClient {
    private GameState gameState;
    private GameMessageExchange gameMessageExchange;

    private int xPosSelected = -1;
    private int yPosSelected = -1;

    private TextView textView;

    /**
     * Custom clickable text span that allows opening the corresponding TLDR page for a given
     * word.
     */
    private class TldrClickableSpan extends ClickableSpan {
        private String word;

        /**
         * Constructor
         *
         * @param word Matched word
         */
        public TldrClickableSpan(String word) {
            this.word = word;
        }

        /**
         * Open TLDR page on click
         *
         * @param widget Not used
         */
        @Override
        public void onClick(@NonNull View widget) {
            NavigationUtils.gotoTldrPage(GameMatchedWordsFragment.this.getContext(), this.word);
        }

        /**
         * Don't change styling for the links.
         * @param ds Text paint
         */
        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
        }
    }

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
        return inflater.inflate(R.layout.game_matched_words_fragment, container, false);
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
        this.textView = view.findViewById(R.id.game_matched_words_fragment_textview);
    }

    /**
     * Receive the game state and the message exchange object from the parent activity.
     *
     * @param gameState State of the current game
     * @param gameMessageExchange Object used to send message to the other clients
     */
    @Override
    public void setGameState(GameState gameState, GameMessageExchange gameMessageExchange) {
        this.gameState = gameState;
        this.gameMessageExchange = gameMessageExchange;
    }

    /**
     * Handle messages indicating changes to the game state.
     *
     * @param what Message code (see constants)
     * @param xPos Horizontal field number or -1
     * @param yPos Vertical field number or -1
     */
    @Override
    public void onGameStateMessage(int what, int xPos, int yPos) {
        switch (what) {
            case GameStateClient.MESSAGE_REFRESH_VIEWS:
                this.refreshView();
                break;
            case GameStateClient.MESSAGE_FIELD_SELECTED:
                this.xPosSelected = xPos;
                this.yPosSelected = yPos;
                this.refreshView();
                break;
        }
    }

    /**
     * Rebuild the displayed content after a change in the game statae or selected field has
     * been received.
     */
    private void refreshView() {
        // Get selected character field, if any
        GameLogic gameLogic = this.gameState.getGameLogic();
        CharacterFieldEntity characterField = gameLogic.getCharacterField(this.xPosSelected, this.yPosSelected);

        // Get sorted list of all matched words
        List<WordEntity> words = new LinkedList<WordEntity>(this.gameState.words);
        Collections.sort(words, (o1, o2) -> o1.word.compareTo(o2.word));

        // Rebuild word cloud
        class SpanRange {
            int start = 0;
            int end = 0;
            CharacterStyle style = null;
        }

        StringBuilder wordCloudString = new StringBuilder();
        List<SpanRange> spanRanges = new LinkedList<>();
        int pos = 0;

        for (WordEntity word : words) {
            // Link to the TLDR page
            SpanRange spanRange = new SpanRange();
            spanRange.start = pos;
            spanRange.end = pos + word.word.length();
            spanRange.style = new TldrClickableSpan(word.word);
            spanRanges.add(spanRange);

            if (characterField != null && characterField.words.contains(word.wordNumber)) {
                spanRange = new SpanRange();
                spanRange.start = pos;
                spanRange.end = pos + word.word.length();
                spanRange.style = new StyleSpan(Typeface.BOLD);
                spanRanges.add(spanRange);
            }

            wordCloudString.append(word.word);
            pos = spanRange.end;

            // Four spaces
            spanRange = new SpanRange();
            spanRange.start = pos;
            spanRange.end = pos + 4;
            spanRange.style = new StyleSpan(Typeface.NORMAL);
            spanRanges.add(spanRange);

            wordCloudString.append("    ");
            pos += 4;
        }

        Spannable wordCloudSpannable = Spannable.Factory.getInstance().newSpannable(wordCloudString.toString());

        for (SpanRange spanRange : spanRanges) {
            wordCloudSpannable.setSpan(spanRange.style, spanRange.start, spanRange.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        this.textView.setText(wordCloudSpannable);
        this.textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
