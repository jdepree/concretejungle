package org.concretejungle.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Date;

public class Tree implements ClusterItem {
    private int mId;
    private int mTypeId;
    private float mLatitude;
    private float mLongitude;
    private String mNotes;
    private Date mDateEntered;
    private int mUserId;
    private boolean mPrivate;
    private String mHash;

    public Tree(int id, int typeId, float latitude, float longitude,
                String notes, Date dateEntered, int userId, boolean isPrivate, String hash) {
        mId = id;
        mTypeId = typeId;
        mLatitude = latitude;
        mLongitude = longitude;
        mNotes = notes;
        mDateEntered = dateEntered;
        mUserId = userId;
        mPrivate = isPrivate;
        mHash = hash;
    }

    public int getId() {
        return mId;
    }

    public int getTypeId() {
        return mTypeId;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public float getLongitude() {
        return mLongitude;
    }

    public String getNotes() {
        return mNotes;
    }

    public Date getDateEntered() {
        return mDateEntered;
    }

    public int getUserId() {
        return mUserId;
    }

    public boolean isPrivate() {
        return mPrivate;
    }

    public String getHash() {
        return mHash;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(mLatitude, mLongitude);
    }
}
