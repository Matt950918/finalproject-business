package game.model;

import java.util.ArrayList;
import java.util.List;

public class DailyNews {
    private String title;
    private IndustryType targetIndustry; // 如果是 null，代表全產業通用
    private List<NewsOption> options;

    public DailyNews(String title, IndustryType targetIndustry) {
        this.title = title;
        this.targetIndustry = targetIndustry;
        this.options = new ArrayList<>();
    }

    public void addOption(NewsOption option) {
        this.options.add(option);
    }

    public String getTitle() { return title; }
    public IndustryType getTargetIndustry() { return targetIndustry; }
    public List<NewsOption> getOptions() { return options; }
}