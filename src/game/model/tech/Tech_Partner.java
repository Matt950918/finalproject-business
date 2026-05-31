package game.model.tech;

import java.io.Serializable;
import java.util.Random;

public class Tech_Partner implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Random random = new Random();

    // 🔼 上游供應商
    private static final String[] UPSTREAM_COMPANIES = {
            "ASML (荷蘭)", "台積電 (台灣)", "應用材料 (美國)", "東京威力 (日本)",
            "日月光 (台灣)", "聯電 (台灣)", "英特爾 (美國)", "三星電子 (韓國)",
            "科林研發 (美國)", "信越化學 (日本)", "SK海力士 (韓國)", "聯發科 (台灣)",
            "美光 (美國)", "力積電 (台灣)", "華邦電 (台灣)", "南亞科 (台灣)",
            "京元電子 (台灣)", "矽品 (台灣)", "環球晶 (台灣)", "默克電子材料 (德國)",
            "KLA (美國)", "新思科技 (美國)", "Cadence (美國)", "艾司摩爾 EUV (荷蘭)",
            "瑞薩電子 (日本)", "德州儀器 (美國)", "恩智浦 NXP (荷蘭)", "英飛凌 (德國)"
    };

    private static final String[] UPSTREAM_DESCRIPTIONS = {
            "頂尖微影製程設備供應", "全球晶圓代工龍頭", "半導體沉積蝕刻設備", "先進製程塗佈顯影設備",
            "全球封測市佔冠軍", "成熟製程晶圓大廠", "IDM 垂直整合製造巨頭", "記憶體與邏輯晶片整合",
            "電漿蝕刻技術專家", "半導體高純度矽晶圓", "全球DRAM記憶體大廠", "行動通訊晶片設計研發",
            "儲存型記憶體領先者", "邏輯晶片與記憶體代工", "利基型記憶體供應商", "動態隨機存取記憶體",
            "專業半導體測試服務", "高階積體電路封測", "半導體矽晶圓材料商", "電子化學特殊材料",
            "光學檢測與量測系統", "IC設計自動化軟體EDA", "電子系統設計軟體EDA", "極紫外光微影技術支援",
            "車用微控制器領先者", "類比訊號與嵌入式處理", "車用與工業半導體方案", "能源效率與電源晶片"
    };

    // 🔽 下游客戶
    private static final String[] DOWNSTREAM_COMPANIES = {
            "NVIDIA (美國)", "Apple (美國)", "Microsoft (美國)", "AMD (美國)",
            "Tesla (美國)", "Google (美國)", "AWS (美國)", "Meta (美國)",
            "任天堂 (日本)", "Sony (日本)", "比亞迪 (中國)", "小米 (中國)",
            "高通 (美國)", "戴爾 (美國)", "博通 (美國)", "OpenAI (美國)",
            "Oracle (美國)", "阿里巴巴 (中國)", "騰訊 (中國)", "百度 (中國)",
            "華華為 (中國)", "聯想 (中國)", "惠普 HP (美國)", "華碩 (台灣)",
            "宏碁 (台灣)", "技嘉 (台灣)", "微星 (台灣)", "Lenovo (中國)",
            "Netflix (美國)", "Uber (美國)", "Lucid Motors (美國)", "Rivian (美國)",
            "蔚來 NIO (中國)", "理想汽車 (中國)", "小鵬 XPeng (中國)", "BMW (德國)",
            "賓士 Mercedes (德國)", "福斯 VW (德國)", "Toyota (日本)", "Honda (日本)",
            "Canon (日本)", "Panasonic (日本)", "DJI 大疆 (中國)"
    };

    private static final String[] DOWNSTREAM_DESCRIPTIONS = {
            "AI 數據中心運算加速", "消費性電子與手機終端", "全球雲端計算服務", "高效能運算處理器",
            "自動駕駛系統運算單元", "搜尋引擎與 AI 雲端", "企業級雲端基礎設施", "AI 與元宇宙生態系",
            "家用掌上型娛樂裝置", "消費影音電子與傳感", "新能源電動車供應鏈", "行動物聯網生態終端",
            "5G 通訊數據晶片研發", "企業運算與個人終端", "高效能網路通訊技術", "人工智慧演算法平台",
            "企業級雲端軟體整合", "大型電商與雲端服務", "社群網路與內容運算", "AI 搜尋與自駕技術",
            "網路通訊設備與終端", "高運算效能 PC 與伺服器", "企業級個人電腦終端", "電競與商業筆電方案",
            "個人電腦與智慧設備", "高品質顯示卡與主機板", "高效能電競系統平台", "全球伺服器製造商",
            "串流影音基礎運算設施", "共享出行數據運算", "高階電動車自動駕駛", "純電智慧汽車運算",
            "電動車能源管理系統", "智慧座艙運算平台", "自動駕駛生態軟體", "豪華汽車電子整合",
            "高級自動駕駛輔助系統", "汽車電子智慧控制", "油電混動控制系統", "混合動力控制方案",
            "影像處理與光學設備", "消費電子與電池儲能", "無人機航控系統"
    };

    public static TechContract generateRandomContract(int aiLevel) {
        boolean isUpstream = random.nextBoolean();

        if (isUpstream) {
            int idx = random.nextInt(UPSTREAM_COMPANIES.length);
            double multiplier = 1.0 + (aiLevel * 0.5);
            double baseCost = 200_000 + random.nextInt(300_000);
            double cost = baseCost * multiplier;
            int duration = 5 + random.nextInt(6);
            // 修正索引取值，確保對應描述
            return new TechContract(UPSTREAM_COMPANIES[idx], UPSTREAM_DESCRIPTIONS[idx], 0, cost, duration);
        } else {
            int idx = random.nextInt(DOWNSTREAM_COMPANIES.length);
            double multiplier = 1.0 + (aiLevel * 0.5);
            double baseRevenue = 400_000 + random.nextInt(600_000);
            double revenue = baseRevenue * multiplier;
            int duration = 5 + random.nextInt(6);
            // 修正索引取值，確保對應描述
            return new TechContract(DOWNSTREAM_COMPANIES[idx], DOWNSTREAM_DESCRIPTIONS[idx], revenue, 0, duration);
        }
    }
}