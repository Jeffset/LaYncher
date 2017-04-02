package by.jeffset.layncher.welcome;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import by.jeffset.layncher.R;

/**
 * View Pager Indicator
 */
public class PagerIndicator extends View {
   public static final String KEY_ACTIVE_VIEW = "PagerIndicator.activeView";
   public static final String KEY_SUPER_STATE = "PagerIndicator.superState";
   private int colorInactiveStroke;
   private int colorSelectedFill;
   private int viewCount;
   private float activeView;

   Paint inactiveCirclePaint;
   Paint selectedCirclePaint;

   public PagerIndicator(Context context) {
      super(context);
      init(null, 0);
   }

   public PagerIndicator(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(attrs, 0);
   }

   public PagerIndicator(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init(attrs, defStyle);
   }

   private void init(AttributeSet attrs, int defStyle) {
      final TypedArray a = getContext().obtainStyledAttributes(
          attrs, R.styleable.PagerIndicator, defStyle, 0);

      colorInactiveStroke = a.getColor(R.styleable.PagerIndicator_colorInactiveStroke, Color.GRAY);
      colorSelectedFill = a.getColor(R.styleable.PagerIndicator_colorSelectedFill,
          ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
      viewCount = a.getInt(R.styleable.PagerIndicator_viewCount, 3);
      activeView = a.getFloat(R.styleable.PagerIndicator_activeView, 0.f);

      a.recycle();

      inactiveCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      inactiveCirclePaint.setStyle(Paint.Style.STROKE);
      float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
          2, getResources().getDisplayMetrics());
      inactiveCirclePaint.setStrokeWidth(strokeWidth);

      selectedCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      selectedCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
      selectedCirclePaint.setStrokeWidth(strokeWidth);

      invalidatePaint();
   }

   private void invalidatePaint() {
      inactiveCirclePaint.setColor(colorInactiveStroke);
      selectedCirclePaint.setColor(colorSelectedFill);
   }

   @Override protected Parcelable onSaveInstanceState() {
      Bundle bundle = new Bundle();
      bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
      bundle.putFloat(KEY_ACTIVE_VIEW, activeView);
      return bundle;
   }

   @Override protected void onRestoreInstanceState(Parcelable state) {
      if (state instanceof Bundle) {
         Bundle bundle = (Bundle) state;
         super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_STATE));
         activeView = bundle.getFloat(KEY_ACTIVE_VIEW, 0.f);
      } else
         super.onRestoreInstanceState(state);
   }

   @Override
   protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      int cy = getHeight() / 2;
      int cx = getWidth() / (viewCount + 1);
      int radius = cy / 2;
      int round = Math.round(activeView);
      for (int i = 0; i < viewCount; i++) {
         canvas.drawCircle(cx * (i + 1), cy, radius,
             i == round ? selectedCirclePaint : inactiveCirclePaint);
      }
   }

   public @ColorInt int getColorInactiveStroke() {
      return colorInactiveStroke;
   }

   public void setColorInactiveStroke(@ColorInt int colorInactiveStroke) {
      this.colorInactiveStroke = colorInactiveStroke;
      invalidatePaint();
      invalidate();
   }

   public @ColorInt int getColorSelectedFill() {
      return colorSelectedFill;
   }

   public void setColorSelectedFill(@ColorInt int colorSelectedFill) {
      this.colorSelectedFill = colorSelectedFill;
      invalidatePaint();
      invalidate();
   }

   public int getViewCount() {
      return viewCount;
   }

   public void setViewCount(int viewCount) {
      if (this.viewCount != viewCount) {
         this.viewCount = viewCount;
         invalidate();
      }
   }

   public float getActiveView() {
      return activeView;
   }

   public void setActiveView(float activeView) {
      if (this.activeView != activeView) {
         this.activeView = activeView;
         invalidate();
      }
   }

}
