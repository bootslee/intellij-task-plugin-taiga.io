package com.busybusy.intellij.taiga.models;

import com.intellij.util.xmlb.annotations.Tag;

import java.util.List;

/**
 * Created by Tjones on 5/26/15.
 */
@Tag("TaigaProject")
public class TaigaProject {
    private String mProjectId;
    private String mProjectTitle;
    private String mSlug;
    private List<TaigaTaskStatus> mStatusList;

    public TaigaProject() {
    }

    public String getProjectId() {
        return mProjectId;
    }

    public void setProjectId(String mProjectId) {
        this.mProjectId = mProjectId;
    }

    public String getProjectTitle() {
        return mProjectTitle;
    }

    public void setProjectTitle(String mProjectTitle) {
        this.mProjectTitle = mProjectTitle;
    }

    public String getSlug() {
        return mSlug;
    }

    public void setSlug(String slug) {
        this.mSlug = slug;
    }

    public List<TaigaTaskStatus> getStatusList() {
        return mStatusList;
    }

    public TaigaProject setStatusList(List<TaigaTaskStatus> statusList) {
        mStatusList = statusList;
        return this;
    }

    @Override
    public String toString() {
        return mProjectTitle != null ? mProjectTitle : super.toString();
    }
}
