package ru.kuzin.practice16;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class UserActivity extends Activity {
    private EditText nameTextView, stateTextView, ageTextView;
    private User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        new UserStaticInfo();
        int position = getIntent().getIntExtra(UserStaticInfo.POSITION, -1);
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
        activeUser.setName(nameTextView.getText().toString());
        activeUser.setState(stateTextView.getText().toString());
        activeUser.setAge(Transform.parseIntOrDefault(ageTextView.getText().toString(), activeUser.getAge()));
        finish();
    }
}
