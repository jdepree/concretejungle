package org.concretejungle.view;

import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.maps.model.LatLng;

import org.concretejungle.R;
import org.concretejungle.util.MapUtils;

import java.util.ArrayList;

public class PlaceSelectorView extends AutoCompleteTextView {
    private LocationSelectedListener mListener;
    private LatLng mCenter;
    private int mRadius;

    public PlaceSelectorView(Context context) {
        super(context);
        init();
    }

    public PlaceSelectorView(Context context, AttributeSet set) {
        super(context, set);
        init();
    }

    protected void init() {
        setAdapter(new PlacesAutoCompleteAdapter(getContext(), R.layout.list_item_location_result));
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MapUtils.LocationWithId loc = (MapUtils.LocationWithId) adapterView.getItemAtPosition(position);
                new DetailsTask().execute(loc.getId(), loc.getDisplayName());
            }
        });
    }

    public void setCenter(LatLng center, int radius) {
        mCenter = center;
        mRadius = radius;
    }

    public void setOnLocationSelectedListener(LocationSelectedListener listener) {
        mListener = listener;
    }

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<MapUtils.LocationWithId> implements Filterable {
        private ArrayList<MapUtils.LocationWithId> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public MapUtils.LocationWithId getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new Filter.FilterResults();
                    if (constraint != null) {
                        resultList = MapUtils.autocomplete(getContext(), constraint.toString(), mCenter, mRadius);
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
    }

    public interface LocationSelectedListener {
        public void onLocationSelected(LatLng location);
    }


    private class DetailsTask extends AsyncTask<String, Void, LatLng> {
        @Override
        public LatLng doInBackground(String... locationInfo) {
            String placeId = locationInfo[0];
            String placeName = locationInfo[1];

            return MapUtils.getPlaceDetails(getContext(), placeId, placeName);
        }

        @Override
        public void onPostExecute(LatLng loc) {
            mListener.onLocationSelected(loc);
        }
    }
}