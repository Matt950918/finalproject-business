package game.model.tech;

import java.io.Serializable;
import java.util.Random;

public class Tech_Partner implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Random random = new Random();

    private static final String[] UPSTREAM_COMPANIES = {
            "ASML艾司摩爾", "台積電TSMC", "應用材料AMAT", "東京威力科創TEL", "信越化學",
            "日月光ASE", "聯電UMC", "英特爾Intel"
    };

    private static final String[] DOWNSTREAM_COMPANIES = {
            "輝達NVIDIA", "蘋果Apple", "微軟Microsoft", "超微AMD", "特斯拉Tesla",
            "谷歌Google", "亞馬遜AWS", "Meta臉書"
    };

    private static final String[] UPSTREAM_DESCRIPTIONS = {
            "提供極紫外光EUV微影設備租賃，是提升奈米製程的絕對關鍵核心。",
            "提供高階晶圓代工產能保留合約，確保我方晶片架構能順利排產。",
            "供應全球最先進的薄膜沉積設備與晶圓化學機械平坦化製程支援。",
            "採購高效能晶圓塗佈顯影設備，能大幅優化初期晶片良率與成本結構。",
            "供應高純度12吋矽晶圓原材料，是所有晶片設計落地的基礎起點。",
            "負責晶片成型後的先進封裝與測試服務，保障出貨前的物理良率。",
            "提供成熟製程產能支援，適合常規控制晶片的穩定外包代工。",
            "提供高階晶圓IDM產線協同開發合約，分攤初期高昂的流片費用。"
    };

    private static final String[] DOWNSTREAM_DESCRIPTIONS = {
            "大舉採購下一代 Blackwell 架構 AI 伺服器晶片，分潤極為豐厚。",
            "計畫在下一代 iPhone 中全面導入我方自研的高效率低功耗神經網路核心。",
            "為其 Azure 全球資料中心訂購特製的雲端推理加速卡，合約規模龐大。",
            "尋求客製化 APU 晶片設計方案，用以整合其最新的行動處理器產品線。",
            "為全新車載自動駕駛電腦（FSD）尋求高效能運算晶片供應商。",
            "為其 TPU 產線尋求第二供應鏈，看中我方高度客製化的架構設計。",
            "為其引領物聯網生態的智慧硬體群採購常規處理器，出貨量極大。",
            "為其全新元宇宙眼鏡與硬體設備採購客製化低延遲影像顯示驅動晶片。"
    };

    // 💡 核心變更：傳入 aiLevel 參數，讓合約價值隨科技動態放大！
    public static TechContract generateRandomContract(int aiLevel) {
        boolean isUpstream = random.nextBoolean();
        int idx = random.nextInt(8);

        // 🚀 科技規模乘數：Lv.0 就是 1 倍，Lv.1 是 1.5 倍，Lv.4 直接暴增 3 倍市場規模！
        double multiplier = 1.0 + (aiLevel * 0.5);

        if (isUpstream) {
            String partner = UPSTREAM_COMPANIES[idx];
            String desc = UPSTREAM_DESCRIPTIONS[idx];

            // 🔪 大砍底層數值：初始只剩 20萬 ~ 50萬，再乘上科技倍率
            double baseCost = 200_000 + random.nextInt(300_000);
            double cost = baseCost * multiplier;

            int duration = 5 + random.nextInt(6);
            return new TechContract(partner, desc, 0, cost, duration);
        } else {
            String partner = DOWNSTREAM_COMPANIES[idx];
            String desc = DOWNSTREAM_DESCRIPTIONS[idx];

            // 🔪 大砍底層數值：初始只剩 40萬 ~ 100萬，再乘上科技倍率
            double baseRevenue = 400_000 + random.nextInt(600_000);
            double revenue = baseRevenue * multiplier;

            int duration = 5 + random.nextInt(6);
            return new TechContract(partner, desc, revenue, 0, duration);
        }
    }
}