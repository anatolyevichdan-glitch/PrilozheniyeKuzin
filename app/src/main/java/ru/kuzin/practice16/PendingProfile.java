package ru.kuzin.practice16;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

// Allows a profile write to be retried after a temporary database failure.
final class PendingProfile {
    private static final String FILE = "pending_profile";
    private PendingProfile() {}

    static void save(Context context, String uid, String name, int age, String state) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
                .putString("uid", uid).putString("name", name).putInt("age", age)
                .putString("state", state).apply();
    }

    static Map<String, Object> forUser(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        if (!uid.equals(prefs.getString("uid", ""))) return null;
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", prefs.getString("name", ""));
        profile.put("age", prefs.getInt("age", 0));
        profile.put("state", prefs.getString("state", ""));
        return profile;
    }

    static void clear(Context context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
