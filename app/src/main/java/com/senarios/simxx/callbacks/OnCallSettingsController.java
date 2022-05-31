package com.senarios.simxx.callbacks;


public interface OnCallSettingsController {

    void onSwitchAudio();

    void onCaptureFormatChange(int width, int height, int framerate);
}
