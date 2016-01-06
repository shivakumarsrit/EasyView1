package io.evercam.androidapp.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.evercam.androidapp.R;

/**
 * The view that inflates from explain_text_view.xml
 * This class provides interfaces for updating the title and message text
 */
public class ExplanationView extends LinearLayout
{
    private TextView mTitleTextView;
    private TextView mMessageTextView;

    public ExplanationView(Context context)
    {
        super(context);
        initSubviews();
    }

    public ExplanationView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initSubviews();
    }

    public ExplanationView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initSubviews();
    }

    private void initSubviews()
    {
        mTitleTextView = (TextView) findViewById(R.id.explain_text_title);
        mMessageTextView = (TextView) findViewById(R.id.explain_text_detail);
    }

    public void updateTitle(int titleId)
    {
        mTitleTextView.setText(titleId);
    }

    public void updateMessage(int messageId)
    {
        mMessageTextView.setText(messageId);
    }
}
