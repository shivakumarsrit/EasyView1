package io.evercam.androidapp.custom;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PortCheckEditText extends EditText
{
    public PortCheckEditText(Context context)
    {
        super(context);
    }

    public PortCheckEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PortCheckEditText(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Clear the status text view when the text in EditText gets changed for the first time
     *
     * @param textViews The status text view(s) list to clear after text changes
     */
    public void hideStatusViewsOnTextChange(final TextView... textViews)
    {
        addTextChangedListener(new TextWatcher()
        {

            boolean isFirstTimeChange = true;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                if(isFirstTimeChange)
                {
                    for(TextView textView : textViews)
                    {
                        hideView(textView);
                        isFirstTimeChange = false;
                    }
                }
            }
        });
    }

    private void hideView(TextView textView)
    {
        textView.setVisibility(View.GONE);
    }
}
