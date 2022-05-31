package com.hdev.common.datamodels;

import androidx.fragment.app.Fragment;

public class BackPressedModel {
    private Fragment fragment;
    private String Tag;

    public BackPressedModel() {
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }
}
