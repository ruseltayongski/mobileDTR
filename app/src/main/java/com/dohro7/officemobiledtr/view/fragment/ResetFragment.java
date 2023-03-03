package com.dohro7.officemobiledtr.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.model.LeaveModel;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.sharedpreference.UserSharedPreference;
import com.dohro7.officemobiledtr.viewmodel.ResetViewModel;

public class ResetFragment extends Fragment
{
    private ResetViewModel resetViewModel;
    private Button resetBtn;
    private TextView usernameTV;
    private  ProgressBar progressbar;
    private String currentUser="";
    private String reset_userid="";
    private EditText userID_et;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.reset_fragment_layout, container, false);
        resetViewModel = ViewModelProviders.of(this).get(ResetViewModel.class);
        resetBtn= view.findViewById(R.id.reset_btn);
        usernameTV= view.findViewById(R.id.reset_tv1);
        progressbar=view.findViewById(R.id.reset_progressbar);
        progressbar.bringToFront();
        userID_et= view.findViewById(R.id.reset_id);

        resetViewModel.getCurrentUser().observe(this, new Observer<UserModel>() {
            @Override
            public void onChanged(UserModel userModel)
            { if(userModel!=null) {currentUser=userModel.id;} }});

        resetViewModel.getCheckedUsername().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.equalsIgnoreCase("1"))
                {
                    progressbar.setVisibility(View.GONE);
                    usernameTV.setText("ID Number doesn't exists");
                    usernameTV.setVisibility(View.VISIBLE);
                    resetBtn.setClickable(true);
                }
                else {
                    displayConfirmationDialog( currentUser, reset_userid,  s);
                    progressbar.setVisibility(View.GONE);
                    Log.e("reset", "after displayConfirmationDialog is called, @observer");
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("log","reset button is clicked");
                reset(currentUser); }});


        resetViewModel.getUsername().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressbar.setVisibility(View.GONE);
                if(s.equalsIgnoreCase("1")) { usernameTV.setText("ID Number doesn't exists");}
                else if(s.equalsIgnoreCase("2")) { usernameTV.setText("You have no authority to reset password! Please contact the IT Administration");}
                else { usernameTV.setText("Password was reset to 123 for user : "+s + " ("+reset_userid+")" ); }
                usernameTV.setVisibility(View.VISIBLE);
                resetBtn.setClickable(true);
            }});

        resetViewModel.getResetErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressbar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
                resetBtn.setClickable(true);
            }
        });
        return view;
    }

    private void reset(String currentUser)
    {
        reset_userid = userID_et.getText().toString();
        if(reset_userid.isEmpty())
        {
            userID_et.setError("Required");
            userID_et.requestFocus();
        }
        if(!reset_userid.isEmpty())
        {
            progressbar.setVisibility(View.VISIBLE);
            Log.e("log", "call resetViewModel.checkUsername");
            resetBtn.setClickable(false);
            //Log.e("reset", "current "+currentUser + "reset_userid " + reset_userid);
            resetViewModel.checkUsername(reset_userid);
        }
    }


    private void displayConfirmationDialog(String currentUser1,String reset_userid1, String name)
    {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_delete_warning);
        dialog.setCancelable(false);

        final  String currentUser=currentUser1;
        final String reset_userid=reset_userid1;

        final TextView msg1 = dialog.findViewById(R.id.dialog_delete_message1);
        final TextView itemDescription = dialog.findViewById(R.id.dialog_delete_message2);
        dialog.findViewById(R.id.dialog_delete_message3).setVisibility(View.GONE);

        final TextView confirmBtn= dialog.findViewById(R.id.dialog_delete_delete);
        confirmBtn.setText("CONFIRM");

        msg1.setText("Are you sure you want to reset DTR password of ");
        itemDescription.setText(" "+ name + " ?");

        dialog.findViewById(R.id.dialog_delete_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressbar.setVisibility(View.GONE);
                usernameTV.setVisibility(View.GONE);
                resetBtn.setClickable(true);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.dialog_delete_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("reset", "@dialog, current user=" + currentUser + " reset_userid=" +reset_userid);
                progressbar.setVisibility(View.VISIBLE);
                resetViewModel.resetPassword(currentUser,reset_userid);
                dialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

}
