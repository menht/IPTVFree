package it.michelelacorte.iptvfree.sd_reader;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class is an custom linear layout class for text view.
 *
 * Created by Salvatore on 30/04/2016
 */
public class TextViewWithImage extends LinearLayout {

	private ImageView mImage;
	private TextView mText;

    /**
     * Public constructor for custom view
     * @param context Context
     */
	public TextViewWithImage(Context context) {
		super(context);
		setOrientation(HORIZONTAL);
		mImage = new ImageView(context);
		mText = new TextView(context);

		LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
		lp.weight = 1;
		addView(mImage, lp);
		lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 3);
		addView(mText, lp);
	}

    /**
     * Get text from TextView
     * @return CharSequence
     */
	public CharSequence getText() {
		return mText.getText();
	}

    /**
     * Set image on TextView
     * @param resId int
     */
	public void setImageResource(int resId) {
		if (resId == -1) {
			mImage.setVisibility(View.GONE);
			return;
		}
		mImage.setImageResource(resId);
	}

    /**
     * Set text on TextView
     * @param aText String
     */
	public void setText(String aText) {
		mText.setText(aText);
	}

}
