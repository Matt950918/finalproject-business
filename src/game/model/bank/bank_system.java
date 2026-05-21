package game.model.bank;

import java.util.ArrayList;
import java.util.List;

public class bank_system {

    private double money = 1_000_000;

    private List<bank_LoanRequest> loans = new ArrayList<>();

    // 📥 新增貸款
    public void addLoan(bank_LoanRequest request) {
        loans.add(request);
    }

    // ⏱ 每回合運作
    public void tick() {

        List<bank_LoanRequest> toRemove = new ArrayList<>();

        for (bank_LoanRequest loan : loans) {

            // 💣 違約事件
            if (loan.checkDefault()) {
                System.out.println("💥 " + loan.getApplicantName() + " 違約！");
                toRemove.add(loan);
                continue;
            }

            // 💰 收款
            double income = loan.processTickPayment();
            money += income;

            // ✔ 還完移除
            if (loan.getRejectCount() > 0 && loan.getAmount() <= 0) {
                toRemove.add(loan);
            }
        }

        loans.removeAll(toRemove);
    }

    public double getMoney() {
        return money;
    }

    public int getLoanCount() {
        return loans.size();
    }
}
