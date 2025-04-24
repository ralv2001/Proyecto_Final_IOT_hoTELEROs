package com.example.proyecto_final_hoteleros.client.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomType implements Parcelable {
    private String name;
    private int size;
    private String price;
    private int imageResource;

    public RoomType(String name, int size, String price, int imageResource) {
        this.name = name;
        this.size = size;
        this.price = price;
        this.imageResource = imageResource;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    // Alias para getSize (si se usa en alguna parte como getArea)
    public int getArea() {
        return size;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResource() {
        return imageResource;
    }

    // Alias para getImageResource (si se usa en alguna parte como getImageResId)
    public int getImageResId() {
        return imageResource;
    }

    // Implementación de Parcelable
    protected RoomType(Parcel in) {
        name = in.readString();
        size = in.readInt();
        price = in.readString();
        imageResource = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(size);
        dest.writeString(price);
        dest.writeInt(imageResource);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RoomType> CREATOR = new Creator<RoomType>() {
        @Override
        public RoomType createFromParcel(Parcel in) {
            return new RoomType(in);
        }

        @Override
        public RoomType[] newArray(int size) {
            return new RoomType[size];
        }
    };
}