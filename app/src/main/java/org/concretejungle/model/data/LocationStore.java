package org.concretejungle.model.data;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class LocationStore {
    private static LocationStore sInstance;

    private Location mLastLocation;

    public static LocationStore getInstance() {
        if (sInstance == null) {
            sInstance = new LocationStore();
        }
        return sInstance;
    }

    public void setLastLocation(Location lastLocation) {
        mLastLocation = lastLocation;
    }

    public Location getLastLocation() {
        return mLastLocation;
    }
}
