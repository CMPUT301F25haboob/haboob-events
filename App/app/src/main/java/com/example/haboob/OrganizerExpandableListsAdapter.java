package com.example.haboob;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * {@code OrganizerExpandableListsAdapter} is a custom adapter used to populate an
 * {@link android.widget.ExpandableListView} with entrant data for a selected event.
 * <p>
 * It maps group headers (e.g., "Invited Entrants", "Waiting Entrants") to lists
 * of entrant identifiers and manages how each group and child item is displayed
 * in the expandable list UI.
 */
public class OrganizerExpandableListsAdapter extends BaseExpandableListAdapter {

    /** Context from the parent fragment or activity. */
    private Context mContext;

    /** Titles for each expandable list group (e.g., list categories). */
    private ArrayList<String> expandableListTitles;

    /** Mapping of each group title to its child items (entrant IDs or names). */
    private HashMap<String, ArrayList<String>> expandableListDetail;

    /**
     * Constructs an adapter to bind expandable list data to a view.
     *
     * @param mContext the current context (e.g., fragment or activity)
     * @param expandableListTitles list of group titles
     * @param expandableListDetail mapping from group title to its list of child entries
     */
    public OrganizerExpandableListsAdapter(Context mContext, ArrayList<String> expandableListTitles, HashMap<String, ArrayList<String>> expandableListDetail) {
        this.mContext = mContext;
        this.expandableListTitles = expandableListTitles;
        this.expandableListDetail = expandableListDetail;
    }

    /**
     * Returns a specific child element from a group.
     *
     * @param groupPosition index of the group
     * @param childPosition index of the child within the group
     * @return the child object
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.expandableListDetail.get(this.expandableListTitles.get(groupPosition)).get(childPosition);
    }

    /**
     * Returns the ID for a specific child element.
     *
     * @param groupPosition index of the group
     * @param childPosition index of the child
     * @return the child ID
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * Inflates and populates a child item view in the expandable list.
     *
     * @param groupPosition index of the group
     * @param childPosition index of the child
     * @param isLastChild whether this child is the last in the group
     * @param convertView recycled view for performance
     * @param parent parent view group
     * @return the populated view for the child
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String expandedListText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.organizer_list_item, null);
        }

        TextView expandedListTextView = convertView.findViewById(R.id.expandedListItem);
        expandedListTextView.setText(expandedListText);
        return convertView;
    }

    /**
     * Returns the number of child elements in a specific group.
     *
     * @param groupPosition index of the group
     * @return number of children within the group
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return this.expandableListDetail.get(this.expandableListTitles.get(groupPosition)).size();
    }

    /**
     * Returns the group (header) at the specified position.
     *
     * @param groupPosition index of the group
     * @return the group object (title)
     */
    @Override
    public Object getGroup(int groupPosition) {
        return this.expandableListTitles.get(groupPosition);
    }

    /**
     * Returns the total number of groups (headers).
     *
     * @return number of groups
     */
    @Override
    public int getGroupCount() {
        return this.expandableListTitles.size();
    }

    /**
     * Returns the ID of a group.
     *
     * @param groupPosition index of the group
     * @return group ID
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * Inflates and populates a group (header) view in the expandable list.
     *
     * @param groupPosition index of the group
     * @param isExpanded whether the group is currently expanded
     * @param convertView recycled view for performance
     * @param parent parent view group
     * @return the populated view for the group header
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.organizer_list_group, null);
        }

        TextView listTitleTextView = convertView.findViewById(R.id.listTitle);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    /**
     * Indicates whether IDs are stable across changes to the underlying data.
     *
     * @return {@code false} since IDs are not stable in this adapter
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Indicates whether a specific child is selectable.
     *
     * @param groupPosition index of the group
     * @param childPosition index of the child
     * @return {@code false} since child items are not selectable by default
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
