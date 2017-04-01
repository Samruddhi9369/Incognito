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

import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.R;

/**
 * Created by Samruddhi on 3/31/2017.
 */

public class CustomChatAdapter extends ArrayAdapter<Message> {

    private TextView chatText;
    public List<Message> messages;
    private Context context;
    public CustomChatAdapter(Context context, int resource, List<Message> messages) {
        super(context, resource, messages);
        this.messages = messages;
    }

    @Override
    public void add(Message object) {
        messages.add(object);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Message message = messages.get(position);
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
