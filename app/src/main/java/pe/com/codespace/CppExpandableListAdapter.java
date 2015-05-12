package pe.com.codespace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import java.util.HashMap;
import java.util.List;

/**
 * Creado por Carlos on 23/11/13.
 */
public class CppExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<RowGroupCpp> _listHeader;
    private HashMap<RowGroupCpp, List<RowItemCpp>> _listChild;

    public CppExpandableListAdapter(Context context, List<RowGroupCpp> listHeader, HashMap<RowGroupCpp, List<RowItemCpp>> listChild){
        this.context = context;
        this._listHeader = listHeader;
        this._listChild = listChild;
    }


    @Override
    public int getGroupCount() {
        return this._listHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listChild.get(this._listHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._listChild.get(this._listHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) {

        View row = view;
        TextHolderGroup holder;

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.listcpp_group, null);
            holder = new TextHolderGroup(row);
            row.setTag(holder);
        }
        else{
            holder = (TextHolderGroup) row.getTag();
        }

        RowGroupCpp temp = (RowGroupCpp) getGroup(groupPosition);
        if(temp.num!=0 && temp.num!=8){
            holder.myTitle.setText("Titulo " + temp.numromano + ": " + temp.title);
        }
        else{
            holder.myTitle.setText(temp.title);
        }

        if(temp.num!=0){
            holder.myDescription.setText(temp.description);
            holder.myDescription.setVisibility(View.VISIBLE);
        }
        else{
            holder.myDescription.setVisibility(View.GONE);
        }

        //row.setPadding(-30,0,0,0);
        return row;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {

        View row = view;
        TextHolderItem holder;

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.listcpp_item, null);
            holder = new TextHolderItem(row);
            row.setTag(holder);
        }
        else{
            holder = (TextHolderItem) row.getTag();
        }

        RowItemCpp temp = (RowItemCpp) getChild(groupPosition, childPosition);
        if(temp != null){
            holder.myTitle.setText("Capítulo " + temp.num + ": " + temp.title);
            holder.myDescription.setText(temp.description);
            //return row;
        }
        else{
           row = null;
        }
        row.setPadding(30,0,0,0);
        return row;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}




