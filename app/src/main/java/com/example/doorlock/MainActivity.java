package com.example.doorlock;


import com.google.android.material.timepicker.MaterialTimePicker;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.doorlock.sharedPreferences.SharedPreferencesKeys;
import com.example.doorlock.sharedPreferences.SharedPreferencesManager;
import com.example.doorlock.user.User;
import com.google.android.material.slider.Slider;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    //Settings and params
    private String password = "";
    private float txPower = -60;
    private float periodicity = 10;
    private TransmitRules rule = TransmitRules.ALWAYS;

    //password, txPower, and periodicity are saved in the Shared Preferences
    //shared preferences are used for saving data when app is killed
    private SharedPreferencesManager preferencesManager;

    //Intent for service
    private Intent serviceIntent;

    //permissions
    ActivityResultLauncher<String[]> bluetoothPermissions;
    private boolean isBluetoothGranted = false;

    private TextView apiText;
    private User user;

    //private SettingsDialog settingsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiText = findViewById(R.id.api_text);
        user = new User(this);

        //settingsDialog = new SettingsDialog(this);

        //TRANSMITTING SERVICE
        serviceIntent = new Intent(this, TransmittingForegroundService.class);

        preferencesManager = new SharedPreferencesManager(this);
        getSavedData();

        firstLaunch();

        //TEXT VIEWS
        TextView timestamp = findViewById(R.id.timestamp);

        //BUTTONS
        Button settingsBtn = findViewById(R.id.settings_button);
        Button scheduleBtn = findViewById(R.id.schedule_button);
        Button historyBtn = findViewById(R.id.history_button);
        Button transmitBtn = findViewById(R.id.btn3);


        //Settings button
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
                String str =  isTransmittingServiceRunning() ? "IS RUNNING" : "IS NOT RUNNING";
                Log.e("VALIDATION",str);
            }
        });

        scheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAuthorizationActivity();
            }
        });
        //History button
        historyBtn.setOnClickListener(view -> {
            openTimePicker();
            //apiText.setText("DSA");
        });


        transmitBtn.setOnClickListener(view -> {

            if(!isTransmittingServiceRunning()){
                Log.e("VALIDATION","IS NOT RUNNING");
                Log.e("START","STARTING SERVICE");
                startForegroundService(serviceIntent);
                timestamp.setText("TRANSMITTING");
            }
            else if(isTransmittingServiceRunning()){
                Context context = getApplicationContext();
                context.stopService(serviceIntent);
                Log.e("VALIDATION","IS RUNNING");
                Log.e("START","STOPPING SERVICE");
                timestamp.setText("STOPPED");
            }
        });

        //-------SRC: https://altbeacon.github.io/android-beacon-library/requesting_permission.html
        //SETTING REQUESTED PERMISSIONS AT RUNTIME

        //for SDK HIGHER THAN 29(Android 10)
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("This app needs background location access");
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        PERMISSION_REQUEST_BACKGROUND_LOCATION);
                            }
                        });
                        builder.show();
                    }
                    else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }
                        });
                        builder.show();
                    }

                }
            } else {
                if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }

        bluetoothPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if(result.get(Manifest.permission.BLUETOOTH_ADVERTISE) != null){
                    isBluetoothGranted = result.get(Manifest.permission.BLUETOOTH_ADVERTISE);
                }
            }

        });

        requestPermission();
    }

    private void openAuthorizationActivity() {
        Intent intent = new Intent(MainActivity.this, AuthorizationActivity.class);
        startActivity(intent);
    }

    //settingsButton
    private void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.settings_layout);

        Button alwaysButton  = dialog.findViewById(R.id.always_button);
        Button lockedButton = dialog.findViewById(R.id.locked_button);
        Button unlockedButton = dialog.findViewById(R.id.unlocked_button);
        //Button confirmButton = dialog.findViewById(R.id.confirm_button);
        changeButtonsColor(alwaysButton,lockedButton,unlockedButton);

        //EditText passwordForm = dialog.findViewById(R.id.password_enter);
        Slider periodicitySlider = dialog.findViewById(R.id.periodicity_slider);
        Slider txPowerSlider = dialog.findViewById(R.id.TXPower_slider);
        TextView periodicityVal = dialog.findViewById(R.id.periodicity_val);
        TextView tXPowerVal = dialog.findViewById(R.id.txpower_val);

        //setting values
        //passwordForm.setText(password);
        periodicitySlider.setValue(periodicity);
        txPowerSlider.setValue(txPower);
        periodicityVal.setText(String.format("%.0f", periodicity));
        tXPowerVal.setText(String.format("%.0f", txPower));


        alwaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rule = TransmitRules.ALWAYS;
                preferencesManager.saveStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(), TransmitRules.ALWAYS.toString());
                changeButtonsColor(alwaysButton,lockedButton,unlockedButton);
            }
        });

        lockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rule = TransmitRules.LOCKED;
                preferencesManager.saveStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(),TransmitRules.LOCKED.toString());
                changeButtonsColor(alwaysButton,lockedButton,unlockedButton);
            }
        });
        unlockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rule = TransmitRules.UNLOCKED;
                preferencesManager.saveStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(),TransmitRules.UNLOCKED.toString());
                changeButtonsColor(alwaysButton,lockedButton,unlockedButton);
            }
        });

        //password form

//        confirmButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                password = passwordForm.getText().toString();
//                Log.e("PASSWORD",passwordForm.getText().toString());
//                preferencesManager.saveStringVal(SharedPreferencesKeys.USER_PASSWORD.getKey(), password);
//                passwordForm.onEditorAction(EditorInfo.IME_ACTION_DONE);
//            }
//        });


        //periodicity slider
        periodicitySlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                periodicity = periodicitySlider.getValue();
                periodicityVal.setText(String.format("%.0f", periodicity));
                preferencesManager.saveFloatVal(SharedPreferencesKeys.PERIODICITY.getKey(), periodicity);
            }
        });

        //txPower slider
        txPowerSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                txPower = txPowerSlider.getValue();
                tXPowerVal.setText(String.format("%.0f", txPower));
                preferencesManager.saveFloatVal(SharedPreferencesKeys.TXPOWER.getKey(), txPower);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void changeButtonsColor(Button always, Button locked, Button unlocked) {

        switch (rule){
            case ALWAYS:
                always.setBackgroundColor(Color.rgb(0, 88, 203));
                locked.setBackgroundColor(Color.GRAY);
                unlocked.setBackgroundColor(Color.GRAY);

                break;
            case LOCKED:
                locked.setBackgroundColor(Color.rgb(0, 88, 203));
                always.setBackgroundColor(Color.GRAY);
                unlocked.setBackgroundColor(Color.GRAY);
                break;
            case UNLOCKED:
                unlocked.setBackgroundColor(Color.rgb(0, 88, 203));
                always.setBackgroundColor(Color.GRAY);
                locked.setBackgroundColor(Color.GRAY);
        }
    }

    //requiring bt advertising permission, new added in recent versions of API
    private void requestPermission(){
        isBluetoothGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
        ) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionsRequests = new ArrayList<>();

        if(!isBluetoothGranted){
            permissionsRequests.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }

        bluetoothPermissions.launch(permissionsRequests.toArray(new String[0]));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(null, "fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(null, "background location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    //ENDS HERE

    //TIMESTAMP
    private String getTimestamp() {
        long tsLong = System.currentTimeMillis()/1000;
        return Long.toString(tsLong);
    }

    //METHOD VERIFY IF SERVICE IS RUNNING
    public boolean isTransmittingServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(TransmittingForegroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    private void getSavedData(){
        password = preferencesManager.getStringVal(SharedPreferencesKeys.USER_PASSWORD.getKey(),
                SharedPreferencesKeys.USER_PASSWORD.getDefaultVal());

        periodicity =  preferencesManager.getFloatVal(SharedPreferencesKeys.PERIODICITY.getKey(),
                Float.parseFloat(SharedPreferencesKeys.PERIODICITY.getDefaultVal()));

        txPower = preferencesManager.getFloatVal(SharedPreferencesKeys.TXPOWER.getKey(),
                Float.parseFloat(SharedPreferencesKeys.TXPOWER.getDefaultVal()));

        String str = preferencesManager.getStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(),
                SharedPreferencesKeys.TRANSMIT_RULE.getDefaultVal());

        rule = str.equals("ALWAYS") ? TransmitRules.ALWAYS : str.equals("LOCKED") ? TransmitRules.LOCKED : TransmitRules.UNLOCKED;
    }

    private void firstLaunch(){
       boolean firstRun =  preferencesManager.getBooleanVal(SharedPreferencesKeys.FIRST_RUN.getKey(), Boolean.parseBoolean(SharedPreferencesKeys.FIRST_RUN.getDefaultVal()));
        if(firstRun){
            openAuthorizationActivity();
            preferencesManager.saveBooleanVal(SharedPreferencesKeys.FIRST_RUN.getKey(), false);
        }
    }




    private void openTimePicker(){
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select Start Time")
                .setHour(12)
                .setMinute(10)
                .build();

        picker.show(getSupportFragmentManager(),"Start Time");
    }


}