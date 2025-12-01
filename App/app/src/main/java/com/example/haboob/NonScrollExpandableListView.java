package com.example.haboob;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * Custom ExpandableListView that disables scrolling and expands to show all content.
 * This view automatically adjusts its height to display all child items without
 * internal scrolling, useful when the ExpandableListView is placed inside a ScrollView.
 *
 * @author David
 * @version 1.0
 */
public class NonScrollExpandableListView extends ExpandableListView {

    /**
     * Constructor for creating the view programmatically.
     *
     * @param context The Context the view is running in
     */
    public NonScrollExpandableListView(Context context) {
        super(context);
    }

    /**
     * Constructor for creating the view from XML.
     *
     * @param context The Context the view is running in
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    public NonScrollExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor for creating the view from XML with a style.
     *
     * @param context The Context the view is running in
     * @param attrs The attributes of the XML tag that is inflating the view
     * @param defStyle An attribute in the current theme that contains a reference to a style resource
     */
    public NonScrollExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Measures the view and its content to determine the measured width and height.
     * Overrides the default behavior to calculate height based on all children,
     * effectively disabling scrolling by making the view tall enough to show all content.
     *
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Calculate height based on all children
        int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}