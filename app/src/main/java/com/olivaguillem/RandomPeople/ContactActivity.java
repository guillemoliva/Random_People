package com.olivaguillem.RandomPeople;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactActivity extends AppCompatActivity {

    Button buttonSend;
    EditText name, subject, message;
    String nameS, subjectS, messageS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        buttonSend = findViewById(R.id.buttonContactSend);
        name = findViewById(R.id.editTextContactName);
        subject = findViewById(R.id.editTextContactSubject);
        message = findViewById(R.id.editTextContactMessage);

        Toolbar toolbar = findViewById(R.id.contactToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.contactUs);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameS = name.getText().toString();
                subjectS = subject.getText().toString();
                messageS = message.getText().toString();

                if (TextUtils.isEmpty(nameS) || TextUtils.isEmpty(subjectS) || TextUtils.isEmpty(messageS)) {
                    Toast.makeText(ContactActivity.this, R.string.fieldsRequired, Toast.LENGTH_SHORT).show();
                }else {
                    sendEmail();
                }
            }
        });

        findViewById(R.id.activityContact).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                return true;
            }
        });

    }


    private void sendEmail() {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        String messageMail = "Name: "+nameS+"\n\nMessage:\n"+messageS;

        emailIntent.setData(Uri.parse("test@gmail.com"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"guillemoliva92@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjectS);
        emailIntent.putExtra(Intent.EXTRA_TEXT, messageMail);

        try {
            emailIntent.setPackage("com.google.android.gm");
            startActivity(emailIntent);
        } catch (Exception e) {
            try {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(ContactActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void setLocale(String lang){

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();

    }

    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }
}
