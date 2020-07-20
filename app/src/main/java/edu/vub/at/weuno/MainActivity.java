package edu.vub.at.weuno;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import edu.vub.at.IAT;
import edu.vub.at.android.util.IATAndroid;
import edu.vub.at.weuno.interfaces.ATWeUno;
import edu.vub.at.weuno.interfaces.JWeUno;

public class MainActivity extends AppCompatActivity implements HandAction, JWeUno {

    // Ambient Talk Objects
    private static IAT iat;
    private static final int _ASSET_INSTALLER_ = 0;
    private static ATWeUno atwu;
    // Handler to communicate UI <-> AT threads.
    private static Handler mHandler;
    public static Handler getmHandler() {
        return mHandler;
    }
    // -------

    // Dialog UI Elements
    // Create the AlertDialog
    AlertDialog dialog;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;
    private View dialogView;

    private TextView playerCountTextView;
    private TextInputEditText playerNicknameEditText;
    private Button connectButton;
    private  Button startGameButton;

    // End Game Dialog
    AlertDialog endGameDialog;
    AlertDialog.Builder endGameBuilder;
    LayoutInflater endGameInflater;
    View endGameDialogView;
    // -------

    // Game UI Elements
    private CardViewAdapter adapter;

    private DrawingView drawingview;
    private Deck cardDeck;
    private TextView txtUno;
    private Button btnUno, btnUnoTop, btnUnoLeft, btnUnoRight;
    private Animation animUnoTop, animUnoBottom, animUnoLeft, animUnoRight;
    private ProgressBar waitingBar;
    // -------

    // Constants
    private static final int _MSG_NEW_PLAYER_ = 101;
    private static final int _MSG_INIT_DECK_ = 102;
    private static final int _MSG_UPD_DECK = 103;
    private static final int  _MSG_PLAY_CARD = 104;
    private static final int _MSG_SWITCH_DIRECTION = 105;
    private static final int _MSG_PLUS_TWO_ACT = 106;
    private static final int _MSG_SKIP_MOVE_ACT = 107;
    private static final int _MSG_PLUS_FOUR_WILD = 108;
    private static final int MSG_UNO_SIGNAL = 109;
    private static final int MSG_LEFT_UNO_SIGNAL = 110;
    private static final int MSG_RIGHT_UNO_SIGNAL = 111;
    private static final int MSG_TOP_UNO_SIGNAL = 112;
    private static final int MSG_ENABLE_ENDGAME_DIALOG = 113;
    private static final int MSG_SEND_SCORE = 114;
    /*
        Update opponents' cards on the board.
     */
    private static final int _MSG_UPD_OP_CARDS = 115;

    private static final int _MSG_NEXT_PLAYER_MOVE = 116;
    // -------

    // Global Game Variables
    int playersCounter;
    boolean isGameStarted; // State which we need to check when we start the application.
    boolean isFirstMove;
    boolean unoSignaled = false;

    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.activity = this;

        // Ambient Talk Initialization
        if (iat == null) {
            Intent i = new Intent(this, WeUnoAssetInstaller.class);
            startActivityForResult(i, _ASSET_INSTALLER_);
        }

        /*
        When player starts the application a dialog window appears on the screen.
        The screen is used to connect to the game and to start the game itself, if the number
        of players at least 2 and max 4.
         */

        // Initialize dialog
        builder = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_waiting_room, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        // Create the AlertDialog
        dialog = builder.create();
        // Display the dialog
        dialog.show();

        // Init dialog elements
        playerCountTextView = dialogView.findViewById(R.id.playerCountTextView);
        playerNicknameEditText = dialogView.findViewById(R.id.playerNicknameEditText);
        connectButton = dialogView.findViewById(R.id.connectButton);
        startGameButton = dialogView.findViewById(R.id.startGameButton);
        startGameButton.setEnabled(false); // Disable start game button by default.

        // Init end game dialog
        AlertDialog endGameDialog = null;
        AlertDialog.Builder endGameBuilder = new AlertDialog.Builder(this);
        LayoutInflater endGameInflater = this.getLayoutInflater();
        View endGameDialogView = endGameInflater.inflate(R.layout.dialog_endgame, null);

        // Init Game State
        // Default connected users counter;
        updateConnectedGamersCounter(0);
        // First load of application sets game to false (not yet started).
        isGameStarted = false;
        isFirstMove = true;

        connectButton.setOnClickListener(v -> {
            if(playerNicknameEditText.getText().toString().isEmpty()) {
                // Display warning that user should have a name;
                Toast.makeText(this, "You have to enter your name", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // Send to message to AT part that a new user has been connected
                getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_NEW_PLAYER_,
                        playerNicknameEditText.getText().toString()));

                // Disable Button, if already connected
                connectButton.setEnabled(false);
            }
        });


        startGameButton.setOnClickListener(v -> {
            // Call init game function here.
            // This function initialize deck and notify other players.
            initGame();
        });

        // Waiting disconnected player view
        waitingBar = findViewById(R.id.waitingProgressBar);
        waitingBar.setVisibility(View.INVISIBLE);

        // Init Deck View
        // set up hand stack
        ArrayList<Card> cards = new ArrayList<>();

        // Init Deck View Elements
        RecyclerView handView = findViewById(R.id.playerhand);
        drawingview = findViewById(R.id.drawingview);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        handView.setLayoutManager(horizontalLayoutManager);
        adapter = new CardViewAdapter(this, cards, this);
        handView.setAdapter(adapter);

        drawingview.setEnabled(false);
        drawingview.setVisibility(View.INVISIBLE);

        // reference the uno button and text view
        txtUno = findViewById(R.id.txtUno);
        btnUno = findViewById(R.id.btnUno);
        btnUnoTop   = findViewById(R.id.btnUnoTop);
        btnUnoLeft  = findViewById(R.id.btnUnoLeft);
        btnUnoRight = findViewById(R.id.btnUnoRight);

        MoveAndPlaceHelper mh = new MoveAndPlaceHelper(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(mh);
        touchHelper.attachToRecyclerView(handView);

        // setup animations
        animUnoTop    = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.uno_top);
        animUnoBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.uno_bottom);
        animUnoLeft   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.uno_left);
        animUnoRight  = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.uno_right);

        // show uno animation on click
        btnUno.setOnClickListener(v -> {
            startUnoAnimation(animUnoBottom);
            //TODO: send uno signal to AmbientTalk world so that it can be distributed to others
            getmHandler().sendMessage(Message.obtain(getmHandler(), MSG_UNO_SIGNAL));
        });

        // TODO: currently clicking on a player stack shows the uno animation, but it should behah changed to check if the player called uno in time
        // TODO: If user has one card and someone caught it, then that user has to draw two cards as a penalty!
        btnUnoTop.setOnClickListener(v -> {
            startUnoAnimation(animUnoTop);
            getmHandler().sendMessage(Message.obtain(getmHandler(), MSG_TOP_UNO_SIGNAL));
        });
        btnUnoLeft.setOnClickListener(v -> {
            startUnoAnimation(animUnoLeft);
            getmHandler().sendMessage(Message.obtain(getmHandler(), MSG_LEFT_UNO_SIGNAL));
        });
        btnUnoRight.setOnClickListener(v -> {
            startUnoAnimation(animUnoRight);
            getmHandler().sendMessage(Message.obtain(getmHandler(), MSG_RIGHT_UNO_SIGNAL));
        });

        setLeftPlayerCardCount(0);
        setTopPlayerCardCount(0);
        setRightPlayerCardCount(0);

    }

    @Override
        public void disableConnectionDialog() {
        runOnUiThread(() -> {
            dialog.hide();
            drawingview.setEnabled(true);
            drawingview.setVisibility(View.VISIBLE);
        });
    }

    // Call this when someone clicked start game.
    private void initGame() {
        cardDeck = new Deck(); // New deck is initialized when game is started.

        // Sync deck between other players
        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_INIT_DECK_,
                cardDeck.getDeckSerialized()));

        // Game is started from this point. (MSG_INIT_DECK does the same for other devices)
        setGameState();

        // Disable dialog window on the device (MSG_INIT_DECK does the same for other devices)
        disableConnectionDialog();

        // Draw one card and then play it
        drawCards(8); // Automatically notifies about the change in draw pile.
        adapter.playCardAndReturn(0);
    }




    @Override
    public void setGameState() {
        isGameStarted = true;
    }

    public void startUnoAnimation(Animation animation){
        runOnUiThread(() -> {
            txtUno.setVisibility(View.VISIBLE);
            txtUno.startAnimation(animation);
            getmHandler().sendMessage(Message.obtain(getmHandler(), MSG_UNO_SIGNAL));
        });
    }

    // Call this whenever the player has to draw cards
    public void drawCards(int n) {
        runOnUiThread(() -> {
            for (int i = 0; i < n; i++) {
                adapter.addCard(cardDeck.drawCard());
            }
            Log.i("Num of cards: ", " " + cardDeck.cards.size());
            drawingview.invalidate();
            getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_UPD_DECK, cardDeck.getDeckSerialized()));
            getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_UPD_OP_CARDS));
       });
    }

    @Override
    public void unoSignaled() {
        runOnUiThread(() -> {
            unoSignaled = true;
            btnUno.setVisibility(View.INVISIBLE);
        });
    }

    // TODO: Call these methods from AmbientTalk indicating that another player has said Uno
    public void topUnoAnimation() {
        startUnoAnimation(animUnoTop);
    }
    public void leftUnoAnimation() {
        startUnoAnimation(animUnoLeft);
    }
    public void rightUnoAnimation() {
        startUnoAnimation(animUnoRight);
    }

    //TODO: call these methods from AmbientTalk to set the number of cards for the other players
    @Override
    public void setTopPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setTopPlayerCount(n);
            drawingview.invalidate();
        });
    }

    @Override
    public void setLeftPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setLeftPlayerCount(n);
            drawingview.invalidate();
        });
    }

    @Override
    public void setRightPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setRightPlayerCount(n);
            drawingview.invalidate();
        });
    }

    public boolean outOfCards() {
        return adapter.getItemCount() == 0;
    }

    @Override
    public void enableEndGameDialog(String winner) {
        runOnUiThread(() -> {
            if(endGameDialog == null) {
                endGameBuilder = new AlertDialog.Builder(this);
                endGameInflater = this.getLayoutInflater();
                endGameDialogView = endGameInflater.inflate(R.layout.dialog_endgame, null);

                endGameBuilder.setView(endGameDialogView);
                endGameBuilder.setCancelable(false);
                endGameDialog = endGameBuilder.create();
                endGameDialog.show();

                TextView winnerName = endGameDialogView.findViewById(R.id.winnerNameTextView);
                winnerName.setText(winner);
            }
        });


    }

    // Enable End Game Dialog on this device and others >> callback to endGameDialogHasBeenEnabled >> send own score >> callback to updateEndGameDialogValues

    public void updateEndGameDialogValues(int addScore) {
        runOnUiThread(() -> {
            TextView winnerScore = endGameDialogView.findViewById(R.id.scoreNumTextView);

            int totalScore = Integer.parseInt(winnerScore.getText().toString());
            totalScore += addScore;
            winnerScore.setText(getString(R.string.winnerScore, totalScore));
        });
    }

    @Override
    public void endGame() {
        getmHandler().sendMessage(Message.obtain(getmHandler(), MSG_ENABLE_ENDGAME_DIALOG));
    }

    @Override
    public void calculateScoreAndSendBack() {
        int score = 0;

        for(Card card: adapter.getmCards()) {
            Card.Action cardAction = card.getAction();
            Card.Color cardColor = card.getColor();

            if(cardColor == Card.Color.wild) {
                score += 50;
            }

            if(cardAction == Card.Action.skip || cardAction == Card.Action.plus2 || cardAction == Card.Action.reverse) {
                score += 20;
            } else {
                switch(cardAction) {
                    case a0: score += 0; break;
                    case a1: score += 1; break;
                    case a2: score += 2; break;
                    case a3: score += 3; break;
                    case a4: score += 4; break;
                    case a5: score += 5; break;
                    case a6: score += 6; break;
                    case a7: score += 7; break;
                    case a8: score += 8; break;
                    case a9: score += 9; break;
                }
            }
        }

        // total score
        getmHandler().sendMessage(Message.obtain(getmHandler(), MSG_SEND_SCORE, score));
    }

    @Override
    public void nextPlayerDisconnected() {
        waitingBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void playerReconnected() {
        // TODO
        if(waitingBar.getVisibility() == View.VISIBLE) {
            getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_NEXT_PLAYER_MOVE));
        }
    }

    @Override
    public boolean isCardPlayed(Card card) {

        Card.Color color = card.getColor();
        Card.Action action = card.getAction();

        Card topCard = drawingview.getTopCard();
        Card.Color topCardColor = topCard.getColor();
        Card.Action topCardAction = topCard.getAction();

        if(!isFirstMove) {
            btnUno.setVisibility(adapter.getItemCount() < 2 ? View.VISIBLE : View.INVISIBLE);
        } else {
            isFirstMove = false;
        }

        if(drawingview.getTopCard() == drawingview.blankCard()) {
            playCard(card);
            getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLAY_CARD, card.getCardSerialized()));
            // Ask the next player to continue the game.
            getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_NEXT_PLAYER_MOVE));

            return true;
        } else {
            if(color == Card.Color.wild) {
                /*
                    In order to implement wild cards I decided not to let player choice the color, but random. I acknowledged that in classic rules player chooses a color.
                    However this implementation could take more time and the course is mainly about distribution, hence I made this decision.
                 */

                // Set random generated color.
                Card randomCard;
                Random rand = new Random();
                int randomNum = rand.nextInt((4 - 1) + 1) + 1;
                // Random card generation could be done better, this implementation left for time saving. In the future can simply replaced by better one.
                switch (randomNum) {
                    case 1: randomCard = new Card(Card.Color.yellow, Card.Action.a0);
                            break;
                    case 2: randomCard = new Card(Card.Color.green, Card.Action.a0);
                            break;
                    case 3: randomCard = new Card(Card.Color.blue, Card.Action.a0);
                        break;
                    case 4: randomCard = new Card(Card.Color.red, Card.Action.a0);
                        break;
                    default: randomCard = new Card(Card.Color.green, Card.Action.a9);
                }

                if(action == Card.Action.plus4) {
                    playCard(randomCard);
                    getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLAY_CARD, randomCard.getCardSerialized()));
                    getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLUS_FOUR_WILD));
                    if(outOfCards()) { endGame(); }
                    return true;
                } else if(action == Card.Action.color) {
                    // Set random generated color
                    // Next player turn.

                    playCard(randomCard);
                    getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLAY_CARD, randomCard.getCardSerialized()));
                    if(outOfCards()) { endGame(); }
                    return true;
                }

            } else {
                if(color != topCardColor) {
                    displayToast("You cannot play this card! Please try another one.");
                    return false;
                } else {
                    if(action == Card.Action.plus2) {
                        playCard(card);
                        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLAY_CARD, card.getCardSerialized()));
                        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLUS_TWO_ACT));
                        if(outOfCards()) { endGame(); }
                        return true;
                    } else if(action == Card.Action.reverse) {
                        playCard(card);
                        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLAY_CARD, card.getCardSerialized()));
                        switchMoveDirection();
                        if(outOfCards()) { endGame(); }
                        return true;
                    } else if(action == Card.Action.skip) {
                        playCard(card);
                        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLAY_CARD, card.getCardSerialized()));
                        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_SKIP_MOVE_ACT));
                        if(outOfCards()) { endGame(); }
                        return true;
                    } else {
                        // If top card has the same color and that is a simple card 1-9, then you just return true and play that card.
                        playCard(card);
                        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_PLAY_CARD, card.getCardSerialized()));
                        // Ask the next player to continue the game.
                        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_NEXT_PLAYER_MOVE));
                        if(outOfCards()) { endGame(); }
                        return true;
                    }
                }
            }
        }

        // If move is not validated -> return false!
        return false;
    }

    // Update top card on this device.
    public void playCard(Card card) {
        // Update local UI
        // TODO:
        runOnUiThread(() -> {
            drawingview.playCard(card);
            drawingview.invalidate();
        });
    }

    // Manage AmbientTalk Startup
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("WeUno", "Return of Asset Installer activity");
        switch (requestCode) {
            case (_ASSET_INSTALLER_):
                if (resultCode == Activity.RESULT_OK) {
                    LooperThread lt = new LooperThread();
                    lt.start();

                    mHandler = lt.mHandler;
                    new StartIATTask().execute((Void)null);
                }
                break;
        }
    }

    @Override
    public JWeUno registerAtApplication(ATWeUno uno) {
        this.atwu = uno;
        return this;
    }

    @Override
    public void updateConnectedGamersCounter(int counter) {
        runOnUiThread(() -> {
            playerCountTextView.setText(getString(R.string.connectedPlayersCounterString, counter));
            this.playersCounter = counter;
        });
    }

    @Override
    public void setDeck(String[][] deck) {
            LinkedList<Card> deck_ = new LinkedList<>();

            for(int i=0; i<deck.length; i++) {
                    deck_.add(new Card(Card.Color.valueOf(deck[i][0]),
                            Card.Action.valueOf(deck[i][1])));
            }

            runOnUiThread(() -> {
                cardDeck = new Deck(deck_);
                drawingview.invalidate();
            });
    }

    @Override
    public void playTurn() throws InterruptedException {
        boolean havePlayableCards = false;
        int cardsCounter = adapter.getItemCount();
        if (cardsCounter == 0 && isFirstMove) {
            drawCards(7);
            // Barrier thread sleep to sync data before move, used in case if this is the first move.
            Thread.sleep(1000);
        }

        //if(!isFirstMove) {
            for (Card card : adapter.getmCards()) {
                if (card.getColor() == Card.Color.wild || drawingview.getTopCard().getColor() == card.getColor()) { havePlayableCards = true; break;}
            }

            if(!havePlayableCards) {
                drawCards(1);
                displayToast("Do not have card to play. Draw one card and skip this turn.");
                getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_NEXT_PLAYER_MOVE));
                return;
            }
        //}

        displayToast("Your Turn!");
    }

    @Override
    public void switchMoveDirection() {
        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_SWITCH_DIRECTION));
    }

    @Override
    public void unoSignalVerification() {
        // If a number of cards is strictly higher than one, then we do nothing.
        if(adapter.getItemCount() > 1) {
            unoSignaled = false;
        }

        if(adapter.getItemCount() == 1 && !unoSignaled) {
            drawCards(2);
        }
    }

    public void displayToast(String toastText) {
        activity.runOnUiThread(() -> Toast.makeText(activity, toastText, Toast.LENGTH_LONG).show());
    }

    @Override
    public void disableConnectButton() {
        runOnUiThread(() -> connectButton.setEnabled(false));
    }

    @Override
    public void enableGameButton() {
        runOnUiThread(() -> startGameButton.setEnabled(true));
    }

    public class StartIATTask extends AsyncTask<Void, String, Void> {

        private ProgressDialog pd;

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pd.setMessage(values[0]);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(MainActivity.this, "weUno", "Starting AmbientTalk");
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pd.dismiss();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                iat = IATAndroid.create(MainActivity.this);

                this.publishProgress("Loading weUno code");
                iat.evalAndPrint("import /.demo.weUno.weUno.makeWeUno()", System.err);
            } catch (Exception e) {
                Log.e("AmbientTalk", "Could not start IAT", e);
            }
            return null;
        }
    }

    // Call the AmbientTalk methods in a separate thread to avoid blocking the UI.
    private class LooperThread extends Thread {

        @SuppressLint("HandlerLeak")
        public Handler mHandler = new Handler() {

            public void handleMessage(Message msg) {
                if (null == atwu) {
                    Log.i("mHandler: ", "ATWU is null :(");
                    return;
                }
                switch (msg.what) {
                    case _MSG_NEW_PLAYER_: {
                        String playerName = (String) msg.obj;
                        atwu.connectPlayer(playerName);
                        break;
                    }
                    case _MSG_INIT_DECK_: {
                        String[][] deck = (String[][]) msg.obj;
                        atwu.initializeGame(deck);
                        break;
                    }

                    case _MSG_UPD_DECK: {
                        String[][] deck = (String[][]) msg.obj;
                        atwu.setDeck(deck);
                        Log.i("UPD DECK", "" + deck.length);
                        break;
                    }

                    case _MSG_PLAY_CARD: {
                        String[] card = (String[]) msg.obj;
                        atwu.playCard(card);
                        break;

                    }

                    case _MSG_UPD_OP_CARDS: {
                        atwu.updateOPcards(adapter.getItemCount());
                        break;
                    }

                    case _MSG_NEXT_PLAYER_MOVE: {
                        atwu.nextPlayer();
                        break;
                    }

                    case _MSG_SWITCH_DIRECTION: {
                        atwu.switchMoveDirection();
                        break;
                    }
                    case _MSG_PLUS_TWO_ACT: {
                        atwu.plusTwoAction();
                        break;
                    }
                    case _MSG_SKIP_MOVE_ACT: {
                        atwu.skipAction();
                        break;
                    }
                    case _MSG_PLUS_FOUR_WILD: {
                        atwu.plusFourWild();
                        break;
                    }
                    case MSG_UNO_SIGNAL: {
                        atwu.unoSignal();
                        break;
                    }
                    case MSG_LEFT_UNO_SIGNAL: {
                        atwu.leftPlayerUnoSignal();
                        break;
                    }
                    case MSG_RIGHT_UNO_SIGNAL: {
                        atwu.rightPlayerUnoSignal();
                        break;
                    }
                    case MSG_TOP_UNO_SIGNAL: {
                        atwu.topPlayerUnoSignal();
                        break;
                    }
                    case MSG_ENABLE_ENDGAME_DIALOG: {
                        String winnerName = (String) msg.obj;
                        atwu.enableDialogWindow();
                    }
                    case MSG_SEND_SCORE: {
                        int score = (int) msg.obj;
                        atwu.updateEndGameScore(score);
                    }
                }
            }
        };

        public void run() {
            Looper.prepare();
            Looper.loop();
        }

    }
}
