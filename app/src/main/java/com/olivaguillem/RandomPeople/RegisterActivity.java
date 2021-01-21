package com.olivaguillem.RandomPeople;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText username, password, password2, email, phone;
    Button createAccount;
    ImageButton visibilityOn, visibilityOff, visibilityOn2, visibilityOff2;

    FirebaseAuth auth;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.editTextRegisterUsername);
        password = findViewById(R.id.editTextRegisterPassword);
        password2 = findViewById(R.id.editTextRegisterPassword2);
        email = findViewById(R.id.editTextRegisterEmail);
        phone = findViewById(R.id.editTextRegisterPhone);
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        createAccount = findViewById(R.id.buttonRegisterCreateAccount);
        visibilityOff = findViewById(R.id.imageButtonRegisterVisibleOff);
        visibilityOff2 = findViewById(R.id.imageButtonRegisterVisibleOff2);
        visibilityOn = findViewById(R.id.imageButtonRegisterVisible);
        visibilityOn2 = findViewById(R.id.imageButtonRegisterVisible2);

        auth = FirebaseAuth.getInstance();
        visibilityOn.setVisibility(View.INVISIBLE);
        visibilityOn2.setVisibility(View.INVISIBLE);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameTxt = username.getText().toString();
                String pass1Txt = password.getText().toString();
                String pass2Txt = password2.getText().toString();
                String emailTxt = email.getText().toString();
                String phoneTxt = phone.getText().toString();
                
                if(TextUtils.isEmpty(usernameTxt) || TextUtils.isEmpty(pass1Txt) || TextUtils.isEmpty(pass2Txt) || TextUtils.isEmpty(emailTxt) ||TextUtils.isEmpty(phoneTxt)){
                    Toast.makeText(RegisterActivity.this, R.string.fieldsRequired, Toast.LENGTH_SHORT).show();
                }else if(!isEmailValid(emailTxt)){
                    Toast.makeText(RegisterActivity.this, R.string.incorrectEmail, Toast.LENGTH_SHORT).show();
                } else if (pass1Txt.length()<6){
                    Toast.makeText(RegisterActivity.this, R.string.password6, Toast.LENGTH_SHORT).show();
                }else if (password==password2) {
                    Toast.makeText(RegisterActivity.this, R.string.samePassword, Toast.LENGTH_SHORT).show();
                }else{
                    registration(usernameTxt, pass1Txt, emailTxt, phoneTxt);
                }
            }
        });

        findViewById(R.id.activityRegisterLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                return true;
            }
        });

        visibilityOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setTransformationMethod(null);
                visibilityOn.setVisibility(View.VISIBLE);
                visibilityOff.setVisibility(View.INVISIBLE);
            }
        });

        visibilityOff2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password2.setTransformationMethod(null);
                visibilityOn2.setVisibility(View.VISIBLE);
                visibilityOff2.setVisibility(View.INVISIBLE);
            }
        });

        visibilityOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setTransformationMethod(new PasswordTransformationMethod());
                visibilityOff.setVisibility(View.VISIBLE);
                visibilityOn.setVisibility(View.INVISIBLE);
            }
        });

        visibilityOn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password2.setTransformationMethod(new PasswordTransformationMethod());
                visibilityOff2.setVisibility(View.VISIBLE);
                visibilityOn2.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void registration(final String username, final String password, final String email, final String phone){

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userId = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userId);
                    hashMap.put("username", username);
                    hashMap.put("imageURL", "default");
                    hashMap.put("password", password);
                    hashMap.put("email", email);
                    hashMap.put("phone", phone);
                    hashMap.put("search", username.toLowerCase());
                    hashMap.put("birthday", "");
                    hashMap.put("city", "");
                    hashMap.put("country", "");

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isComplete()){
                                Toast.makeText(RegisterActivity.this, R.string.registerSuccesfully, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this, R.string.existEmail, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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
