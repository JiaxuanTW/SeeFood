package com.nukcsie.nothotdog.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class RecognitionItem implements Parcelable, Comparable<RecognitionItem> {
    public String label;
    public int score;

    public RecognitionItem(String label, int score) {
        this.label = label;
        this.score = score;
    }

    protected RecognitionItem(@NotNull Parcel in) {
        label = in.readString();
        score = in.readInt();
    }

    public double getConfidence() {
        return Math.round(score * 10000d / 256d) / 100d;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeInt(score);
    }

    public static final Creator<RecognitionItem> CREATOR = new Creator<RecognitionItem>() {
        @NotNull
        @Contract("_ -> new")
        @Override
        public RecognitionItem createFromParcel(Parcel in) {
            return new RecognitionItem(in);
        }

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public RecognitionItem[] newArray(int size) {
            return new RecognitionItem[size];
        }
    };

    @Override
    public int compareTo(@NotNull RecognitionItem item) {
        return score - item.score;
    }
}