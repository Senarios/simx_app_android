package com.hdev.common.datamodels;

import java.io.Serializable;
import java.util.Objects;

public class Tags implements Serializable {
    private String broadcast,tag;

    public Tags(String broadcast, String tag) {
        this.broadcast = broadcast;
        this.tag = tag;
    }

    public String getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
      return o!=null && this.getTag().equalsIgnoreCase(((Tags)o).tag);
    }

    @Override
    public int hashCode() {
        return this.tag.hashCode()*31;
    }
}
