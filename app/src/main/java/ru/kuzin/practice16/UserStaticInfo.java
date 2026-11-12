package ru.kuzin.practice16;

import java.util.ArrayList;
import java.util.List;

public class UserStaticInfo {
    public static final String POSITION = "position";
    public static final List<User> users = new ArrayList<>();

    public UserStaticInfo() {
        if (users.isEmpty()) {
            users.add(new User("Иван", "На связи", 19, 0));
            users.add(new User("Анна", "Хорошего дня!", 20, 1));
            users.add(new User("Михаил", "Скоро вернусь", 21, 2));
            users.add(new User("Мария", "Занята учёбой", 19, 0));
        }
    }
}
