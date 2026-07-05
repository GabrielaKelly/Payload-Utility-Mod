package net.payload.settings;


import java.util.ArrayList;
import java.util.List;

public class SettingGroup extends Setting<Boolean> {
    private List<Setting<?>> settings = new ArrayList<>();
    private boolean expanded = false;

    public SettingGroup(String ID, String displayName, String description) {
        super(ID, displayName, description, false);
        this.type = TYPE.BOOLEAN; // Using boolean type for expandable state
    }

    public void addSetting(Setting<?> setting) {
        settings.add(setting);
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggle() {
        this.expanded = !this.expanded;
    }

    @Override
    protected boolean isValueValid(Boolean value) {
        return true;
    }

    public static class Builder extends Setting.BUILDER<Builder, SettingGroup, Boolean> {
        @Override
        public SettingGroup build() {
            if (id == null) throw new IllegalStateException("ID must be set");
            return new SettingGroup(id, displayName != null ? displayName : id, description != null ? description : "");
        }

        public static Builder builder() {
            return new Builder();
        }
    }
}