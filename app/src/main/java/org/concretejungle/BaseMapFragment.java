package org.concretejungle;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

public abstract class BaseMapFragment extends Fragment {

    protected void setupMapView(Bundle savedInstanceState) {
        getMapView().onCreate(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getMapView() != null) {
            getMapView().onResume();
        }
    }

    @Override
    public void onPause() {
        if(getMapView() != null) {
            getMapView().onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(getMapView() != null) {
            getMapView().onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(getMapView() != null) {
            getMapView().onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MapView mapView = getMapView();
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }

    }

    protected abstract MapView getMapView();
}
