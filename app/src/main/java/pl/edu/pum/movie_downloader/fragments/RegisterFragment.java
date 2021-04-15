package pl.edu.pum.movie_downloader.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.List;

import pl.edu.pum.movie_downloader.R;
import pl.edu.pum.movie_downloader.database.FireBaseAuthHandler;
import pl.edu.pum.movie_downloader.models.User;

public class RegisterFragment extends Fragment
{
    private EditText mNickEditView;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mRepeatedPasswordEditText;
    private Button mRegisterButton;
    private ProgressBar mRegisterProgressBar;
    private CheckBox mShowPasswordsCheckBox;
    private static final int PASSWORD_LENGTH = 8;
    List<EditText> mForm = new ArrayList<EditText>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.register_fragment, container, false);

        mNickEditView = view.findViewById(R.id.nick);
        mEmailEditText = view.findViewById(R.id.email);
        mPasswordEditText = view.findViewById(R.id.password);
        mRepeatedPasswordEditText = view.findViewById(R.id.repeat_password);
        mRegisterButton = view.findViewById(R.id.register_button);
        mRegisterProgressBar = view.findViewById(R.id.wait_for_register_bar);
        mShowPasswordsCheckBox = view.findViewById(R.id.show_passwords_checkbox);

        //get all EditText from register form
        RelativeLayout layout = view.findViewById(R.id.register_form_layout);
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            if (layout.getChildAt(i) instanceof EditText)
            {
                EditText test = (EditText )layout.getChildAt(i);
                mForm.add(test);
            }
        }
        addClearButton(mForm);

        mRegisterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String nickname = mNickEditView.getText().toString();
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String repeatedPassword = mRepeatedPasswordEditText.getText().toString();

                if (ifEmptyField())
                {
                    if (checkPassword(password, repeatedPassword))
                    {
                        setPasswordFieldState("Correct password", 0);
                        mRegisterButton.setVisibility(View.INVISIBLE);
                        mRegisterProgressBar.setVisibility(View.VISIBLE);
                        createNewUser(nickname, email, password);
                    }
                }
            }
        });

        mShowPasswordsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (buttonView.isChecked())
                {
                    mPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mRepeatedPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mRepeatedPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addClearButton(List<EditText> form)
    {
        Drawable default_edit_text_theme = mForm.get(0).getBackground(); //just handle default theme of edittext to change
        for (EditText edt: form)
        {
            edt.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    edt.setBackground(default_edit_text_theme);
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    if (s.length() > 0)
                    {
                        edt.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, R.drawable.ic_baseline_clear_24,0);
                    }
                    else
                    {
                        edt.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, 0,0);
                    }
                }
            });

            edt.setOnTouchListener(new View.OnTouchListener()
            {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        if (edt.getCompoundDrawables()[2] != null)
                        {
                            if(event.getX() >= (edt.getRight()- edt.getLeft() - edt.getCompoundDrawables()[2].getBounds().width()))
                            {
                                edt.setText("");
                            }
                        }
                    }
                    return false;
                }
            });

        }
    }

    private boolean ifEmptyField()
    {
        boolean isEmpty = true;
        for (EditText editText : mForm)
        {
            if (editText.getText().toString().isEmpty())
            {
                editText.setError("This field cannot be blank");
                editText.setBackgroundResource(R.drawable.error_edit_text_background);
                isEmpty = false;
            }
            else
            {
                editText.setBackgroundResource(R.drawable.confirm_edit_text_background);
            }
        }
        return isEmpty;
    }

    private boolean checkPassword(String password, String repeatedPassword)
    {
        if (!password.equals(repeatedPassword))
        {
            setPasswordFieldState("Password didn't match", 1);
            return false;
        }
        String regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        boolean isCorrect = password.matches(regexp);

        if (!isCorrect)
        {
            setPasswordFieldState("Password does not meet the requirements", 1);
        }
        return isCorrect;
    }

    private void setPasswordFieldState(String msg, int state)
    {
        if (state == 0)
        {
            mPasswordEditText.setBackgroundResource(R.drawable.confirm_edit_text_background);
            mRepeatedPasswordEditText.setBackgroundResource(R.drawable.confirm_edit_text_background);
            Log.d("PASSWORD", msg);
        }
        else if (state == 1)
        {
            mPasswordEditText.setBackgroundResource(R.drawable.error_edit_text_background);
            mRepeatedPasswordEditText.setBackgroundResource(R.drawable.error_edit_text_background);
            mRepeatedPasswordEditText.setError(msg);
        }

    }

    private void createNewUser(String nickname, String email, String password)
    {
        User newUser = new User(nickname, email, password);
        FireBaseAuthHandler fireBaseAuthHandler = FireBaseAuthHandler.getInstance();
        FirebaseAuth firebaseAuth = fireBaseAuthHandler.getAuthorization();

        firebaseAuth.createUserWithEmailAndPassword(newUser.getUserEmail(),
                newUser.getUserPassword()).
                addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            FirebaseUser user = FireBaseAuthHandler.getInstance().getAuthorization().getCurrentUser();
                            sendActivationEmailToUser(user);
                            setDisplayNameForNewUser(newUser.getUserNickname(), user);
                            firebaseAuth.signOut();
                            Log.d("User register status", "New account registration successful");
                            Navigation.findNavController(RegisterFragment.this.getView()).navigate(R.id.action_registerFragment_to_logFragment);
                        }
                        else
                        {
                            Log.d("User register status", "New account registration unsuccessful");
                            showErrorAlert();
                        }
                    }
                });
    }

    private void sendActivationEmailToUser(FirebaseUser user)
    {
        //send verification email for new user email
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                Toast.makeText(getContext(), "Verification E-mail has been sent.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.d("Activation link status", "onFailure: Email not sent " + e.toString());

            }
        });
    }

    private void setDisplayNameForNewUser(String nick, FirebaseUser user)
    {
        //set Display name for new user

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                .Builder()
                .setDisplayName(nick)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Log.d("User account update status", "User profile updated");
                    }
                });
    }

    private void showErrorAlert()
    {
        mRegisterButton.setVisibility(View.VISIBLE);
        mRegisterProgressBar.setVisibility(View.INVISIBLE);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.drawable.rounded_corners).create();
        alertDialog.setTitle("Register failure");
        alertDialog.setMessage("An error occurred during sign in.\n" +
                "Please check your registration details or try again later.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mRegisterButton.setVisibility(View.VISIBLE);
        mRegisterProgressBar.setVisibility(View.INVISIBLE);
    }
}
