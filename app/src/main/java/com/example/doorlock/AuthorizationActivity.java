package com.example.doorlock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.doorlock.sharedPreferences.SharedPreferencesKeys;
import com.example.doorlock.sharedPreferences.SharedPreferencesManager;
import com.example.doorlock.user.User;

public class AuthorizationActivity extends AppCompatActivity {

    private User user;
    private SharedPreferencesManager preferences;
    private ImageButton homeBtn;
    private Button requestBtn;
    private EditText name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        preferences = new SharedPreferencesManager(this);

        user = new User(this);

        TextView statusTxt = findViewById(R.id.status_text);

        homeBtn = findViewById(R.id.home_button);
        requestBtn = findViewById(R.id.request_button);
        name = findViewById(R.id.user_name_editText);

        setStatus(statusTxt);


        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = name.getText().toString().trim();
                Log.e("STR",String.valueOf(input.length()));
                requestBtn.setEnabled(input.length() > 9);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.getAuthorization().authorizationRequest();
            }
        });
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setStatus(TextView statusTxt){
        boolean status =  preferences.getBooleanVal(SharedPreferencesKeys.USER_ACTIVATED.getKey(),Boolean.parseBoolean(SharedPreferencesKeys.USER_ACTIVATED.getDefaultVal()));
        if(status){
            String name = preferences.getStringVal(SharedPreferencesKeys.USER_NAME.getKey(),SharedPreferencesKeys.USER_NAME.getDefaultVal());
            statusTxt.setText("AUTHORIZED AS " + name);
            statusTxt.setTextColor(Color.WHITE);
            requestBtn.setVisibility(View.GONE);
        }
        else{
            statusTxt.setText("STATUS : UNAUTHORIZED");
            statusTxt.setTextColor(Color.rgb(147, 0, 10));
            requestBtn.setVisibility(View.VISIBLE);
        }
    }
}