package com.olivaguillem.RandomPeople;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.olivaguillem.RandomPeople.Model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Profile2Activity extends AppCompatActivity{

    EditText birthday,language, gender, phone, username, city, country;
    Spinner spinnerMovies, spinnerMusic, spinnerHobbies;
    List<String> listMoviesGenreSelected, listMusicGenreSelected, listHobbiesSelected;
    ImageView profileImage;
    String emailString, passwordString, languageSelected, userid, usernameString;
    int genderSelected;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);

        language = findViewById(R.id.editTextProfile2Languages);
        language.setFocusable(false);
        gender = findViewById(R.id.editTextProfile2Genders);
        gender.setFocusable(false);
        profileImage = findViewById(R.id.imageViewProfile2UserIcon);
        birthday = findViewById(R.id.editTextProfile2BirthDate);
        birthday.setFocusable(false);
        username = findViewById(R.id.editTextProfile2Username);
        username.setFocusable(false);
        phone = findViewById(R.id.editTextProfile2PhoneNumber);
        phone.setFocusable(false);
        city = findViewById(R.id.editTextProfile2City);
        city.setFocusable(false);
        country = findViewById(R.id.editTextProfile2Country);
        country.setFocusable(false);
        spinnerHobbies = findViewById(R.id.spinnerHobbies2);
        spinnerMovies = findViewById(R.id.spinnerMoviesGenre2);
        spinnerMusic = findViewById(R.id.spinnerMusicGenre2);
        listMoviesGenreSelected =  new ArrayList<>();
        listMusicGenreSelected =  new ArrayList<>();
        listHobbiesSelected =  new ArrayList<>();

        final Intent intent = getIntent();
        userid = intent.getStringExtra("id");

        Toolbar toolbar = findViewById(R.id.profile2Toolbar);
        setSupportActionBar(toolbar);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                languageSelected = user.getLanguage();
                genderSelected = user.getGender();
                String[] genderArray = getResources().getStringArray(R.array.gender);
                usernameString = user.getUsername();

                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.drawable.default_user);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }
                phone.setText(user.getPhone());
                city.setText(user.getCity());
                country.setText(user.getCountry());
                birthday.setText(user.getBirthday());
                try {
                    language.setText(user.getLanguage());
                    gender.setText(genderArray[genderSelected]);
                } catch (Exception e){}
                    listMusicGenreSelected.clear();
                    listHobbiesSelected.clear();
                    listMoviesGenreSelected.clear();

                try {
                    listMusicGenreSelected.addAll(user.getMusic());
                } catch (Exception e){}
                try {
                    listMoviesGenreSelected.addAll(user.getMovies());
                } catch (Exception e){}
                try {
                    listHobbiesSelected.addAll(user.getHobbies());
                } catch (Exception e){}

                loadSpinners();
                emailString = user.getEmail();
                passwordString = user.getPassword();

                getSupportActionBar().setTitle(usernameString);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    public void loadSpinners(){


        ArrayAdapter<String> adapterMovies = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listMoviesGenreSelected);
        adapterMovies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMovies.setAdapter(adapterMovies);

        ArrayAdapter<String> adapterMusic = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listMusicGenreSelected);
        adapterMusic.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMusic.setAdapter(adapterMusic);

        ArrayAdapter<String> adapterHobbies = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listHobbiesSelected);
        adapterHobbies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHobbies.setAdapter(adapterHobbies);

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