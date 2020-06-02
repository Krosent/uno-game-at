package edu.vub.at.weuno;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;

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
    // -------

    // Game UI Elements
    private CardViewAdapter adapter;

    private DrawingView drawingview;
    private Deck cardDeck;
    private TextView txtUno;
    private Button btnUno, btnUnoTop, btnUnoLeft, btnUnoRight;
    private Animation animUnoTop, animUnoBottom, animUnoLeft, animUnoRight;
    // -------

    // Constants
    private static final int _MSG_NEW_PLAYER_ = 1;
    private static final int _MSG_INIT_DECK_ = 2;
    private static final int _MSG_ASK_DRAW_CARDS_ = 3;
    private static final int _MSG_OFF_CONNECTION_DIALOG = 4;
    private static final int _MSG_UPD_DECK = 5;
    private static final int _MSG_NOTIFY_ABOUT_DRAW_ = 6;
    private static final int _MSG_INIT_RELAT_ = 7;
    // -------

    // Global Game Variables
    int playersCounter;
    boolean isGameStarted; // State which we need to check when we start the application.

    // Direction of how game is played.
    // By default is clockwise(forward). If reverse card has been applied - the direction changes to backwards.
    public enum Direction { forward, backwards }
    Direction moveDirection = Direction.forward;

    // flag which demonstrates ability to play a card for the current player.
    public boolean movementEnabled = false;

    // Timer
    CountDownTimer MyCountDownTimer;

    // TODO: State of current game (if disconected)
    // TODO: Players Information

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Init Game State
        // Default connected users counter;
        updateConnectedGamersCounter(0);
        // First load of application sets game to false (not yet started).
        isGameStarted = false;

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
        });

        // TODO: currently clicking on a player stack shows the uno animation, but it should behah changed to check if the player called uno in time
        btnUnoTop.setOnClickListener(v -> { startUnoAnimation(animUnoTop); });
        btnUnoLeft.setOnClickListener(v -> { startUnoAnimation(animUnoLeft); });
        btnUnoRight.setOnClickListener(v -> { startUnoAnimation(animUnoRight); });

    }

    @Override
        public void disableConnectionDialog() {
        runOnUiThread(() -> {
            dialog.hide();
            drawingview.setEnabled(true);
            drawingview.setVisibility(View.VISIBLE);
        });
    }

    // TODO: call this when someone clicked start game.
    private void initGame() {
        cardDeck = new Deck(); //TODO: do this when a new round has started

        // Sync deck between other players
        getmHandler().sendMessage(Message.obtain(getmHandler(), _MSG_INIT_DECK_,
                cardDeck.getDeckSerialized()));

        // Init succ and pred for each player
        initRelationship();

        startTimer();

        // Game is started from this point. (MSG_INIT_DECK does the same for other devices)
        setGameState();

        // Disable dialog window on the device (MSG_INIT_DECK does the same for other devices)
        disableConnectionDialog();


        // TODO: Draw one more card and instantly play it
        // drawingview.playCard(cardDeck.peekTopCard());

        //TODO: draw card >> play it >> notify about all changes >> give turn to the next player
        //
        // Play one card from top
        // TODO: NEED TO FIX IT
       // isCardPlayed(cardDeck.peekTopCard());
        // Draw it
       //drawCards(1); // automoticaly notifies about the change in draw pile.

        setLeftPlayerCardCount(0);
        setTopPlayerCardCount(0);
        setRightPlayerCardCount(0);



        // Draw one card again and play it.

        // Send move to the next user.

        //startGame();
    }

    private void startTimer() {
        MyCountDownTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                //TimeLeftInMillis = millisUntilFinished;
                //updateCountDownText(); //  Updating CountDown_Tv


                /*for incrementing progressbar every second calculating progress for every second*/
               // progress = (int) (START_TIME_IN_MILLIS / (1 * 100));
                //incrementing progress on every tick
               // ProgressBarStatus +=progress;
                //MyProgressBar.setProgress(ProgressBarStatus);

            }

            @Override
            public void onFinish() {
                // Draw cards. After draw other users are notified.
                drawCards(7);
                // Ask others to draw cards

                getmHandler().sendMessageDelayed(Message.obtain(getmHandler(), _MSG_ASK_DRAW_CARDS_, 0, 0, 7), 1000);
                getmHandler().sendMessageDelayed(Message.obtain(getmHandler(), _MSG_ASK_DRAW_CARDS_, 1, 0, 7), 1200);
                getmHandler().sendMessageDelayed(Message.obtain(getmHandler(), _MSG_ASK_DRAW_CARDS_, 2, 0, 7), 1400);
                getmHandler().sendMessageDelayed(Message.obtain(getmHandler(), _MSG_ASK_DRAW_CARDS_, 3, 0, 7), 1600);

            }
        }.start();

       // TimerRunning = true;
       // StartPauseButton.setText("Pause");
       // ResetButton.setVisibility(View.INVISIBLE);


    }

    private void initRelationship() {
        getmHandler().sendMessage(Message.obtain(getmHandler(),_MSG_INIT_RELAT_));
    }

    @Override
    public void setGameState() {
        isGameStarted = true;
    }

    public void startUnoAnimation(Animation animation){
        runOnUiThread(() -> {
            txtUno.setVisibility(View.VISIBLE);
            txtUno.startAnimation(animation);
        });
    }

    //TODO: call this whenever the player has to draw cards
    public void drawCards(int n) {
        runOnUiThread(() -> {
            for (int i = 0; i < n; i++) {
                adapter.addCard(cardDeck.drawCard());
            }
            Log.i("Num of cards: ", " " + cardDeck.cards.size());
            drawingview.invalidate();
            getmHandler().sendMessage(Message.obtain(getmHandler(),_MSG_NOTIFY_ABOUT_DRAW_, n, 0,  cardDeck.getDeckSerialized()));

            // notify other players about draw of cards.
            // TODO: Notify other players about the draw. They have to update corresponding player on the deck.
            // We pass there: number of draw cards, deck
       });


    }

    //TODO: call these methods from AmbientTalk indicating that another player has said Uno
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

    // this method is called when a user plays a card
    // you should check if this is valid, if not you shouldn't update the drawingview and return false
    @Override
    public boolean isCardPlayed(Card card) {

        // TODO: Validate move:
        if(drawingview.getTopCard() == drawingview.blankCard()) {
            cardPlayedUI(card);
        } else {

        }

        // TODO: If move is not validated -> return false!

        // TODO: If Move is validated, evaluate below code:

        //TODO: don't do this if card is not valid, maybe show a toast indicating that the move is invalid and return false
        drawingview.playCard(card);
        drawingview.invalidate();

        btnUno.setVisibility(adapter.getItemCount() < 2 ? View.VISIBLE : View.INVISIBLE);

        return true;
    }

    // Update top card on this device.
    public void cardPlayedUI(Card card) {
        // Update local UI
        drawingview.playCard(card);
        drawingview.invalidate();
    }


    public void makeMove(Card card) {
        if(isCardPlayed(card)) {

            // notify other players
        } else {

        }

        // TODO: HERE!
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
    public void updateDeck(String[][] deck) {
            LinkedList<Card> deck_ = new LinkedList<>();

            for(int i=0; i<deck.length; i++) {
                    deck_.add(new Card(Card.Color.valueOf(deck[i][0]),
                            Card.Action.valueOf(deck[i][1])));
            }

            runOnUiThread(() -> {
                //cardDeck.setDeck(deck_);
                cardDeck = new Deck(deck_);

                // Setup top card
                ////drawingview.playCard(cardDeck.peekTopCard());


                // Deck and Hand Views enable visibility
                //dialog.hide();
                //drawingview.setEnabled(true);
                //drawingview.setVisibility(View.VISIBLE);
                drawingview.invalidate();

                // START GAME
                //startGame();
            });
    }

    @Override
    public void startGame() {
       // isGameStarted = true;

        // Setup top card
        //drawingview.playCard(cardDeck.peekTopCard());

        //drawingview.setLeftPlayerCount(0); //TODO: set initially to 0
        //drawingview.setTopPlayerCount(0);
        //drawingview.setRightPlayerCount(0);

        // drawCards(7);

    }

    // Ask another player to draw n cards. We ask him explicitly but his or her id.
    public void askDrawsCard(int number, int id) {
        // TODO:
    }

    /*
    Local storage functions.
     */

    public SharedPreferences getSharedPref() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref;

    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = getSharedPref().edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPref().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public int getInt(String key) {
        return getSharedPref().getInt(key, -1);
    }

    public String getString(String key) {
        return getSharedPref().getString(key, "empty");
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
                    case _MSG_ASK_DRAW_CARDS_: {
                        int number = (int) msg.obj;
                        int playerID = (int) msg.arg1;
                        atwu.askDrawCards(number, playerID);
                        break;
                    }
                    case _MSG_UPD_DECK: {
                        String[][] deck = (String[][]) msg.obj;
                        atwu.updateDeck(deck);
                        Log.i("UPD DECK", "fe: " + deck.length);
                        break;
                    }

                    case _MSG_NOTIFY_ABOUT_DRAW_: {
                        int numberOfDraw = msg.arg1;
                        String[][] deck = (String[][]) msg.obj;
                        atwu.drawedCards(numberOfDraw, deck);
                        Log.i("Notify About Draw:", "notif: " + deck.length);
                        break;
                    }

                    case _MSG_INIT_RELAT_: {
                        atwu.initializeRelations();
                        break;
                    }

                    /*
                    case _MSG_TOUCH_MOVE_:
                        atws.touchMove((Vector<Float>) msg.obj);
                        break;
                    case _MSG_TOUCH_END_: {
                        float[] endPoint = (float[]) msg.obj;
                        atws.touchEnd(endPoint[0], endPoint[1]);
                        break;
                    }
                    case _MSG_RESET_:
                        atws.reset();
                        break;
                        */
                }
            }
        };

        public void run() {
            Looper.prepare();
            Looper.loop();
        }


    }
}
