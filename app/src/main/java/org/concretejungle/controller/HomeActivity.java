package org.concretejungle.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import org.concretejungle.SingleFragmentActivity;

public class HomeActivity extends SingleFragmentActivity {
    public static final String PREF_STORE_NAME = "ConcreteJungle";

    public static Intent newIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    public Fragment getFragment() {
        return MapFragment.newInstance();
    }
}
