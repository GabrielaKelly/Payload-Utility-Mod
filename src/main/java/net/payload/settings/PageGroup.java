package net.payload.settings;

import java.util.ArrayList;
import java.util.List;

public class PageGroup extends Setting<Boolean> {
    private List<Page> pages = new ArrayList<>();
    private int currentPage = 0;

    public PageGroup(String ID, String description) {
        super(ID, description, false);
        this.type = TYPE.BOOLEAN;
    }

    public void addPage(Page page) {
        pages.add(page);
    }

    public List<Page> getPages() {
        return pages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    @Override
    protected boolean isValueValid(Boolean value) {
        return true;
    }

    public static class Page {
        private final String name;
        private final List<Setting<?>> settings = new ArrayList<>();

        public Page(String name) {
            this.name = name;
        }

        public void addSetting(Setting<?> setting) {
            settings.add(setting);
        }

        public String getName() {
            return name;
        }

        public List<Setting<?>> getSettings() {
            return settings;
        }
    }

    public static class Builder extends Setting.BUILDER<Builder, PageGroup, Boolean> {
        @Override
        public PageGroup build() {
            if (id == null) throw new IllegalStateException("ID must be set");
            return new PageGroup(id, description != null ? description : "");
        }

        public static Builder builder() {
            return new Builder();
        }
    }
}