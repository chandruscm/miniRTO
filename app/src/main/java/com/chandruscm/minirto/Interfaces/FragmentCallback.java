package com.chandruscm.minirto.Interfaces;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

public interface FragmentCallback
{
    public void showFab();
    public void hideFab();
    public void showSnackBar(@StringRes int message, @StringRes int button, int pos,int action);
}

