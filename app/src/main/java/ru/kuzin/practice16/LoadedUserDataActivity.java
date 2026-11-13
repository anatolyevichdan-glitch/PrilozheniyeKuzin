package ru.kuzin.practice16;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import java.util.Map;

public class LoadedUserDataActivity extends Activity {
    private MediaPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean loading;
    private int requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loaded_user_data);
        player = MediaPlayer.create(this, R.raw.noise);
        if (player != null) {
            player.setVolume(0.2f, 0.2f);
            player.setLooping(true);
        }
        loadProfile();
    }

    private void loadProfile() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) { signOut(null); return; }
        loading = true;
        findViewById(R.id.retryLoading).setVisibility(View.GONE);
        findViewById(R.id.signOutLoading).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.loadingText)).setText(R.string.loading);
        int request = ++requestId;
        handler.postDelayed(() -> {
            if (loading && request == requestId) showFailure();
        }, 15000);
        DatabaseReference profile = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url))
                .getReference("UsersProfileInfo").child(firebaseUser.getUid());
        Map<String, Object> pending = PendingProfile.forUser(this, firebaseUser.getUid());
        if (pending != null) {
            profile.setValue(pending).addOnCompleteListener(this, saved -> {
                if (!loading || request != requestId || isFinishing()) return;
                if (!saved.isSuccessful()) { showFailure(); return; }
                PendingProfile.clear(this);
                readProfile(profile, firebaseUser.getUid(), request);
            });
        } else readProfile(profile, firebaseUser.getUid(), request);
    }

    private void readProfile(DatabaseReference profile, String uid, int request) {
        profile.get().addOnCompleteListener(this, task -> {
            if (!loading || request != requestId || isFinishing()) return;
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                showFailure(); return;
            }
            DataSnapshot data = task.getResult();
            String name = data.child("name").getValue(String.class);
            String state = data.child("state").getValue(String.class);
            Long age = data.child("age").getValue(Long.class);
            if (name == null || state == null) { showFailure(); return; }
            UserStaticInfo.users.clear();
            new UserStaticInfo();
            UserStaticInfo.users.add(0, new User(name, state, age == null ? 0 : age.intValue(), 1));
            UserStaticInfo.profileId = uid;
            loading = false;
            handler.postDelayed(() -> {
                if (!isFinishing()) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            }, 800);
        });
    }

    private void showFailure() {
        loading = false;
        ((TextView) findViewById(R.id.loadingText)).setText(R.string.profile_error);
        findViewById(R.id.retryLoading).setVisibility(View.VISIBLE);
        findViewById(R.id.signOutLoading).setVisibility(View.VISIBLE);
    }

    public void retryLoading(View view) { if (!loading) loadProfile(); }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
        UserStaticInfo.users.clear();
        UserStaticInfo.profileId = null;
        startActivity(new Intent(this, SignActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) player.start();
    }

    @Override
    protected void onStop() {
        if (player != null && player.isPlaying()) player.pause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (player != null) { player.release(); player = null; }
        super.onDestroy();
    }
}
