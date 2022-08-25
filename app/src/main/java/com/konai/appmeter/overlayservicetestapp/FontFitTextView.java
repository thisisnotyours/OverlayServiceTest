package com.konai.appmeter.overlayservicetestapp;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class FontFitTextView extends TextView {

    public FontFitTextView(Context context) {
        super(context);
        initialize();
    }

    public FontFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());

        //max size defaults to the initially specified text size unless it is too small
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private void refitText(String text, int textWidth,int textHeight)
    {
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        int targetHeight = textHeight - this.getPaddingTop() - this.getPaddingBottom();
        float hi = Math.min(targetHeight, 300);
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        String caltext;
///////////
        if(mTextFixLen > 0) {

            caltext = mTextComp;

        }
        else
            caltext = text;
////////////////

        Rect bounds = new Rect();

        mTestPaint.set(this.getPaint());

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            mTestPaint.getTextBounds(caltext, 0, caltext.length(), bounds);
            if((mTestPaint.measureText(caltext)) >= targetWidth || (1+(2*(size+(float)bounds.top)-bounds.bottom)) >=targetHeight)
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float) (lo * mrate));

//        Log.d("refitText", "refitText:" + lo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth,height);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth(),this.getHeight());

//        Log.d("refitText", "refitText");
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {

        if (w != oldw) {
            refitText(this.getText().toString(), w,h);
        }
    }

    public void setSizeRate(double dvalue)
    {

        mrate = dvalue;

    }

    public void setTextLen(int nlen)
    {
        mTextFixLen = nlen;
        mTextComp = "";
        for(int i = 0; i < nlen; i++)
            mTextComp += "가";

    }

    public void setCalTextby(String stext)
    {

        mTextFixLen = stext.length();
        mTextComp = stext;
    }

    //Attributes
    private Paint mTestPaint;
    private double mrate = 0.8;
    private int mTextFixLen = 0;
    private String mTextComp = "";

}
