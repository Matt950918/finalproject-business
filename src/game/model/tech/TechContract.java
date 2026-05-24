package game.model.tech;

import java.util.Random;

public class TechContract {

    private String partnerName;
    private String description;

    private double revenue; // 每期可獲取的收入 (下游訂單才有)
    private double cost;    // 每期需支付的成本 (上游或開發成本)

    private int durationTicks; // 合約持續期數
    private boolean isNegotiated; // 防呆機制：一份合約只能談判一次

    private static final Random random = new Random();

    public TechContract(String partnerName, String description, double revenue, double cost, int durationTicks) {
        this.partnerName = partnerName;
        this.description = description;
        this.revenue = revenue;
        this.cost = cost;
        this.durationTicks = durationTicks;
        this.isNegotiated = false;
    }

    // ==========================================
    // 🔪 核心玩法：跟上游砍價 (壓低成本)
    // ==========================================
    public boolean negotiateCostDown(int aiResearchLevel) {
        if (isNegotiated) return false;
        this.isNegotiated = true;

        // 基礎成功率 35%，每級 AI 升級增加 5% 談判籌碼
        double successRate = 0.35 + (aiResearchLevel * 0.05);

        System.out.println("💬 CEO 嘗試向 " + partnerName + " 砍價...");
        if (random.nextDouble() < successRate) {
            this.cost *= 0.75; // 談判成功，成本打 75 折！毛利大增！
            System.out.println("✅ 談判成功！" + partnerName + " 妥協，成本大幅下降！");
            return true;
        } else {
            System.out.println("❌ 談判破裂！" + partnerName + " 覺得你太沒誠意，直接抽單！");
            return false;
        }
    }

    // ==========================================
    // 💰 核心玩法：跟下游抬價 (提高分潤)
    // ==========================================
    public boolean negotiateRevenueUp(int aiResearchLevel) {
        if (isNegotiated) return false;
        this.isNegotiated = true;

        // 抬價比較難，基礎成功率只有 25%
        double successRate = 0.25 + (aiResearchLevel * 0.05);

        System.out.println("💬 CEO 要求 " + partnerName + " 提高採購報價...");
        if (random.nextDouble() < successRate) {
            this.revenue *= 1.30; // 談判成功，營收暴增 30%！
            System.out.println("✅ 談判成功！" + partnerName + " 勉強同意提高報價！");
            return true;
        } else {
            System.out.println("❌ 談判破裂！" + partnerName + " 轉頭去找別家供應商了！");
            return false;
        }
    }

    // ==========================================
    // 結算邏輯
    // ==========================================
    public double processTick() {
        if (durationTicks > 0) {
            durationTicks--;
            // 每期的淨利潤 = 收入 - 成本 (如果是純上游合約，淨利潤會是負的)
            return revenue - cost;
        }
        return 0;
    }

    // Getters
    public String getPartnerName() { return partnerName; }
    public String getDescription() { return description; }
    public double getRevenue() { return revenue; }
    public double getCost() { return cost; }
    public double getMargin() { return revenue - cost; } // 當前毛利
    public int getDurationTicks() { return durationTicks; }
    public boolean isNegotiated() { return isNegotiated; }
}