package client;

import client.controller.AuthControl;
import client.model.ClientApp;

import java.util.prefs.Preferences;

/**
 * Класс, сохраняющий пользовательские настройки
 */
public class UserPreferences {
    private static AuthControl authControl;
    private static Preferences userPrefs = Preferences.userRoot().node("hermespref");

    /** Сохраняет IP-адрес сервера */
    public static void savePrefIpAdress(String ipAdress) {
        userPrefs.put("ipAdress", ipAdress);
    }

    /** Сохраняет логин пользователя */
    public static void savePrefUserName(String username) {
        userPrefs.put("userName", username);
    }

    /** Загружает последний используемый IP-адрес сервера */
    public static void loadPrefIpArdess(){
        ClientApp.setIp(userPrefs.get("ipAdress", "10.50.2.254"));
    }

    /** Загружает логин последнего авторизованного пользователя */
    public static void loadPrefUserName(){
        authControl.setUsername(userPrefs.get("userName", ""));
    }

    public static void setAuthControl(AuthControl authControl) {
        UserPreferences.authControl = authControl;
    }
}
