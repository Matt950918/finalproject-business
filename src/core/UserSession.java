package core;

public class UserSession {
    // 目前登入者帳號
    public static String username = null;

    // 💡 新增：儲存目前登入者輸入的公司名稱
    public static String companyName = "遠東集團";

    // 是否已登入
    public static boolean isLoggedIn() {
        return username != null;
    }

    // 登出
    public static void logout() {
        username = null;
        companyName = "遠東集團"; // 登出時重置
    }
}