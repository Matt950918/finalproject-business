package game.model.bank;

import java.util.ArrayList;
import java.util.List;

public class bank_system {

    // 💰 初始資金 5000 萬
    private double money = 50_000_000;

    private List<bank_LoanRequest> loans = new ArrayList<>();

    // 💸 確實從金庫扣款的機制
    public boolean deductMoney(double amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public void addLoan(bank_LoanRequest request) {
        loans.add(request);
    }

    // ==========================================
    // 🔄 換日結算 (注意這裡已經改成 List<String> 了！)
    // ==========================================
    public List<String> tick() {
        List<String> dailyReports = new ArrayList<>();
        List<bank_LoanRequest> toRemove = new ArrayList<>();

        for (bank_LoanRequest loan : loans) {
            // 1. 檢查是否違約 (爆雷)
            if (loan.checkDefault()) {
                String boomMsg = bank_Customer.getEndGameScript(loan.getApplicantName(), "BOOM");

                // 💡 魔法過濾器：自動把 CSV 裡的【突發事件】等括弧與裡面的字刪除
                if (boomMsg != null) {
                    boomMsg = boomMsg.replaceAll("【.*?】", "").trim();
                }

                // 保留系統的第一句括弧
                dailyReports.add("💥 【違約呆帳】" + loan.getApplicantName() + " 跑路了！\n" + boomMsg);
                toRemove.add(loan);
                continue;
            }

            // 2. 正常收取本期還款
            double income = loan.processTickPayment();
            money += income;

            // 3. 檢查是否已全數還清
            if (loan.getTicksRemaining() <= 0) {
                String successMsg = bank_Customer.getEndGameScript(loan.getApplicantName(), "SUCCESS");

                // 💡 同理，自動把 CSV 裡的還款括弧過濾掉
                if (successMsg != null) {
                    successMsg = successMsg.replaceAll("【.*?】", "").trim();
                }

                // 保留系統的第一句括弧
                dailyReports.add("✅ 【還款結清】" + loan.getApplicantName() + " 合約期滿，本利和已全數結清！\n" + successMsg);
                toRemove.add(loan);
            }
        }

        loans.removeAll(toRemove);

        // 👈 把今天的報告交給主畫面去彈窗
        return dailyReports;
    }

    public double getMoney() {
        return money;
    }

    public int getLoanCount() {
        return loans.size();
    }
}