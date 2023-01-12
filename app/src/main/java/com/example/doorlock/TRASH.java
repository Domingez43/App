/*
//////////////////////////////////////////////////////////////////////
////////////TRANSMIT WORKER FOR SCHEDULING OF TRANSMITTING////////////
//////////////////////////////////////////////////////////////////////
package com.example.doorlock;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class TransmitWorker extends Worker {


    public TransmitWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return null;
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    public static void TransmitRequest(){
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(TransmitWorker.class,10, TimeUnit.MINUTES)
                .setInitialDelay(3,TimeUnit.SECONDS)
                .build();
    }
}
*/


/*
//////////////////////////////////////////////////////////////////////
///////////////////////////SETTINGS DIALOG////////////////////////////
///////////////SOMETHING WRONG WITH MATERIAL COMPONENTS///////////////
//////////////////////////////////////////////////////////////////////
package com.example.doorlock;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.slider.Slider;

public class SettingsDialog {

    private final Context context;
    private Dialog dialog;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String password = "";
    private float txPower;
    private float periodicity;
    private TransmitRules transmitRule;

    public SettingsDialog(Context context) {
        this.context = context;
    }

    public void dialogInitialization(){
        dialog = new Dialog(context);

        dialog.setContentView(R.layout.settings_layout);

        Button alwaysButton  = dialog.findViewById(R.id.always_button);
        Button lockedButton = dialog.findViewById(R.id.locked_button);
        Button unlockedButton = dialog.findViewById(R.id.unlocked_button);
        Button confirmButton = dialog.findViewById(R.id.confirm_button);
        changeButtonsColor(alwaysButton,lockedButton,unlockedButton);

        EditText passwordForm = dialog.findViewById(R.id.password_enter);
        Slider periodicitySlider = dialog.findViewById(R.id.periodicity_slider);
        Slider txPowerSlider = dialog.findViewById(R.id.TXPower_slider);
        TextView periodicityVal = dialog.findViewById(R.id.periodicity_val);
        TextView tXPowerVal = dialog.findViewById(R.id.txpower_val);

        //setting values
        passwordForm.setText(password);
        periodicitySlider.setValue(periodicity);
        txPowerSlider.setValue(txPower);
        periodicityVal.setText(String.format("%.0f", periodicity));
        tXPowerVal.setText(String.format("%.0f", txPower));


        alwaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transmitRule = TransmitRules.ALWAYS;
                changeButtonsColor(alwaysButton,lockedButton,unlockedButton);
            }
        });

        lockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transmitRule = TransmitRules.LOCKED;
                changeButtonsColor(alwaysButton,lockedButton,unlockedButton);
            }
        });
        unlockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transmitRule = TransmitRules.UNLOCKED;
                changeButtonsColor(alwaysButton,lockedButton,unlockedButton);
            }
        });

        //password form

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password = passwordForm.getText().toString();
                Log.e("PASSWORD",passwordForm.getText().toString());
                storePassword();
                passwordForm.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });


        //periodicity slider
        periodicitySlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                periodicity = periodicitySlider.getValue();
                periodicityVal.setText(String.format("%.0f", periodicity));
                storePeriodicity();
            }
        });

        //txPower slider
        txPowerSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                txPower = txPowerSlider.getValue();
                tXPowerVal.setText(String.format("%.0f", txPower));
                storeTXPower();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    private void changeButtonsColor(Button always, Button locked, Button unlocked) {
        switch (transmitRule){
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
        storeRule();
    }

    private void getSavedData(){
        preferences = context.getSharedPreferences("settings",context.MODE_PRIVATE);
        password = preferences.getString("password","");
        if(preferences.getFloat("txPower",-404) != -404){
            txPower = preferences.getFloat("txPower",-60);
        }
        if(preferences.getFloat("periodicity",-404) != -404){
            periodicity = preferences.getFloat("periodicity",10);
        }

        if(preferences.getString("rule","").equals("ALWAYS")){
            transmitRule = TransmitRules.ALWAYS;
        }else if(preferences.getString("rule","").equals("LOCKED")){
            transmitRule = TransmitRules.LOCKED;
        }else if(preferences.getString("rule","").equals("UNLOCKED")){
            transmitRule = TransmitRules.UNLOCKED;
        }else{
            transmitRule = TransmitRules.ALWAYS;
        }
    }


    private void storePassword(){
        editor = preferences.edit();
        editor.putString("password",password);
        editor.apply();
    }

    private void storePeriodicity(){
        editor = preferences.edit();
        editor.putFloat("periodicity",periodicity);
        editor.apply();
    }

    private void storeTXPower(){
        editor = preferences.edit();
        editor.putFloat("txPower",txPower);
        editor.apply();
    }

    private void storeRule(){
        editor = preferences.edit();
        editor.putString("rule",transmitRule.toString());
        editor.apply();
    }

}

 */


/*
//////////////////////////////////////////////////////////////////////
///////////////////////////HISTORY FRAGMENT///////////////////////////
//////////////////////////////////////////////////////////////////////

package com.example.doorlock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
}
 */



/*
//////////////////////////////////////////////////////////////////////
/////////////////////////////TIME PICKER//////////////////////////////
//////////////////////////////////////////////////////////////////////

package com.example.doorlock;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePicker extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(),(TimePickerDialog.OnTimeSetListener) getActivity(),hour,minute, DateFormat.is24HourFormat(getActivity()));
    }
}
 */


/*
//////////////////////////////////////////////////////////////////////
///////////////////////////////MQTT V5////////////////////////////////
//////////////////////////////////////////////////////////////////////


    private void buildClientV5(){
        clientV5 = MqttClient.builder()
                //.identifier need to add
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(8883)   //1883
                .sslWithDefaultConfig()
                .automaticReconnectWithDefaultConfig()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(passwordMQTT))
                .applySimpleAuth()
                .buildBlocking(); //WHICH TO USE ASYNC OR BLOCKING
    }


    private void connectClientV5(){
        clientV5.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(passwordMQTT))
                .applySimpleAuth()
                .send();
    }


    private void subscribeClientV5(){
        clientV5.subscribeWith()
                .topicFilter(topic)
                .send();
        // set a callback that is called when a message is received (using the async API style)
        clientV5.toAsync().publishes(ALL, publish -> {

            Message message = new Message();
            message.message = String.valueOf(UTF_8.decode(publish.getPayload().get()));
            database.messageDao().insertMessage(message);

            Log.e("MQTT","Received message: " +
                    publish.getTopic() + " -> " +
                    UTF_8.decode(publish.getPayload().get()));

        });
    }


    public void publishMessageV5(String message){
        // publish a message to the topic "my/test/topic"
        clientV5.publishWith()
                .topic(topic)
                .payload(UTF_8.encode(message))
                .send();
    }


    public void disconnectClientV5(){
        try {
            clientV5.disconnect();
        }catch (Exception e){
            Log.e("MQTT","CLIENT ALREADY DISCONNECTED");
        }
    }
 */