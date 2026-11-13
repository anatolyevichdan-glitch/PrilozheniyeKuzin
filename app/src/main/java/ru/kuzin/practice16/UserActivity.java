package ru.kuzin.practice16;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class UserActivity extends Activity {
    private EditText nameTextView, stateTextView, ageTextView;
    private User activeUser;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, SignActivity.class));
            finish();
            return;
        }
        if (UserStaticInfo.profileId == null || !UserStaticInfo.profileId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            startActivity(new Intent(this, LoadedUserDataActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_user);
        new UserStaticInfo();
        position = getIntent().getIntExtra(UserStaticInfo.POSITION, -1);
        if (position < 0 || position >= UserStaticInfo.users.size()) { finish(); return; }
        activeUser = UserStaticInfo.users.get(position);
        nameTextView = findViewById(R.id.NameTextView);
        stateTextView = findViewById(R.id.StateTextView);
        ageTextView = findViewById(R.id.AgeTextView);
        if (savedInstanceState == null) {
            nameTextView.setText(activeUser.getName());
            stateTextView.setText(activeUser.getState());
            ageTextView.setText(String.valueOf(activeUser.getAge()));
        }
    }

    public void Back(View view) { finish(); }

    public void Save(View view) {
        String name = nameTextView.getText().toString().trim();
        String state = stateTextView.getText().toString().trim();
        int age = Transform.parseIntOrDefault(ageTextView.getText().toString(), activeUser.getAge());
        if (position == 0 && UserStaticInfo.profileId != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            Map<String, Object> profile = new HashMap<>();
            profile.put("name", name);
            profile.put("state", state);
            profile.put("age", age);
            view.setEnabled(false);
            FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("UsersProfileInfo")
                    .child(UserStaticInfo.profileId).setValue(profile).addOnCompleteListener(this, task -> {
                        view.setEnabled(true);
                        if (task.isSuccessful()) { updateUser(name, state, age); finish(); }
                        else Toast.makeText(this, R.string.profile_error, Toast.LENGTH_SHORT).show();
                    });
        } else { updateUser(name, state, age); finish(); }
    }

    private void updateUser(String name, String state, int age) {
        activeUser.setName(name);
        activeUser.setState(state);
        activeUser.setAge(age);
    }
}
