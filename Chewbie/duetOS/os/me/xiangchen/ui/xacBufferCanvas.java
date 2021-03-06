package me.xiangchen.ui;

import java.util.ArrayList;
import java.util.Hashtable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

public class xacBufferCanvas extends View {

	public static final String LOGTAG = "DuetOS";
	ArrayList<Path> paths;
	ArrayList<Path> recycledPaths;
	ArrayList<RectF> rectfs;
	Paint pathPaint;
	Paint rectPaint;
	Hashtable<Path, Paint> htPathPaint;

	public xacBufferCanvas(Context context) {
		super(context);
		paths = new ArrayList<Path>();
		recycledPaths = new ArrayList<Path>();
		rectfs = new ArrayList<RectF>();
		htPathPaint = new Hashtable<Path, Paint>();
	}

	public void setPathPaint(Paint p) {
		pathPaint = p;
	}

	public void setRectPaint(Paint p) {
		rectPaint = p;
	}

	public void addPath(Path path, Paint paint) {
		paths.add(path);
		Paint tmpPaint = new Paint(paint);
		htPathPaint.put(path, tmpPaint);
		
		recycledPaths.clear();
	}

	public void addRect(RectF rectf) {
		rectfs.add(rectf);
	}

	public void clearRects() {
		rectfs.clear();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// for (Path path : paths) {
		// canvas.drawPath(path, pathPaint);
		// }
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(width, height);
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		// canvas.drawColor(Color.GREEN);
		// Log.d(LOGTAG, "redrawing...");
		for (Path path : paths) {
			canvas.drawPath(path, htPathPaint.get(path));
		}

		if (rectfs != null) {
			for (RectF rectf : rectfs) {
				canvas.drawRect(rectf, rectPaint);
			}
		}
	}

	public void undo() {
		if(paths.size() > 0) {
			Path tmpPath = paths.get(paths.size() - 1);
			recycledPaths.add(tmpPath);
			paths.remove(tmpPath);
			this.invalidate();
		}
	}

	public void redo() {
		if(recycledPaths.size() > 0) {
			Path tmpPath = recycledPaths.get(recycledPaths.size() - 1);
			paths.add(tmpPath);
			recycledPaths.remove(tmpPath);
			this.invalidate();
		}
	}
}
