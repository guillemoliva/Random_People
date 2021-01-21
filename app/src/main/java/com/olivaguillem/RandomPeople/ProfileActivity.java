package com.olivaguillem.RandomPeople;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.olivaguillem.RandomPeople.Dialogs.DatePickerFragment;
import com.olivaguillem.RandomPeople.Model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity implements MultiSelectSpinner.OnMultipleItemsSelectedListener{

    EditText birthDate;
    Spinner spinnerLanguage,spinnerGender;
    MultiSelectSpinner multiSpinnerMovies, multiSpinnerMusic, multiSpinnerHobbies;
    List<String> listMoviesGenre, listMoviesGenreSelected, listMusicGenre, listMusicGenreSelected, listHobbies,listHobbiesSelected, listLanguage, listGenders;
    ImageView profileImage;
    EditText username, email, phone, city, country;
    Button saveChanges;
    String birthday, emailString, passwordString, languageSelected;
    int languageCode, genderSelected;
    DatabaseReference reference;
    FirebaseUser fuser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST= 1;
    private Uri imageUri;
    private StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        spinnerLanguage = findViewById(R.id.spinnerProfileLanguages);
        spinnerGender = findViewById(R.id.spinnerGenders);
        profileImage = findViewById(R.id.imageViewProfileUserIcon);
        username = findViewById(R.id.editTextProfileUsername);
        email = findViewById(R.id.editTextProfileEmail);
        phone = findViewById(R.id.editTextProfilePhoneNumber);
        saveChanges = findViewById(R.id.buttonProfileSaveChanges);
        city = findViewById(R.id.editTextProfileCity);
        country = findViewById(R.id.editTextProfileCountry);
        listMoviesGenreSelected =  new ArrayList<>();
        listMusicGenreSelected =  new ArrayList<>();
        listHobbiesSelected =  new ArrayList<>();
        Toolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        loadCalendar();

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                try {
                    languageSelected = user.getLanguage();
                } catch (Exception e) {}
                try {
                    genderSelected = user.getGender();
                } catch (Exception e) {}

                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.drawable.default_user);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }
                email.setText(user.getEmail());
                phone.setText(user.getPhone());
                city.setText(user.getCity());
                country.setText(user.getCountry());
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
                languageCode = getIndex(spinnerLanguage,languageSelected);
                spinnerLanguage.setSelection(languageCode);
                spinnerGender.setSelection(genderSelected);
                birthDate.setText(user.getBirthday());
                emailString = user.getEmail();
                passwordString = user.getPassword();
                loadSpinners();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AuthCredential credential = EmailAuthProvider.getCredential(emailString, passwordString);
                fuser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, getResources().getString(R.string.existEmail), Toast.LENGTH_SHORT).show();
                                }else{

                                    if(!isEmailValid(email.getText().toString())){
                                        Toast.makeText(ProfileActivity.this, getResources().getString(R.string.incorrectEmail), Toast.LENGTH_SHORT).show();
                                    }else {

                                        String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
                                        int selectedGender = spinnerGender.getSelectedItemPosition();
                                        languageCode = getIndex(spinnerLanguage,selectedLanguage);
                                        HashMap<String, Object> hashMap = new HashMap<>();

                                        hashMap.put("username", username.getText().toString());
                                        hashMap.put("phone", phone.getText().toString());
                                        hashMap.put("email", email.getText().toString());
                                        hashMap.put("city", city.getText().toString());
                                        hashMap.put("country", country.getText().toString());
                                        hashMap.put("language", selectedLanguage);
                                        hashMap.put("languageCode", languageCode);
                                        hashMap.put("gender", selectedGender);
                                        if(birthday != null) {
                                            hashMap.put("birthday", birthday);
                                        }
                                        hashMap.put("movies", listMoviesGenreSelected);
                                        hashMap.put("music", listMusicGenreSelected);
                                        hashMap.put("hobbies", listHobbiesSelected);

                                        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isComplete()) {
                                                    Toast.makeText(ProfileActivity.this, R.string.changesSaved, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });

        findViewById(R.id.profileLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                return true;
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });


    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getApplicationContext());
        pd.setMessage(getResources().getString(R.string.uploading));
        if(isFinishing()) {
            pd.show();
        }

        if (imageUri != null){
            final  StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", "" + mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(this, getResources().getString(R.string.noImageSelected), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, getResources().getString(R.string.uploadInProgress), Toast.LENGTH_SHORT).show();
            }else{
                uploadImage();
            }
        }
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                final String selectedDate = day + " / " + (month+1) + " / " + year;
                birthday = selectedDate;
                birthDate.setText(selectedDate);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    @Override
    public void selectedIndices(HashMap<Integer, String> indices) {
    }

    @Override
    public void selectedStrings(List<String> strings) {

        if(listMoviesGenre.containsAll(strings)){
            listMoviesGenreSelected.clear();
            listMoviesGenreSelected.addAll(strings);
        }else if(listMusicGenre.containsAll(strings)){
            listMusicGenreSelected.clear();
            listMusicGenreSelected.addAll(strings);
        }else if(listHobbies.containsAll(strings)){
            listHobbiesSelected.clear();
            listHobbiesSelected.addAll(strings);
        }
    }

    public void loadSpinners(){

        listLanguage = Arrays.asList(getResources().getStringArray(R.array.languages));
        listGenders = Arrays.asList(getResources().getStringArray(R.array.gender));

        listMoviesGenre = Arrays.asList(getResources().getStringArray(R.array.movieGenres));
        Collections.sort(listMoviesGenre.subList(1,listMoviesGenre.size()));
        listMusicGenre = Arrays.asList(getResources().getStringArray(R.array.musicGenres));
        Collections.sort(listMusicGenre.subList(1,listMusicGenre.size()));
        listHobbies = Arrays.asList(getResources().getStringArray(R.array.hobbies));
        Collections.sort(listHobbies.subList(1,listHobbies.size()));

        multiSpinnerMovies = findViewById(R.id.multiSpinnerMoviesGenre);
        multiSpinnerMovies.setItems(listMoviesGenre);
        multiSpinnerMovies.hasNoneOption(true);
        multiSpinnerMovies.setSelection(listMoviesGenreSelected);
        multiSpinnerMovies.setListener(this);

        multiSpinnerMusic = findViewById(R.id.multiSpinnerMusicGenre);
        multiSpinnerMusic.setItems(listMusicGenre);
        multiSpinnerMusic.hasNoneOption(true);
        multiSpinnerMusic.setSelection(listMusicGenreSelected);
        multiSpinnerMusic.setListener(this);

        multiSpinnerHobbies = findViewById(R.id.multiSpinnerHobbies);
        multiSpinnerHobbies.setItems(listHobbies);
        multiSpinnerHobbies.hasNoneOption(true);
        multiSpinnerHobbies.setSelection(listHobbiesSelected);
        multiSpinnerHobbies.setListener(this);
    }

    public void loadCalendar(){
        birthDate = findViewById(R.id.editTextProfileBirthDate);
        birthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragment = new DatePickerFragment();
                showDatePickerDialog();
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
