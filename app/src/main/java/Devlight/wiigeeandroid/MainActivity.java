package Devlight.wiigeeandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.stanko.tools.SharedPrefsHelper;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;
import org.wiigee.filter.HighPassFilter;
import org.wiigee.filter.LowPassFilter;

import java.io.IOException;
import java.util.ArrayList;

import Devlight.wiigeeandroid.R.id;

public class MainActivity extends Activity {

    static final int _TRAIN_BUTTON = 0x01;
    static final int _SAVE_BUTTON = 0x02;
    static final int _RECOGNIZE_BUTTON = 0x03;

    AndroidWiigee wiigee;
    Logger logger;

    boolean isRecording;
    boolean isRecognizing;
    Button trainAndSaveButton;
    Button recognizeButton;
    Switch switchToTrain;
    Button saveButton;
    Switch switchLoadFromFile;
    Button clearButton;
    int counter;
    int counter2;
    String[] nameOfPatterns;
    ArrayList<String> nameOfPatternsList = new ArrayList<>();
    final String TAG_KEY = "nameKey";
    final String TAG_COUNTER = "counter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadNameOfGesture();
        setContentView(R.layout.activity_main);
        trainAndSaveButton = (Button) findViewById(id.trainButton);
        recognizeButton = (Button) findViewById(id.recognizeButton);
        saveButton = (Button) findViewById(id.save_button);
        saveButton.setEnabled(false);
        switchLoadFromFile = (Switch) findViewById(id.load_from_list_swich);
        clearButton = (Button) findViewById(id.button_clear);
        recognizeButton.setVisibility(View.GONE);
        wiigee = new AndroidWiigee(this);
        logger = new Logger((TextView) findViewById(id.logText), 25);
        isRecording = false;
        isRecognizing = false;
        switchToTrain = (Switch) findViewById(id.train_or_recognize);
        clearButton.setEnabled(false);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wiigee.getDevice().getProcessingUnit().reset();
                logger.clear();
                saveButton.setEnabled(false);
                counter = 0;
                counter2 = 0;
                switchToTrain.setChecked(false);
            }
        });

        switchLoadFromFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && nameOfPatterns != null) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title("Saved Patterns")
                            .items(nameOfPatterns)
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    switchLoadFromFile.setChecked(false);
                                    Toast.makeText(MainActivity.this, "You don't chose anything", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    wiigee.getDevice().getProcessingUnit().reset();
                                    wiigee.getDevice().loadClassifire(text.toString());
                                    switchLoadFromFile.setChecked(false);
                                    switchToTrain.setChecked(true);
                                    saveButton.setEnabled(false);
                                    clearButton.setEnabled(true);
                                    logger.clear();
                                }
                            })
                            .show();
                } else {
                    if (nameOfPatterns == null) {
                        Toast.makeText(MainActivity.this, "save pattern for first", Toast.LENGTH_SHORT).show();
                    } else {


                    }
                    switchLoadFromFile.setChecked(false);
                }
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Save name of gesture pattern:");
                alert.setMessage("Enter name of gesture pattern:");
                final EditText input = new EditText(MainActivity.this);
                alert.setView(input);
                alert.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String srt = input.getEditableText().toString();
                        saveNameOfGesture(srt);
                    }
                });
                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        });


        switchToTrain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    recognizeButton.setVisibility(View.VISIBLE);
                    trainAndSaveButton.setVisibility(View.GONE);
                } else {

                    trainAndSaveButton.setVisibility(View.VISIBLE);
                    recognizeButton.setVisibility(View.GONE);
                }
            }
        });

        trainAndSaveButton.setText("click to START train");
        trainAndSaveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {
                        logger.addLog("-------gesture-#------------" + counter++);
                        Button btn = (Button) view;
                        btn.setText("train STARTED");
                        isRecording = false;
                        wiigee.getDevice().fireButtonPressedEvent(_TRAIN_BUTTON);
                        logger.addLog("Record - STARTED");
                        btn.setBackgroundColor(Color.CYAN);
                        break;
                    }
                    case (MotionEvent.ACTION_UP): {
                        Button btn = (Button) view;
                        btn.setText("click to START train");
                        wiigee.getDevice().fireButtonReleasedEvent(_TRAIN_BUTTON);
                        logger.addLog("Record - STOPED");
                        wiigee.getDevice().fireButtonPressedEvent(_SAVE_BUTTON);
                        wiigee.getDevice().fireButtonReleasedEvent(_SAVE_BUTTON);
                        logger.addLog("Gesture - SAVED");
                        btn.setBackgroundColor(Color.TRANSPARENT);
                        saveButton.setEnabled(true);
                        clearButton.setEnabled(true);
                        break;
                    }
                }

                return false;
            }
        });
        recognizeButton.setText("click to RECOGNIZE");
        recognizeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {
                        logger.addLog("==========recognize-#-=======" + counter2++);
                        Button btn = (Button) view;
                        btn.setText("RECOGNIZING");
                        isRecognizing = false;
                        wiigee.getDevice().fireButtonPressedEvent(_RECOGNIZE_BUTTON);
                        logger.addLog("Recognizing - START");
                        btn.setBackgroundColor(Color.GREEN);
                        break;
                    }
                    case (MotionEvent.ACTION_UP): {
                        Button btn = (Button) view;
                        btn.setText("click to RECOGNIZE");
                        wiigee.getDevice().fireButtonReleasedEvent(_RECOGNIZE_BUTTON);
                        btn.setBackgroundColor(Color.TRANSPARENT);
                        break;
                    }
                }
                return false;
            }
        });

        wiigee.setTrainButton(_TRAIN_BUTTON);
        wiigee.setCloseGestureButton(_SAVE_BUTTON);
        wiigee.setRecognitionButton(_RECOGNIZE_BUTTON);
        wiigee.addFilter(new HighPassFilter(0.3));
        wiigee.addFilter(new LowPassFilter(0.03));
        wiigee.addGestureListener(new GestureListener() {
            @Override
            public void gestureReceived(GestureEvent event) {

                logger.addLog("Recognized: " + event.getId() + " Probability: " + event.getProbability());
          event.isValid();

            }
        });
    }

    private void saveNameOfGesture(String name) {
        wiigee.getDevice().getProcessingUnit().saveClassifier(name);

        if (nameOfPatterns != null) {
            nameOfPatternsList.clear();
            for (String namessss : nameOfPatterns) {
                nameOfPatternsList.add(namessss);
            }
            nameOfPatternsList.add(name);
            nameOfPatterns = new String[nameOfPatternsList.size()];
            int counterz =0;
            for(String namessssss:nameOfPatternsList ){
                nameOfPatterns[counterz]= namessssss;
                counterz++;
            }
            nameOfPatterns[nameOfPatterns.length-1]= name;
        } else {
            nameOfPatterns = new String[1];
            nameOfPatternsList.add(name);
            nameOfPatterns[nameOfPatterns.length-1]= name;
        }

        int counterNames = 0;
        for (String nameOne : nameOfPatternsList) {
            SharedPrefsHelper.save(TAG_KEY + counterNames, nameOne);
            counterNames++;
        }

        SharedPrefsHelper.save(TAG_COUNTER, counterNames);
    }

    private void loadNameOfGesture() {

        if (SharedPrefsHelper.getInt(TAG_COUNTER, 897) != 897) {
            int count = SharedPrefsHelper.getInt(TAG_COUNTER);
            nameOfPatterns = new String[count];
            for (int iterator = 0; iterator < count; iterator++) {
                nameOfPatterns[iterator] = SharedPrefsHelper.getString(TAG_KEY + iterator);
            }
        } else {
            Log.d("Йобана в рот", "-----------------------------------");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            wiigee.getDevice().setAccelerationEnabled(true);
        } catch (IOException e) {

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            wiigee.getDevice().setAccelerationEnabled(false);
        } catch (IOException e) {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
