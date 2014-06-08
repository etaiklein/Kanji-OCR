package edu.dartmouth.cs.drawing;

/**
 * Drawing View
 * 
 * A class to view a screen you can draw on
 * 
 * Etai Klein
 * 6/7/14
 * 
 */

import java.util.ArrayList;

import android.view.View;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

//drawing
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.MotionEvent;

//function
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;


public class DrawingView extends View {

	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint, backPaint;
	//initial color
	private int paintColor = 0xFF000000;
	
	//canvas
	private Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;
	//current Background
	private String background = "一";
	//brush
	private float brushSize, lastBrushSize;
	//strokes
	ArrayList<Integer> strokes = new ArrayList<Integer>();
	
	//eraser
	private boolean erase=false;

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupDrawing();
	}

	private void setupDrawing() {
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
		drawPath = new Path();
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setFilterBitmap(true);
		drawPaint.setDither(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		
		backPaint = new Paint();
		backPaint.setColor(Color.GRAY);
		backPaint.setTextSize(70);
		backPaint.setAntiAlias(true);
		backPaint.setStrokeWidth(getResources().getInteger(R.integer.very_small_size));
		backPaint.setStyle(Paint.Style.STROKE);
		backPaint.setStrokeJoin(Paint.Join.ROUND);
		backPaint.setStrokeCap(Paint.Cap.ROUND);
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
	}

	protected void setBackgroundImage(String kanji){
		background = kanji;
		System.out.println("background changed to " + background);
		startNew();
		}
	
	@Override
	//view size
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		System.out.println("called");
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				
		//enable svg drawing
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		//set the background to be white
		setDrawingCacheBackgroundColor(Color.WHITE);
		
		//set the picture
		drawText();
	} 
	
	public void drawText(){
		drawCanvas = new Canvas(canvasBitmap);
		drawCanvas.drawText(background, drawCanvas.getWidth()/2.15f, drawCanvas.getHeight()/10f, backPaint);
        drawCanvas.drawLine(0, drawCanvas.getHeight() - drawCanvas.getWidth(), drawCanvas.getWidth(), drawCanvas.getHeight() - drawCanvas.getWidth(), backPaint);

	}
	
	public Bitmap getBitmap(){
		return canvasBitmap;
	}
	
	public void setBitmap(Bitmap bm){
		canvasBitmap = bm;
		drawCanvas.drawBitmap(canvasBitmap, 0, 0, drawPaint);
	}
	
	public Bitmap drawTextToBitmap(Context mContext,  int resourceId,  String mText) {
	    try {
	         Resources resources = mContext.getResources();
	            float scale = resources.getDisplayMetrics().density;
	            Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);

	            android.graphics.Bitmap.Config bitmapConfig =   bitmap.getConfig();
	            // set default bitmap config if none
	            if(bitmapConfig == null) {
	              bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
	            }
	            // resource bitmaps are imutable,
	            // so we need to convert it to mutable one
	            bitmap = bitmap.copy(bitmapConfig, true);

	            Canvas canvas = new Canvas(bitmap);
	            // new antialised Paint
	            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	            // text color - #3D3D3D
	            paint.setColor(Color.rgb(110,110, 110));
	            // text size in pixels
	            paint.setTextSize((int) (12 * scale));
	            // text shadow
	            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

	            // draw text to the Canvas center
	            Rect bounds = new Rect();
	            paint.getTextBounds(mText, 0, mText.length(), bounds);
	            int x = (bitmap.getWidth() - bounds.width())/6;
	            int y = (bitmap.getHeight() + bounds.height())/5;

	          canvas.drawText(mText, x * scale, y * scale, paint);

	            return bitmap;
	    } catch (Exception e) {
	        // TODO: handle exception



	        return null;
	    }

	  }

	@Override
	//draw view
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}

	@Override
	//detect user touch     
	public boolean onTouchEvent(MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();

		switch (event.getAction()) {

		//track current point on finger press
		case MotionEvent.ACTION_DOWN:
			drawPath.moveTo(touchX, touchY);
			if (strokes == null){
				strokes = new ArrayList<Integer>();
			}
			strokes.add((int) touchX);
			strokes.add((int) touchY);
			break;

			//draw line to current point on finger move
		case MotionEvent.ACTION_MOVE:
			drawPath.lineTo(touchX, touchY);
			break;

			//set line on finger release
		case MotionEvent.ACTION_UP:
			drawCanvas.drawPath(drawPath, drawPaint);
			drawPath.reset();
			strokes.add((int) touchX);
			strokes.add((int) touchY);
			break;
		default:
			return false;
		}	
		//invalidates the view - ??
		invalidate();
		return true;
	}

	//set color     
	public void setColor(String newColor){
		invalidate();
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}

	//update size
	public void setBrushSize(float newSize){
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				newSize, getResources().getDisplayMetrics());
		brushSize=pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}

	public void setLastBrushSize(float lastSize){
		lastBrushSize=lastSize;
	}
	public float getLastBrushSize(){
		return lastBrushSize;
	}

	//set erase true or false       
	public void setErase(boolean isErase){
		erase=isErase;
		if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else drawPaint.setXfermode(null);
		drawText();
		
	}

	public void startNew(){
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
		drawText();
		strokes = new ArrayList<Integer>();
	}

	public String getStats() {
		return drawCanvas.getMaximumBitmapHeight() + " " + drawCanvas.getMaximumBitmapWidth() + " " + drawCanvas.getHeight() + " " + drawCanvas.getWidth() + " " + drawCanvas.getClipBounds();
	}
}
