package me.scryptminers.android.incognito.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.scryptminers.android.incognito.Model.User;
import me.scryptminers.android.incognito.R;

/**
 * Created by Samruddhi on 3/30/2017.
 */

public class CustomFriendsAdapter extends ArrayAdapter<User> {

    private List<User> friendsList;
    public CustomFriendsAdapter(Context context, int resource, List<User> friendsList) {
        super(context, resource, friendsList);
        this.friendsList = friendsList;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User friend = friendsList.get(position);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.custom_row, null);
        TextView textView = (TextView) row.findViewById(R.id.rowName);
        TextView textDate = (TextView) row.findViewById(R.id.rowDate);
        textView.setText(friend.getFirstName()+" "+friend.getLastName());
        textDate.setText(friend.getEmail());
        return row;
    }

}
