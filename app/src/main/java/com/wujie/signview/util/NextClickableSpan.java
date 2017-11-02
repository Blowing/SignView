package com.wujie.signview.util;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Troy on 2017-11-1.
 */

public class NextClickableSpan extends ClickableSpan {

    private Context mContext;

    public NextClickableSpan(Context context) {
        this.mContext = context;
    }
    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(Color.BLUE);
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
        Toast.makeText(mContext, "你点击了下一步", Toast.LENGTH_SHORT).show();
    }
}
