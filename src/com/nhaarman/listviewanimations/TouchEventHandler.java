package com.nhaarman.listviewanimations;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

public interface TouchEventHandler {

    boolean onTouchEvent(@NonNull MotionEvent event);

    boolean isInteracting();

}
