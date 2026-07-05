package net.payload.interfaces;

public interface IClientPlayerInteractionManager {
    float getCurrentBreakingProgress();
    void setCurrentBreakingProgress(float progress);

    void payload$syncSelected();
}