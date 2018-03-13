package hu.daniel.vince.humanmobility.view.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Ferenc Lakos.
 * Date: 2015. 11. 01.
 */
public class Utils {

    private Utils(){}

    public static int dpToPx(Context context, int dp) {
        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        context.getResources().getDisplayMetrics()));
    }

}
