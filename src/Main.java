 // 如果 Main 放在 game.model 底下，請保留這行；如果放在 src 底下，請刪除這行
public class Main {
    public static void main(String[] args) {
        // 這裡會強制指定去啟動大寫的 MainApp 畫面
        javafx.application.Application.launch(Mainapp.class, args);
    }
}