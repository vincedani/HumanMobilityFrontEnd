package hu.daniel.vince.humanmobility.view.activity;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.view.drawable.ProgressDrawable;
import hu.daniel.vince.humanmobility.view.utils.Utils;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 02. 21.
 */

public class MaterialProgressBar extends ProgressBar {

        public MaterialProgressBar(Context context) {
            this(context, null);
        }

        public MaterialProgressBar(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MaterialProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context);
        }

        private void init(Context context) {
            setIndeterminate(true);
            setIndeterminateDrawable(
                    new ProgressDrawable(
                            ContextCompat.getColor(context, R.color.color_accent),
                            Utils.dpToPx(context, 3)));
        }
}
