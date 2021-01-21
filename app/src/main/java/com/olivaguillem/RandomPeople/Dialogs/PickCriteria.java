package com.olivaguillem.RandomPeople.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.olivaguillem.RandomPeople.MainActivity;
import com.olivaguillem.RandomPeople.MessageActivity;
import com.olivaguillem.RandomPeople.Model.User;
import com.olivaguillem.RandomPeople.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PickCriteria extends DialogFragment {

    boolean existUser;
    Context dialogContext;
    List<User> usersRandomCriteria = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dialogContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        usersRandomCriteria = MainActivity.usersWithoutChat;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.whichCriteria).setItems(R.array.criterias, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int which) {
                MainActivity.referenceUsers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        existUser = false;
                        Collections.shuffle(usersRandomCriteria);
                        for (User userRandom : usersRandomCriteria) {
                            assert user != null;
                            if (which == 0) {
                                if (userRandom.getHobbies()!=null) {
                                    if (!Collections.disjoint(userRandom.getHobbies(), user.getHobbies())) {
                                        existUser = true;
                                        Intent intent = new Intent(getContext(), MessageActivity.class);
                                        intent.putExtra("userid", userRandom.getId());
                                        startActivity(intent);
                                        break;
                                    }
                                }
                            } else if (which == 1) {
                                if (userRandom.getMovies()!=null) {
                                    if (!Collections.disjoint(userRandom.getMovies(), user.getMovies())) {
                                        existUser = true;
                                        Intent intent = new Intent(getContext(), MessageActivity.class);
                                        intent.putExtra("userid", userRandom.getId());
                                        startActivity(intent);
                                        break;
                                    }
                                }
                            } else if (which == 2) {
                                if (userRandom.getMusic()!=null) {
                                    if (!Collections.disjoint(userRandom.getMusic(), user.getMusic())) {
                                        existUser = true;
                                        Intent intent = new Intent(getContext(), MessageActivity.class);
                                        intent.putExtra("userid", userRandom.getId());
                                        startActivity(intent);
                                        break;
                                    }
                                }
                            }
                        }

                        if (!existUser) {
                            try {
                                Toast.makeText(dialogContext, R.string.noUserCriteria, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {}
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        return builder.create();
    }
}
