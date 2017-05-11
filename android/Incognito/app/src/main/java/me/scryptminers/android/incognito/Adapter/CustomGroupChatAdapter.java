package me.scryptminers.android.incognito.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.scryptminers.android.incognito.Model.GroupMessage;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.R;

/**
 * Created by Samruddhi on 5/2/2017.
 */

public class CustomGroupChatAdapter extends ArrayAdapter<GroupMessage> {

    private TextView chatText;
    public List<GroupMessage> messages;
    private Context context;
    public CustomGroupChatAdapter(Context context, int resource, List<GroupMessage> messages) {
        super(context, resource, messages);
        this.messages = messages;
    }

    @Override
    public void add(GroupMessage object) {
        messages.add(object);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GroupMessage message = messages.get(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (message.getDirection().matches("left")) {
            row = inflater.inflate(R.layout.right_message_row, parent, false);
        }else{
            row = inflater.inflate(R.layout.left_message_row, parent, false);
        }
        chatText = (TextView) row.findViewById(R.id.messageText);
        chatText.setText(message.getMessage());
        return row;
    }
}

