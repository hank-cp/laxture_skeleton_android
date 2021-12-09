package com.laxture.skeleton.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.laxture.skeleton.R;
import com.laxture.skeleton.util.SkeletonUtil;

public class SimpleListItemView extends SaveStateLayout {

    TextView vTextView;

    public SimpleListItemView(Context context) {
        super(context);
        init();
    }

    public SimpleListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public SimpleListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(SkeletonUtil.getStyledContext(), R.layout.simple_list_item, this);
        vTextView = findViewById(R.id.text);
    }

    public void setText(String text) {
        this.vTextView.setText(text);
    }
}
