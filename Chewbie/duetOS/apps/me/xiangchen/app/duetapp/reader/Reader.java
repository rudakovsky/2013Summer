package me.xiangchen.app.duetapp.reader;

import java.util.Calendar;
import java.util.Hashtable;

import me.xiangchen.app.duetapp.App;
import me.xiangchen.app.duetos.R;
import me.xiangchen.technique.flipsense.xacFlipSenseFeatureMaker;
import me.xiangchen.technique.handsense.xacHandSenseFeatureMaker;
import me.xiangchen.technique.touchsense.xacTouchSenseFeatureMaker;
import me.xiangchen.ui.xacBufferCanvas;
import me.xiangchen.ui.xacInteractiveCanvas;
import me.xiangchen.ui.xacSketchCanvas;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Reader extends App {

	public final static String LOGTAG = "DuetOS";
	public final static int HAND = 0;
	public final static int PEN = 1;
	public final static int HIGHLIGHTER = 2;
	public final static int TEXTSELECTION = 3;

	public final static int APPWIDTH = 1080;
	
	public final static float CURSORWIDTH = 15;
	public final static float CURSORMARGIN = 20;
	
	public final static int SHIFTHORI = 0;
	public final static int SHIFTVERT = 1;

	TextView textView;
	String text;
	ScrollView scrollView;
	xacSketchCanvas canvas;

	int handedness;
	int handPart;
	int isFlipped;

	boolean textScrollable = false;

	Hashtable<Integer, Integer> htHandPart;

	long timeTouchDown;

	float xPrev;
	float yPrev;

	xacBufferCanvas bufCan;

	RelativeLayout scrollLayout;

	int dScrollX = 0;
	int dScrollY = 0;

	xacInteractiveCanvas menu;
	float alphaMenu = 0.0f;

	String selectedText = "";
	int firstLine;
	int firstOffset;
	int prevLine;
	int prevOffset;

	public Reader(Context context) {
		super(context);
		color = xacInteractiveCanvas.fgColorBlue;
		// appView = new xacInteractiveCanvas(context);
		// appView.setBackgroundColor(color);

		appLayout = new RelativeLayout(context);
		scrollLayout = new RelativeLayout(context);

		// scroll view
		scrollView = new ScrollView(context);
		scrollView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				doTouch(event);
				return true;
			}
		});

		// text view
		textView = new TextView(context);
		textView.setTextSize(20);
		textView.setBackgroundColor(Color.WHITE);
		text = context.getString(R.string.a_tale_of_two_cities);
		textView.setText(text);
		scrollLayout.addView(textView);

		// sketch canvs
		canvas = new xacSketchCanvas(context);
		appLayout.addView(canvas);

		// buffer canvas
		bufCan = new xacBufferCanvas(context);
		Paint rectPaint = new Paint();
		rectPaint.setColor(0x880000FF);
		bufCan.setRectPaint(rectPaint);
		RelativeLayout.LayoutParams paramsBufCan = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		scrollLayout.addView(bufCan, paramsBufCan);
		canvas.setClientCanvas(bufCan);

		scrollView.addView(scrollLayout);
		appLayout.addView(scrollView);

		// menu
		menu = new xacInteractiveCanvas(context);
		GradientDrawable gradientDrawable = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
						0x00000000, 0x33000000, 0xAA000000, 0xEE000000,
						0xEE000000 });
		gradientDrawable.setCornerRadius(0f);
		menu.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				appLayout.removeView(menu);
				return false;
			}
		});
		menu.setBackgroundDrawable(gradientDrawable);

		// sensing initialization
		xacHandSenseFeatureMaker.setLabel(xacHandSenseFeatureMaker.UNKNOWN);
		xacHandSenseFeatureMaker.createFeatureTable();

		xacTouchSenseFeatureMaker.setLabel(xacTouchSenseFeatureMaker.UNKNOWN);
		xacTouchSenseFeatureMaker.createFeatureTable();

		xacFlipSenseFeatureMaker.setLabel(xacFlipSenseFeatureMaker.UNKNOWN);
		xacFlipSenseFeatureMaker.createFeatureTable();
	}

	@Override
	public void runOnUIThread() {

	}

	@SuppressLint("NewApi")
	@Override
	public void doTouch(MotionEvent event) {
		int action = event.getAction();

		// potential feature: detect which hand wears the watch

		Calendar calendar = Calendar.getInstance();
		long curTime = calendar.getTimeInMillis();

		PointerCoords coords = new PointerCoords();
		event.getPointerCoords(0, coords);
		float xCur = coords.x;
		float yCur = coords.y;

		canvas.setScrollOffsets(0, 0);
		switch (action) {
		case MotionEvent.ACTION_DOWN:

			timeTouchDown = curTime;
			appLayout.removeView(menu);
			if (selectedText.length() > 0) {
				unSelectText(textView, text);
				selectedText = "";
			}

			// is there a flip?
			isFlipped = xacFlipSenseFeatureMaker.calculateFlipGesture();
			switch (isFlipped) {
			case xacFlipSenseFeatureMaker.FLIP:
				// show menu
				appLayout.addView(menu);
				// alphaMenu = 0.0f;
				// menu.setAlpha(alphaMenu);
				break;
			case xacFlipSenseFeatureMaker.NOFLIP:
				// register hand part
				handPart = xacTouchSenseFeatureMaker
						.calculateHandPart(new double[] { event.getSize(0) });
				switch (handPart) {
				case xacTouchSenseFeatureMaker.PAD:
					handedness = xacHandSenseFeatureMaker.UNKNOWN;

					break;
				case xacTouchSenseFeatureMaker.SIDE:
					break;
				case xacTouchSenseFeatureMaker.KNUCKLE:
					firstLine = -1;
					prevLine = -1;
					bufCan.clearRects();
					break;
				}
				break;
			}
			break;
		case MotionEvent.ACTION_MOVE:

			if (isFlipped == xacFlipSenseFeatureMaker.FLIP) {
				break;
			}

			switch (handPart) {
			case xacTouchSenseFeatureMaker.PAD:
				canvas.doTouch(event);
				break;
			case xacTouchSenseFeatureMaker.SIDE:
				float speedRatio = 0.75f;
				int dx = 0;
				int dy = (int) ((yPrev - yCur) * speedRatio);
				dScrollX += dx;

				if (dScrollY + dy < 0) {
					dy *= 0.01f;
				}
				dScrollY += dy;

				scrollView.scrollBy(dx, dy);
				canvas.setScrollOffsets(dx, dy);

				dScrollY = Math.max(0, dScrollY);

				break;
			case xacTouchSenseFeatureMaker.KNUCKLE:
				Layout layout = textView.getLayout();
				if (layout != null) {
					prevLine = layout
							.getLineForVertical((int) (yCur + dScrollY)) - SHIFTVERT;
					prevOffset = layout.getOffsetForHorizontal(prevLine, xCur) - SHIFTHORI;

					if (firstLine < 0) {
						firstLine = prevLine;
						firstOffset = prevOffset;
					}
					
					float l = xCur - CURSORWIDTH / 2;
					float t = prevLine * textView.getLineHeight() - CURSORMARGIN;// + dScrollY;
					float r = xCur + CURSORWIDTH / 2;
					float b = t + textView.getLineHeight() + CURSORMARGIN;
					updateCursor(l, t, r, b);
				}
				break;
			}
			break;
		case MotionEvent.ACTION_UP:

			if (isFlipped == xacFlipSenseFeatureMaker.FLIP) {
				break;
			}

			switch (handPart) {
			case xacTouchSenseFeatureMaker.PAD:
				canvas.doTouch(event);

				break;
			case xacTouchSenseFeatureMaker.SIDE:
				unSelectText(textView, text);
				break;
			case xacTouchSenseFeatureMaker.KNUCKLE:
				int start = Math.min(firstOffset, prevOffset);
				int end = Math.max(firstOffset, prevOffset);
				selectedText = text.substring(start, end);
				selectText(textView, start, end);

				updateCursor(0, 0, 0, 0);
				break;
			}

			break;
		}

		xPrev = xCur;
		yPrev = yCur;
	}

	private void updateCursor(float l, float t, float r, float b) {
		bufCan.clearRects();
		RectF rectf = new RectF(l, t, r, b);
		bufCan.addRect(rectf);
		bufCan.invalidate();
	}
	
	@Override
	public void doAccelerometer(float values[]) {
		xacHandSenseFeatureMaker.updatePhoneAccel(values);
		xacHandSenseFeatureMaker.addPhoneFeatureEntry();

		xacTouchSenseFeatureMaker.updatePhoneAccel(values);
		xacTouchSenseFeatureMaker.addPhoneFeatureEntry();

		xacFlipSenseFeatureMaker.updatePhoneAccel(values);
		xacFlipSenseFeatureMaker.addPhoneFeatureEntry();
	}

	private void selectText(TextView tv, int start, int end) {
		Spannable textSpannable = new SpannableStringBuilder(tv.getText());
		textSpannable.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 0,
				start - 1, 0);
		textSpannable.setSpan(new BackgroundColorSpan(0x881ABCBD), start, end,
				0);
		textSpannable.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
				end + 1, text.length() - 1, 0);
		tv.setText(textSpannable);
	}

	private void unSelectText(TextView tv, String txt) {
		tv.setText(txt);
	}

}
