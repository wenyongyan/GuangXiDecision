package com.cxwl.guangxi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 解决HorizontalScrollView嵌套HorizontalScrollView滑动冲突
 * @author shawn_sun
 *
 */

public class MyVerticalScrollView extends ScrollView{
	
	private ScrollListener scrollListener = null;
	
	public MyVerticalScrollView(Context context) {
		super(context);
	}
	
	public MyVerticalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MyVerticalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setScrollListener(ScrollListener scrollListener) {
		this.scrollListener = scrollListener;
	}
	
	public interface ScrollListener {  
	    void onScrollChanged(MyVerticalScrollView hScrollView, int x, int y, int oldx, int oldy);  
	}  
	
    @Override  
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {  
        super.onScrollChanged(x, y, oldx, oldy);  
        if (scrollListener != null) {  
        	scrollListener.onScrollChanged(this, x, y, oldx, oldy);  
        }  
    }  
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		//防止滑动嵌套引起的滑动冲突
		if (ev.getAction() == MotionEvent.ACTION_MOVE && getParent() != null) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		return super.onTouchEvent(ev);
	}

}
