package com.olivaguillem.RandomPeople;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.ActionMenuView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olivaguillem.RandomPeople.Adapter.UserAdapter;
import com.olivaguillem.RandomPeople.Dialogs.ChooseTheme;
import com.olivaguillem.RandomPeople.Dialogs.DeleteChat;
import com.olivaguillem.RandomPeople.Dialogs.NewChatCriteriaProfileFragment;
import com.olivaguillem.RandomPeople.Dialogs.PickCriteria;
import com.olivaguillem.RandomPeople.Dialogs.WriteCorrectly;
import com.olivaguillem.RandomPeople.Model.Chat;
import com.olivaguillem.RandomPeople.Model.Chatlist;
import com.olivaguillem.RandomPeople.Model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    public static DatabaseReference referenceUsers;
    DatabaseReference referenceChatsList, reference;
    RecyclerView recyclerView;
    public static List<User> mUsers, uChatsList, usersWithoutChat;
    List<User> lUsers;
    FloatingActionButton fab;
    UserAdapter userAdapter;
    ActionMenuView bottomBar;
    List<Chatlist> usersChatsList;
    List<Chat> usersChats;
    List<String> listMoviesGenreSelected, listMusicGenreSelected, listHobbiesSelected;
    static List<String> deletedUsers;
    static EditText search_users;
    ImageButton back,search;
    RelativeLayout relativeLayoutSearch;
    ConstraintLayout constraintLayoutRecycler;
    String birthday, city, country;
    public static boolean deleteChat;
    int NightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean searchingUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
        NightMode = sharedPreferences.getInt("NightModeInt", 1);
        AppCompatDelegate.setDefaultNightMode(NightMode);
        recyclerView = findViewById(R.id.recyclerViewMain);
        fab = findViewById(R.id.fabNewChat);
        bottomBar = findViewById(R.id.menuMain);
        search_users = findViewById(R.id.editTextMainSearch);
        back = findViewById(R.id.buttonMainBack);
        search = findViewById(R.id.iconMainSearch);
        relativeLayoutSearch = findViewById(R.id.relativeLayoutSearch);
        constraintLayoutRecycler = findViewById(R.id.constraintLayoutRecycler);
        mUsers = new ArrayList<>();
        usersChats = new ArrayList<>();
        lUsers = new ArrayList<>();
        usersChatsList = new ArrayList<>();
        listMoviesGenreSelected =  new ArrayList<>();
        listMusicGenreSelected =  new ArrayList<>();
        listHobbiesSelected =  new ArrayList<>();
        deletedUsers =  new ArrayList<>();
        uChatsList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        User user = null;
                        if(!searchingUser) {
                            user = uChatsList.get(position);
                        }else{
                            user = lUsers.get(position);
                        }
                        Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                        intent.putExtra("userid", user.getId());
                        startActivity(intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK));
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        DialogFragment deleteChat = new DeleteChat();
                        deleteChat.setCancelable(false);
                        Bundle args = new Bundle();
                        args.putInt("position",position);
                        deleteChat.setArguments(args);
                        deleteChat.show(getSupportFragmentManager(), "Delete chat");
                    }
                })
        );

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        referenceUsers = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        readUsers();

        relativeLayoutSearch.setVisibility(View.GONE);


        referenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deletedUsers.clear();
                User user = dataSnapshot.getValue(User.class);
                birthday = user.getBirthday();
                city = user.getCity();
                country = user.getCountry();
                try {
                    listMoviesGenreSelected.addAll(user.getMovies());
                    listMusicGenreSelected.addAll(user.getMusic());
                    listHobbiesSelected.addAll(user.getHobbies());
                } catch (Exception e){}
                try {
                    deletedUsers.addAll(user.getDeletedUsers());
                } catch (Exception e) {e.printStackTrace();}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchBar();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(usersChatsList);
                relativeLayoutSearch.setVisibility(View.VISIBLE);
                ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) constraintLayoutRecycler.getLayoutParams();
                newLayoutParams.topMargin = 120;
                constraintLayoutRecycler.setLayoutParams(newLayoutParams);
            }
        });

        referenceChatsList = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        referenceChatsList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersChatsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersChatsList.add(chatlist);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(MainActivity.this, fab);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        return true;
                    }
                });

                popup.show();
            }
        });

        Menu bottomMenu = bottomBar.getMenu();
        getMenuInflater().inflate(R.menu.menu, bottomMenu);

        for (int i = 0; i < bottomMenu.size(); i++){
            bottomMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.menuProfile) {
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        return true;
                    }
                    if (id == R.id.menuLanguage) {
                        showChangeLanguageDialog();
                        return true;
                    }

                    if (id == R.id.menuTheme) {
                        DialogFragment chooseTheme = new ChooseTheme();
                        chooseTheme.show(getSupportFragmentManager(), "Choose your theme");
                        return true;
                    }

                    if (id == R.id.menuPassword) {
                        startActivity(new Intent(MainActivity.this,PasswordActivity.class));
                        return true;
                    }

                    if (id == R.id.menuContact) {
                        startActivity(new Intent(MainActivity.this,ContactActivity.class));
                        return true;
                    }

                    if (id == R.id.menuLogout) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        return true;
                    }
                    return true;
                }
            });
        }

        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(search_users.getText().length()>0) {
                    searchUsers(charSequence.toString().toLowerCase());
                    searchingUser = true;
                }else{
                    searchUsers(charSequence.toString().toLowerCase());
                    searchingUser = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }


    private void searchUsers(String s) {
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search").startAt(s).endAt(s+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lUsers.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    assert user!=null;
                    assert fuser != null;
                    for (Chatlist chatlist : usersChatsList) {
                        if (!user.getId().equals(fuser.getUid()) && user.getId().equals(chatlist.getId())) {
                            lUsers.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(MainActivity.this, lUsers, false);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (search_users.getText().toString().equals("")) {
                    mUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            mUsers.add(user);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void onClickRandomChat(MenuItem item) {
        readUsers();

        if(listHobbiesSelected.size() == 0 || listMoviesGenreSelected.size() == 0 || listMusicGenreSelected.size() == 0 || birthday.equals("") || city.equals("") || country.equals("")){
            DialogFragment newFragment = new NewChatCriteriaProfileFragment();
            newFragment.show(getSupportFragmentManager(), "Edit your profile");
        }else {
            usersWithoutChat = mUsers;
            usersWithoutChat.removeAll(uChatsList);
            for(int i = 0; i < usersWithoutChat.size() ; i++){
                if(deletedUsers.contains(usersWithoutChat.get(i).getId())){
                    usersWithoutChat.remove(i);
                }
            }

            if (usersWithoutChat.isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.noMoreUsers), Toast.LENGTH_SHORT).show();
            } else {
                int listSize = usersWithoutChat.size();
                Random r = new Random();
                int result = r.nextInt(listSize);
                final User randomUser = usersWithoutChat.get(result);
                DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
                mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (randomUser.getId().equals(user.getId())) {
                                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                                intent.putExtra("userid", user.getId());
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public void onClickRandomChatByCriteria(MenuItem item) {
        readUsers();

        if(listHobbiesSelected.size() == 0 || listMoviesGenreSelected.size() == 0 || listMusicGenreSelected.size() == 0 || birthday.equals("") || city.equals("") || country.equals("")){
            DialogFragment newFragment = new NewChatCriteriaProfileFragment();
            newFragment.show(getSupportFragmentManager(), "Edit your profile");
        }else{
            usersWithoutChat = mUsers;
            usersWithoutChat.removeAll(uChatsList);
            for(int i = 0; i < usersWithoutChat.size() ; i++){
                if(deletedUsers.contains(usersWithoutChat.get(i).getId())){
                    usersWithoutChat.remove(i);
                }
            }

            if(usersWithoutChat.isEmpty()){
                Toast.makeText(this, getResources().getString(R.string.noMoreUsers), Toast.LENGTH_SHORT).show();
            }else {
                DialogFragment dialogFragment = new PickCriteria();
                dialogFragment.show(getSupportFragmentManager(),"Select your criteria");
            }
        }


    }

    private void chatList() {
        Collections.sort(usersChatsList);
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uChatsList.clear();
                for (Chatlist chatlist : usersChatsList){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        if (chatlist.getId().equals(user.getId())){
                            uChatsList.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(MainActivity.this, uChatsList, true);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void deleteChat(final int position){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        final DatabaseReference referenceOtherUser = FirebaseDatabase.getInstance().getReference("Chatlist");
        final DatabaseReference referenceChats = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addListenerForSingleValueEvent (new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deletedUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    if (chatlist.getId().equals(uChatsList.get(position).getId())){
                        reference.child(chatlist.getId()).removeValue();
                        referenceOtherUser.child(chatlist.getId()).child(firebaseUser.getUid()).removeValue();
                        referenceChats.addListenerForSingleValueEvent (new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Chat chat = snapshot.getValue(Chat.class);
                                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(chatlist.getId()) ||
                                            chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(chatlist.getId())){
                                        snapshot.getRef().removeValue();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void showChangeLanguageDialog(){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle(R.string.chooseLanguage);
        mBuilder.setItems(R.array.appLanguages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    setLocale("en");
                    loadLocale();
                    recreate();
                }else if(i == 1){
                    setLocale("es");
                    loadLocale();
                    recreate();
                }
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();

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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        NightMode = AppCompatDelegate.getDefaultNightMode();

        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putInt("NightModeInt", NightMode);
        editor.apply();
    }

    public void hideSearchBar(){
        search_users.setText("");
        relativeLayoutSearch.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) constraintLayoutRecycler.getLayoutParams();
        newLayoutParams.topMargin = 0;
        constraintLayoutRecycler.setLayoutParams(newLayoutParams);
        chatList();
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

