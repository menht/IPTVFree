package it.michelelacorte.iptvfree.util;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import it.michelelacorte.iptvfree.MainActivity;


/**
 * This class provide animation behavior for Floatin Action Button.
 *
 * Created by Michele on 28/04/2016.
 */
public class FabHideOnScroll extends FloatingActionButton.Behavior {

    /**
     * Public constructor (not used)
     * @param context Context
     * @param attrs AttributeSet
     */
    public FabHideOnScroll(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if (child.getVisibility() == View.VISIBLE && dyConsumed > 0) {
            if(MainActivity.isFabOpen)
            {
                //Restore FAB initial state
                MainActivity.fabMenu.startAnimation(MainActivity.rotate_backward);
                MainActivity.fabDownload.startAnimation(MainActivity.fab_close);
                MainActivity.fabAdd.startAnimation(MainActivity.fab_close);
                MainActivity.labelDownload.startAnimation(MainActivity.fab_close);
                MainActivity.labelAdd.startAnimation(MainActivity.fab_close);
                MainActivity.isFabOpen = false;
            }
            child.hide();
        } else if (child.getVisibility() == View.GONE && dyConsumed < 0) {
            child.show();
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}