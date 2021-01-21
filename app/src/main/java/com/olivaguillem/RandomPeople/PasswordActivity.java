package com.olivaguillem.RandomPeople;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.olivaguillem.RandomPeople.Model.User;

import java.util.HashMap;
import java.util.Locale;

public class PasswordActivity extends AppCompatActivity {

    TextView forgotPassword;
    EditText currentPassword, password,confirmPassword;
    Button updatePassword;
    String currentPasswordString,currentEmail;
    ImageButton visibilityOn, visibilityOff, visibilityOn2, visibilityOff2, visibilityOn3, visibilityOff3;

    DatabaseReference reference;
    FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        forgotPassword = findViewById(R.id.textViewPasswordForgot);
        currentPassword = findViewById(R.id.editTextPasswordCurrentPassword);
        password = findViewById(R.id.editTextPasswordPassword);
        confirmPassword = findViewById(R.id.editTextRegisterConfirmPassword);
        updatePassword = findViewById(R.id.buttonPasswordUpdate);
        visibilityOff = findViewById(R.id.imageButtonPasswordVisibleOff);
        visibilityOn = findViewById(R.id.imageButtonPasswordVisible);
        visibilityOff2 = findViewById(R.id.imageButtonPasswordVisibleOff2);
        visibilityOn2 = findViewById(R.id.imageButtonPasswordVisible2);
        visibilityOff3 = findViewById(R.id.imageButtonPasswordVisibleOff3);
        visibilityOn3 = findViewById(R.id.imageButtonPasswordVisible3);

        visibilityOn.setVisibility(View.INVISIBLE);
        visibilityOn2.setVisibility(View.INVISIBLE);
        visibilityOn3.setVisibility(View.INVISIBLE);

        Toolbar toolbar = findViewById(R.id.passwordToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.updatePassword);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                currentEmail = user.getEmail();

                updatePassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentPasswordString = currentPassword.getText().toString();
                            if (password.getText().toString().length()<6){
                                Toast.makeText(PasswordActivity.this, R.string.password6, Toast.LENGTH_SHORT).show();
                            }else if(!password.getText().toString().equals(confirmPassword.getText().toString())){
                                Toast.makeText(PasswordActivity.this, R.string.samePassword, Toast.LENGTH_SHORT).show();
                            }else{
                                try {
                                    AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPasswordString);
                                    fuser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            fuser.updatePassword(password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        HashMap<String, Object> hashMap = new HashMap<>();
                                                        hashMap.put("password", password.getText().toString());

                                                        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isComplete()) {
                                                                    Toast.makeText(PasswordActivity.this, R.string.passwordUpdated, Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } catch (Exception e) {
                                    Toast.makeText(PasswordActivity.this, R.string.wrongPassword, Toast.LENGTH_SHORT).show();
                                }
                            }

                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PasswordActivity.this, ResetPasswordActivity.class));
            }
        });



        findViewById(R.id.activityPassword).setOnTouchListener(new View.OnTouchListener() {
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
                currentPassword.setTransformationMethod(null);
                visibilityOn.setVisibility(View.VISIBLE);
                visibilityOff.setVisibility(View.INVISIBLE);
            }
        });

        visibilityOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPassword.setTransformationMethod(new PasswordTransformationMethod());
                visibilityOn.setVisibility(View.INVISIBLE);
                visibilityOff.setVisibility(View.VISIBLE);
            }
        });

        visibilityOff2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setTransformationMethod(null);
                visibilityOn2.setVisibility(View.VISIBLE);
                visibilityOff2.setVisibility(View.INVISIBLE);
            }
        });

        visibilityOn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setTransformationMethod(new PasswordTransformationMethod());
                visibilityOn2.setVisibility(View.INVISIBLE);
                visibilityOff2.setVisibility(View.VISIBLE);
            }
        });

        visibilityOff3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmPassword.setTransformationMethod(null);
                visibilityOn3.setVisibility(View.VISIBLE);
                visibilityOff3.setVisibility(View.INVISIBLE);
            }
        });

        visibilityOn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                visibilityOn3.setVisibility(View.INVISIBLE);
                visibilityOff3.setVisibility(View.VISIBLE);
            }
        });

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
