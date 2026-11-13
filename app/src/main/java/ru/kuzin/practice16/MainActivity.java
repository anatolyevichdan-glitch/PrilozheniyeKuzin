package ru.kuzin.practice16;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends Activity {
    private FrameLayout userPanel;
    private TextView nameTextView, stateTextView, ageTextView;
    private UserListAdapter userListAdapter;
    private int positionActiveUser = -1;

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
        setContentView(R.layout.activity_main);
        new UserStaticInfo();
        userPanel = findViewById(R.id.userPanel);
        nameTextView = findViewById(R.id.NameTextView);
        stateTextView = findViewById(R.id.StateTextView);
        ageTextView = findViewById(R.id.AgeTextView);
        ListView listView = findViewById(R.id.listView);
        userListAdapter = new UserListAdapter();
        listView.setAdapter(userListAdapter);
        if (savedInstanceState != null) {
            positionActiveUser = savedInstanceState.getInt("selected", -1);
            if (positionActiveUser >= 0 && positionActiveUser < UserStaticInfo.users.size()) {
                initPanel(UserStaticInfo.users.get(positionActiveUser));
                userPanel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("selected", userPanel.getVisibility() == View.VISIBLE ? positionActiveUser : -1);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userListAdapter != null) userListAdapter.notifyDataSetChanged();
        if (positionActiveUser >= 0 && positionActiveUser < UserStaticInfo.users.size()) {
            initPanel(UserStaticInfo.users.get(positionActiveUser));
        }
    }

    private void initPanel(User user) {
        nameTextView.setText(user.getName());
        stateTextView.setText(user.getState());
        ageTextView.setText(getString(R.string.age_value, user.getAge()));
    }

    public void SignOut(View view) {
        FirebaseAuth.getInstance().signOut();
        UserStaticInfo.users.clear();
        UserStaticInfo.profileId = null;
        startActivity(new Intent(this, SignActivity.class));
        finish();
    }

    public void BackToList(View view) {
        userPanel.setVisibility(View.GONE);
    }

    public void EditUser(View view) {
        if (positionActiveUser < 0) return;
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(UserStaticInfo.POSITION, positionActiveUser);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (userPanel.getVisibility() == View.VISIBLE) userPanel.setVisibility(View.GONE);
        else super.onBackPressed();
    }

    private class UserListAdapter extends BaseAdapter {
        @Override public int getCount() { return UserStaticInfo.users.size(); }
        @Override public User getItem(int position) { return UserStaticInfo.users.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(final int position, View currentView, ViewGroup parent) {
            if (currentView == null) currentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_user, parent, false);
            User user = getItem(position);
            ((TextView) currentView.findViewById(R.id.NameTextView)).setText(user.getName());
            ((TextView) currentView.findViewById(R.id.StateTextView)).setText(user.getState());
            FrameLayout stateRound = currentView.findViewById(R.id.StateRound);
            switch (user.getStateSignal()) {
                case 1: stateRound.setBackgroundResource(R.drawable.back_online); break;
                case 2: stateRound.setBackgroundResource(R.drawable.back_departed); break;
                default: stateRound.setBackgroundResource(R.drawable.back_offline); break;
            }
            currentView.setOnClickListener(view -> {
                positionActiveUser = position;
                initPanel(getItem(position));
                userPanel.setVisibility(View.VISIBLE);
            });
            return currentView;
        }
    }
}
