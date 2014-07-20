package com.whatever.like.totally;

import com.whatever.like.totally.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.view.View;
import android.speech.*;
import android.widget.TextView;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.Button;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class TotallyLikeWhateverHome extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_totally_like_whatever_home);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ring = RingtoneManager.getRingtone(getApplicationContext(), notification);

        myText = (TextView)findViewById(R.id.text_area);
        myPace = (TextView)findViewById(R.id.text_pace);

        talking_button = (Button) findViewById(R.id.dummy_button);

        flaggedWords = new ArrayList<String>();
        flaggedWords.add("like");
        flaggedWords.add("yeah");
        flaggedWords.add("um");
        flaggedWords.add("umm");
        flaggedWords.add("so");
        flaggedWords.add("obviously");
        flaggedWords.add("ohmygod");

        flaggedPhrases = new ArrayList<String>();
        flaggedPhrases.add("but actually");
        flaggedPhrases.add("oh my god");
        flaggedPhrases.add("sorry but");
        flaggedPhrases.add("you know");
        flaggedPhrases.add("a little bit");
        flaggedPhrases.add("sorry if");
        flaggedPhrases.add("sorry for");
        flaggedPhrases.add("bear with me");
        flaggedPhrases.add("moving right along");
        flaggedPhrases.add("sort of");
        flaggedPhrases.add("i mean");

        receive.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onBeginningOfSpeech() {
                Calendar c = Calendar.getInstance();
                startTime = c.get(Calendar.SECOND);
                endTime = null;

                if (talking_animation != null) {
                    talking_animation.start();
                } else {
                    System.out.print("Error!");
                }
            }
            public void onRmsChanged(float rmsdB) { }

            public void onEndOfSpeech() {
                buttonEnabled = true;
                /*
                LinearLayout lView = (LinearLayout)findViewById(R.id.text_display_area);
                myText.setText("Finished!");
                myText.setTextColor(getResources().getColor(R.color.black));
                myText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                lView.addView(myText);*/
                Calendar c = Calendar.getInstance();
                endTime = c.get(Calendar.SECOND);
                talking_animation.stop();
            }

            public void onEvent(int eventType, Bundle params) {
            }

            public void onBufferReceived(byte[] buffer) {
            }

            public void onPartialResults(Bundle partialResults) {
                onResults(partialResults);
            }

            public void onReadyForSpeech(Bundle bundle) {
                //LinearLayout lView = (LinearLayout)findViewById(R.id.text_display_area);

                myText.setText("Start");
                flaggedCount = 0;

                //lView.addView(myText);

                talking_button.setBackgroundResource(R.drawable.talking);
                animation = talking_button.getBackground();
                if (animation instanceof AnimationDrawable) {
                    talking_animation = (AnimationDrawable) animation;
                }
            }

            public void onResults(Bundle results) {
                //LinearLayout lView = (LinearLayout)findViewById(R.id.text_display_area);

                ArrayList<String> returnedStrings = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (returnedStrings != null && returnedStrings.size() > 0) {
                    new CheckFlaggedWordsTask().execute(returnedStrings.get(0));
                    myText.setText(String.format("You've said a flagged word %d times\n\n%s",
                            flaggedCount, returnedStrings.get(0)));

                    if (endTime != null && startTime != null) {
                        int duration = endTime - startTime;
                        double pace = (double) (returnedStrings.get(0).split(" ")).length / duration;
                        if (pace < 0) {
                            pace = 0.0;
                        }
                        myPace.setText(String.format("Your average pace is %.2f words per second.",
                                pace));
                    }

                    //lView.addView(myText);
                } else {
                    myText.setText("Nothing returned.");

                    //lView.addView(myText);
                }
            }

            public void onError(int error) {
                //LinearLayout lView = (LinearLayout)findViewById(R.id.text_display_area);

                myText.setText("Error!");

                //lView.addView(myText);
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnClickListener(mPressed);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }
    private Integer startTime;
    private Integer endTime;
    private Drawable animation;
    private AnimationDrawable talking_animation;
    private TextView myPace = null;
    private TextView myText = null;
    private Button talking_button = null;
    private Context self = this;
    private Activity me = this;
    private SpeechRecognizer receive = SpeechRecognizer.createSpeechRecognizer(this);
    private int flaggedCount = 0;
    private ArrayList<String> flaggedWords;
    private ArrayList<String> flaggedPhrases;
    private boolean buttonEnabled = true;
    View.OnClickListener mPressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // disable button
            if (!buttonEnabled)
                return;
            buttonEnabled = false;

            // listening stuff
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L);

            receive.startListening(intent);
        }
    };

    //Vibrate
    private Vibrator vib;
    private android.net.Uri notification;
    private Ringtone ring;

    private class CheckFlaggedWordsTask extends AsyncTask<String, Integer, String> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected String doInBackground(String... returnedStrings) {
            String[] words = returnedStrings[0].split(" ");
            int newFlaggedCount = 0;
            for (String word : words) {
                if (flaggedWords.contains(word)) {
                    newFlaggedCount++;
                }
            }
            for (String phrase : flaggedPhrases) {
                int count = (String.format(" %s ", returnedStrings[0])).toLowerCase().replace(" ",
                        "  ").split(String.format(" %s ", phrase.replace(" ", "  "))).length - 1;
                newFlaggedCount += count;
            }
            if (newFlaggedCount > flaggedCount) {
                vib.vibrate(400);
                ring.play();
                publishProgress(getResources().getColor(R.color.red));
                findViewById(R.id.scrolling_area).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.scrolling_area).setBackgroundColor(
                                getResources().getColor(R.color.cyan));
                    }
                }, 250);
                flaggedCount = newFlaggedCount;
            }
            return returnedStrings[0];
        }

        protected void onProgressUpdate(Integer... color) {
            findViewById(R.id.scrolling_area).setBackgroundColor(color[0]);
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(String returnedString) {
            myText.setText(String.format("You've said a flagged word %d times\n\n%s",
                    flaggedCount, returnedString));
        }
    }
}
