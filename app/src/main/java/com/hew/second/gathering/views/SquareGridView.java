package com.hew.second.gathering.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

public class SquareGridView extends CardView {
    boolean expanded = false;

    public SquareGridView(Context context) {
        super(context);
    }

    public SquareGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        // widthとheightに同じ値を指定することで正方形！
        setMeasuredDimension(width, width);

    }
}
