package ru.kuzin.practice16;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;


public class SignActivity extends Activity {
    private static final String PREFERENCES = "mysettings";
    private static final String LOGIN = "login";
    private EditText login, password, newLogin, newPassword, newName, newAge, newState;
    private Button signInButton, signUpButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        auth = FirebaseAuth.getInstance();
        TabHost tabs = findViewById(R.id.tabHost);
        tabs.setup();
        tabs.addTab(tabs.newTabSpec("signin").setIndicator(getString(R.string.sign_in)).setContent(R.id.tabSignIn));
        tabs.addTab(tabs.newTabSpec("signup").setIndicator(getString(R.string.sign_up_tab)).setContent(R.id.tabSignUp));
        login = findViewById(R.id.LoginTextView);
        password = findViewById(R.id.PasswordTextView);
        newLogin = findViewById(R.id.NewLoginTextView);
        newPassword = findViewById(R.id.NewPasswordTextView);
        newName = findViewById(R.id.NewNameTextView);
        newAge = findViewById(R.id.NewAgeTextView);
        newState = findViewById(R.id.NewStateTextView);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
        login.setText(getSharedPreferences(PREFERENCES, MODE_PRIVATE).getString(LOGIN, ""));
        // Firebase Authentication safely restores its own session; no password is saved here.
        if (auth.getCurrentUser() != null) openLoading();
    }

    public void SignIn(View view) { signIn(); }

    private void signIn() {
        if (!required(login, password)) return;
        String email = login.getText().toString().trim();
        String pass = password.getText().toString();
        if (!validateEmail(email)) return;
        setBusy(true);
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, task -> {
            setBusy(false);
            if (task.isSuccessful()) {
                rememberEmail(email);
                openLoading();
            } else {
                showError(task.getException(), R.string.invalid_credentials);
            }
        });
    }

    public void SignUp(View view) {
        if (!required(newLogin, newPassword, newName, newState)) return;
        String email = newLogin.getText().toString().trim();
        String pass = newPassword.getText().toString();
        String name = newName.getText().toString().trim();
        String state = newState.getText().toString().trim();
        int age = Transform.parseIntOrDefault(newAge.getText().toString(), 0);
        if (!validateEmail(email)) return;
        if (pass.length() < 6) { toast(R.string.weak_password); return; }
        setBusy(true);
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                setBusy(false);
                showError(task.getException(), R.string.auth_error);
                return;
            }
            String uid = task.getResult().getUser().getUid();
            PendingProfile.save(this, uid, name, age, state);
            rememberEmail(email);
            setBusy(false);
            openLoading();
        });
    }

    private boolean required(EditText... fields) {
        boolean valid = true;
        for (EditText field : fields) {
            if (field.getText().toString().trim().isEmpty()) {
                field.startAnimation(AnimationUtils.loadAnimation(this, R.anim.error_edit));
                valid = false;
            }
        }
        if (!valid) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= 26)
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                else vibrator.vibrate(150);
            }
            toast(R.string.empty_fields);
        }
        return valid;
    }

    private boolean validateEmail(String email) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) return true;
        toast(R.string.invalid_email);
        return false;
    }

    private void setBusy(boolean busy) {
        signInButton.setEnabled(!busy);
        signUpButton.setEnabled(!busy);
    }

    private void rememberEmail(String email) {
        getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit().putString(LOGIN, email).apply();
    }

    private void openLoading() {
        startActivity(new Intent(this, LoadedUserDataActivity.class));
        finish();
    }

    private void showError(Exception error, int fallback) {
        if (error instanceof FirebaseAuthUserCollisionException) toast(R.string.user_exists);
        else if (error instanceof FirebaseNetworkException) toast(R.string.network_error);
        else toast(fallback);
    }

    private void toast(int message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
