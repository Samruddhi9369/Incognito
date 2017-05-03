package me.scryptminers.android.incognito.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.scryptminers.android.incognito.Model.Group;
import me.scryptminers.android.incognito.Model.User;
import me.scryptminers.android.incognito.R;

/**
 * Created by Samruddhi on 4/28/2017.
 */

public class CustomGroupsAdapter extends ArrayAdapter<Group> {

    private List<Group> groupsList;
    public CustomGroupsAdapter(Context context, int resource, List<Group> groupsList) {
        super(context, resource, groupsList);
        this.groupsList = groupsList;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Group group = groupsList.get(position);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.custom_row, null);
        TextView textView = (TextView) row.findViewById(R.id.rowName);
        TextView textDate = (TextView) row.findViewById(R.id.rowDate);
        textView.setText(group.getGroupName());
        String[] members1 = group.getGroupMembers();
        StringBuilder builder = new StringBuilder();
        for (String member : members1) {
            builder.append(member).append(",");
        }

        builder.deleteCharAt(builder.length() - 1);
        Log.e("MEmbers",builder.toString());
        textDate.setText("Participants: "+ group.getGroupMembers().length);
        return row;
    }

}
