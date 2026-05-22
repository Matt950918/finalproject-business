package core;

public class UserSession {

    // 目前登入者帳號
    public static String username = null;

    // 是否已登入
    public static boolean isLoggedIn() {
        return username != null;
    }

    // 登出
    public static void logout() {
        username = null;
    }
}