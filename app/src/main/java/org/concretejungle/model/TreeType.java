package org.concretejungle.model;

import java.util.Calendar;

public class TreeType implements Comparable {
    private static int sDayExecuted;

    private int mId;
    private String mName;
    private String mInfoUrl;
    private String mMarkerName;
    private String mImageName;
    private boolean mDisplayOnMap;
    private int mStartDay;
    private int mEndDay;

    public TreeType(int id, String name, String infoUrl, String markerName, int startDay, int endDay) {
        mId = id;
        mName = name;
        mInfoUrl = infoUrl;
        mMarkerName = markerName;
        mImageName = mName.toLowerCase().replace(' ', '_');
        mStartDay = startDay;
        mEndDay = endDay;
        mDisplayOnMap = isInSeason();
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mInfoUrl;
    }

    public String getMarkerName() {
        return mMarkerName;
    }

    public String getImageName() { return mImageName; }

    public boolean isDisplayOnMap() {
        return mDisplayOnMap;
    }

    public void setDisplayOnMap(boolean displayOnMap) {
        mDisplayOnMap = displayOnMap;
    }

    public boolean isInSeason() {
        if ((sDayExecuted >= mStartDay && sDayExecuted <= mEndDay) || (mEndDay < mStartDay && sDayExecuted <= mStartDay)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public int compareTo(Object o) {
        TreeType other = (TreeType)o;

        if (isInSeason()) {
            if (other.isInSeason()) {
                return mName.compareTo(other.mName);
            } else {
                return 1;
            }
        } else {
            if (other.isInSeason()) {
                return -1;
            } else {
                return mName.compareTo(other.mName);
            }
        }

    }

    public static void updateSeasonDate() {
        sDayExecuted = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    }
}
