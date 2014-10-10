package org.concretejungle.controller;


import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import org.concretejungle.SingleFragmentActivity;

public class LoginActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    public Fragment getFragment() {
        return LoginFragment.newInstance();
    }
}
