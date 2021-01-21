package com.olivaguillem.RandomPeople.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.olivaguillem.RandomPeople.MainActivity;
import com.olivaguillem.RandomPeople.R;

public class DeleteChat extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setTitle(R.string.deleteChatTitle);
        builder1.setCancelable(false);
        builder1.setMessage(R.string.deleteChat);
        builder1.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.deleteChat=true;
                Bundle mArgs = getArguments();
                int position = mArgs.getInt("position");
                MainActivity.deleteChat(position);
            }
        });
        builder1.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.deleteChat=false;
                dialogInterface.cancel();
            }
        });
        return builder1.create();
    }
}