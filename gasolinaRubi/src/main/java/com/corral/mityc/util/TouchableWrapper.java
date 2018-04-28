package com.corral.mityc.util;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.corral.mityc.MitycRubi;

public class TouchableWrapper extends FrameLayout {

    public TouchableWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                MitycRubi.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                break;

            case MotionEvent.ACTION_UP:
                MitycRubi.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}