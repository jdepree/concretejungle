package org.concretejungle.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import org.concretejungle.R;
import org.concretejungle.model.TreeType;
import org.concretejungle.model.data.TreeStore;

import java.util.List;

public class TypeSelectFragment extends ListFragment {
    private List<TreeType> mTypes;
    private ArrayAdapter mAdapter;
    private MenuItem mShowAllMenuItem;

    private boolean mAllSelected = true;

    public static Fragment newInstance() {
        return new TypeSelectFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_type_select, menu);
        mShowAllMenuItem = menu.findItem(R.id.fragment_filter_show_all);
        if (mAllSelected) {
            mShowAllMenuItem.setTitle(R.string.filter_hide_all);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_filter_show_all:
                if (mAllSelected) {
                    mAllSelected = false;
                    mShowAllMenuItem.setTitle(R.string.filter_show_all);
                    for (TreeType type : mTypes) {
                        type.setDisplayOnMap(false);
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAllSelected = true;
                    mShowAllMenuItem.setTitle(R.string.filter_hide_all);
                    for (TreeType type : mTypes) {
                        type.setDisplayOnMap(true);
                    }
                    mAdapter.notifyDataSetChanged();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        mTypes = TreeStore.getInstance().getTreeTypes();
        mAdapter = new ArrayAdapter<TreeType>(getActivity(),
                android.R.layout.simple_list_item_checked,
                mTypes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                boolean isChecked = mTypes.get(position).isDisplayOnMap();

                CheckedTextView nextView = (CheckedTextView)view.findViewById(android.R.id.text1);
                nextView.setChecked(isChecked);

                if (!isChecked) {
                    mAllSelected = false;
                }
                return view;
            }
        };
        setListAdapter(mAdapter);

        return view;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        CheckedTextView checkedView = (CheckedTextView)view.findViewById(android.R.id.text1);
        boolean currentState = checkedView.isChecked();
        checkedView.setChecked(!currentState);

        TreeType type = mTypes.get(position);
        type.setDisplayOnMap(!currentState);

        if (currentState) {
            mShowAllMenuItem.setTitle(R.string.filter_show_all);
            mAllSelected = false;
        }
    }
}
