package com.philong.androidaudiorecord.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Long on 08/03/2017.
 */

public class RecordingItem implements Parcelable {

    private String name;
    private String filePath;
    private int id;
    private int length;
    private long time;

    public RecordingItem() {
    }

    public RecordingItem(Parcel source){
        id = source.readInt();
        name = source.readString();
        filePath = source.readString();
        length = source.readInt();
        time = source.readLong();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static final Parcelable.Creator<RecordingItem> CREATOR = new Creator<RecordingItem>() {
        @Override
        public RecordingItem createFromParcel(Parcel source) {
            return new RecordingItem(source);
        }

        @Override
        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(length);
        dest.writeLong(time);
        dest.writeString(filePath);
        dest.writeString(name);
    }
}
