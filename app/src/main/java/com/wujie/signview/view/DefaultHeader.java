package com.wujie.signview.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wujie.signview.R;

/**
 * Created by Troy on 2017-10-13.
 */

public class DefaultHeader extends BaseHeader {
    private Context mContext;
    private int rotationSrc;
    private int arrowSrc;

    private long freshTime;
    private final int ROTATE_ANIM_DURATION = 180;
    private RotateAnimation mRotateUpAnim;
    private RotateAnimation mRotateDownAnim;

    private TextView mTitle;
    private ProgressBar mProgressBar;
    private ImageView mArrow;

    public DefaultHeader(Context context) {
        this(context, R.drawable.common_progress, R.drawable.common_progress);

    }

    public DefaultHeader(Context context, int rotationSrc, int arrowSrc) {
        this.mContext = context;
        this.rotationSrc = rotationSrc;
        this.arrowSrc = arrowSrc;
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup viewGroup) {
        View convertView = inflater.inflate(R.layout.springview_default_header, viewGroup, true);
        mTitle = (TextView) convertView.findViewById(R.id.default_header_title);
        mProgressBar = (ProgressBar) convertView.findViewById(R.id.default_header_progressbar);
        mProgressBar.setIndeterminateDrawable(ContextCompat.getDrawable(mContext,rotationSrc));
        mArrow = (ImageView)convertView.findViewById(R.id.default_header_arrow);
        mArrow.setImageResource(arrowSrc);
        return convertView;
    }

    @Override
    public void onPreDrag(View rootView) {

    }

    @Override
    public void onDropAnim(View rootView, int dy) {

    }

    @Override
    public void onLimitDes(View rootView, boolean upORdown) {
        if (upORdown) {
            mTitle.setText("下拉刷新");
            if(mArrow.getVisibility() == View.VISIBLE) {
                mArrow.startAnimation(mRotateDownAnim);
            }
        } else {
            mTitle.setText("释放立即刷新");
            if(mArrow.getVisibility() == View.VISIBLE) {
                mArrow.startAnimation(mRotateUpAnim);
            }
        }
    }

    @Override
    public void onStartAnim() {
        freshTime = System.currentTimeMillis();
        mTitle.setText("正在刷新");
        mArrow.setVisibility(View.INVISIBLE);
        mArrow.clearAnimation();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishAnim() {
        mArrow.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }
}
