/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.janrain.android.engage.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.janrain.android.utils.AndroidUtils;

/**
 * @internal
 *
 * Two modes of operation:
 *  - default: 1/2 available screen area by using 71% of each dimension
 *  - set a target height/width dimension in DIP
 */
public class CustomMeasuringFrameLayout extends FrameLayout {
    private int mTargetHeight;
    private int mTargetWidth;
    private Integer mTargetHeightDip;
    private Integer mTargetWidthDip;
    private Context mContext;

    public CustomMeasuringFrameLayout(Context context) {
        super(context);

        init(context);
    }

    public CustomMeasuringFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public CustomMeasuringFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context c) {
        mContext = c;
        computeTargetSize();
    }

    public void setTargetSizeDip(Integer wDip, Integer hDip) {
        mTargetHeightDip = hDip;
        mTargetHeight = AndroidUtils.scaleDipToPixels(hDip);
        
        mTargetWidthDip = wDip;
        mTargetWidth = AndroidUtils.scaleDipToPixels(wDip);

        // Tried this to keep the window from growing larger than the desired size, but it didn't work,
        // instead, it resulted in a FrameLayout of the right size in a too-big window.
//        setLayoutParams(new LayoutParams(mTargetWidth, mTargetHeight));
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        computeTargetSize();
    }

    private void computeTargetSize() {
        if (mTargetHeightDip == null) {
            mTargetHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.71);
        }

        if (mTargetWidthDip == null) {
            mTargetWidth = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.71);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wM = MeasureSpec.getMode(widthMeasureSpec);
        int hM = MeasureSpec.getMode(heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        switch (hM) {
            case MeasureSpec.AT_MOST:
                h = Math.min(mTargetHeight, h);
                hM = MeasureSpec.EXACTLY;
                break;
            case MeasureSpec.EXACTLY:
//                h = mTargetHeight;
                break;
            case MeasureSpec.UNSPECIFIED:
                h = mTargetHeight;
                hM = MeasureSpec.EXACTLY;
                break;
        }

        switch (wM) {
            case MeasureSpec.AT_MOST:
                w = Math.min(mTargetWidth, h);
                wM = MeasureSpec.EXACTLY;
                break;
            case MeasureSpec.EXACTLY:
//                w = mTargetWidth;
                break;
            case MeasureSpec.UNSPECIFIED:
                w = mTargetWidth;
                wM = MeasureSpec.EXACTLY;
                break;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, hM);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(w, wM);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        Log.d("CMFL", "wM:" + wM + " w:" + w + " hM:" + hM + " h:" + h + " mw:" + getMeasuredWidth() +
//                " mh:" + getMeasuredHeight());
    }
}
