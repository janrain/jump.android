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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import com.janrain.android.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @internal
 */
public class ColorButton extends Button {
    public static final String TAG = ColorButton.class.getSimpleName();
    public static boolean sEnabled = true;
    private int mColor;

    public ColorButton(Context context) {
        super(context);
    }

    public ColorButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        processAttributes(context, attributeSet);
    }

    public ColorButton(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        processAttributes(context, attributeSet);
    }

    private void processAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ColorButton);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.ColorButton_jr_color) {
                mColor = a.getColor(attr, 0);
                colorify();
            }
        }
        a.recycle();
    }

    public void setColor(int color) {
        mColor = color;
        if (getBackground().getCurrent() instanceof NinePatchDrawable) {
            colorify();
            invalidate();
            return;
        }
        // I tried all this fancy state manipulating code, but it didn't work and I don't know why.
        //    setColor(color, new int[]{android.R.attr.state_enabled});
        //    setColor(color, new int[]{});
        //    setColor(color, new int[]{-android.R.attr.state_window_focused, android.R.attr.state_enabled});
        //    setColor(color, new int[]{-android.R.attr.state_window_focused, -android.R.attr.state_enabled});
        //}
        //
        //public void setColor(int color, int[] stateSet) {
        StateListDrawable sld = (StateListDrawable) getBackground();
        //2910 enabled
        //2908 focused
        //2919 pressed
        //2909 window focused

        //int[] oldStateSet = sld.getState();
        //sld.setState(stateSet);
        ((ColorFilterRejectingDrawableWrapper) sld.getCurrent())
                ._setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        //sld.getCurrent().invalidateSelf();
        //sld.setState(oldStateSet);
        //sld.getCurrent().invalidateSelf();
        invalidate();
    }

    private void colorify() {
        if (!sEnabled) return;

        StateListDrawable b = (StateListDrawable) getBackground();
        //b.mutate(); bugged for <= 2.1 :( android issue # 17184
        StateListDrawable b_ = new StateListDrawable();

        Method getStateSet, getStateCount, getStateDrawable;
        try {
            getStateSet = b.getClass().getDeclaredMethod("getStateSet", int.class);
            getStateCount = b.getClass().getDeclaredMethod("getStateCount");
            getStateDrawable = b.getClass().getDeclaredMethod("getStateDrawable", int.class);
        } catch (NoSuchMethodException e) { Log.e(TAG, e.toString()); return; }

        try {
            int stateCount = (Integer) getStateCount.invoke(b);
            for (int i=0; i < stateCount; i++) {
                int[] ss = (int[]) getStateSet.invoke(b, i);
                NinePatchDrawable d = (NinePatchDrawable) getStateDrawable.invoke(b, i);
                b_.addState(ss, new ColorFilterRejectingDrawableWrapper(d));

                // For some weird reason Arrays.asList(ss) -> List<int[]> not List<Integer> :(
                // Workaround:
                ArrayList<Integer> ssl = new ArrayList<Integer>();
                for (int j : ss) ssl.add(j);

                if (!ssl.contains(android.R.attr.state_pressed)
                        && !ssl.contains(android.R.attr.state_focused))
                    d.setColorFilter(mColor, PorterDuff.Mode.MULTIPLY);
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.toString()); return;
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.toString()); return;
        }

        setBackgroundDrawable(b_);
    }

    @Override
    public void setVisibility(int visibility) {
//        JREngage.logd(TAG, "[ColorButton][" + getText() + "][" + visibility + "]", new Exception());
        super.setVisibility(visibility);
    }

    private static class ColorFilterRejectingDrawableWrapper extends Drawable {
        private Drawable mDrawable;
        ColorFilterRejectingDrawableWrapper(Drawable d) {
            mDrawable = d;
        }

        private void _setColorFilter(int color, PorterDuff.Mode mode) {
            mDrawable.setColorFilter(color, mode);
        }
        @Override
        public void draw(Canvas canvas) {
            mDrawable.draw(canvas);
        }

        @Override
        public void setAlpha(int alpha) {
            mDrawable.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            //reject
        }

        @Override
        public int getOpacity() {
            return mDrawable.getOpacity();
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            mDrawable.setBounds(left, top, right, bottom);
        }

        @Override
        public void setBounds(Rect bounds) {
            mDrawable.setBounds(bounds);
        }

        @Override
        public void setChangingConfigurations(int configs) {
            mDrawable.setChangingConfigurations(configs);
        }

        @Override
        public int getChangingConfigurations() {
            return mDrawable.getChangingConfigurations();
        }

        @Override
        public void setDither(boolean dither) {
            mDrawable.setDither(dither);
        }

        @Override
        public void setFilterBitmap(boolean filter) {
            mDrawable.setFilterBitmap(filter);
        }

        @Override
        public void invalidateSelf() {
            mDrawable.invalidateSelf();
        }

        @Override
        public void scheduleSelf(Runnable what, long when) {
            mDrawable.scheduleSelf(what, when);
        }

        @Override
        public void unscheduleSelf(Runnable what) {
            mDrawable.unscheduleSelf(what);
        }

        @Override
        public void setColorFilter(int color, PorterDuff.Mode mode) {
            //mDrawable.setColorFilter(color, mode);
        }

        @Override
        public void clearColorFilter() {
            //mDrawable.clearColorFilter();
        }

        @Override
        public boolean isStateful() {
            return mDrawable.isStateful();
        }

        @Override
        public boolean setState(int[] stateSet) {
            return mDrawable.setState(stateSet);
        }

        @Override
        public int[] getState() {
            return mDrawable.getState();
        }

        @Override
        public Drawable getCurrent() {
            return mDrawable.getCurrent();
        }

        @Override
        public boolean setVisible(boolean visible, boolean restart) {
            return mDrawable.setVisible(visible, restart);
        }

        @Override
        public Region getTransparentRegion() {
            return mDrawable.getTransparentRegion();
        }

        //@Override
        //protected boolean onStateChange(int[] state) {
        //    return mDrawable.onStateChange(state);
        //}
        //
        //@Override
        //protected boolean onLevelChange(int level) {
        //    return mDrawable.onLevelChange(level);
        //}
        //
        //@Override
        //protected void onBoundsChange(Rect bounds) {
        //    mDrawable.onBoundsChange(bounds);
        //}

        @Override
        public int getIntrinsicWidth() {
            return mDrawable.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mDrawable.getIntrinsicHeight();
        }

        @Override
        public int getMinimumWidth() {
            return mDrawable.getMinimumWidth();
        }

        @Override
        public int getMinimumHeight() {
            return mDrawable.getMinimumHeight();
        }

        @Override
        public boolean getPadding(Rect padding) {
            return mDrawable.getPadding(padding);
        }

        @Override
        public ColorFilterRejectingDrawableWrapper mutate() {
            mDrawable.mutate();
            return this;
        }

        @Override
        public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
                throws XmlPullParserException, IOException {
            throw new RuntimeException();
            //mDrawable.inflate(r, parser, attrs);
        }

        @Override
        public ConstantState getConstantState() {
            return mDrawable.getConstantState();
        }
    }
}
