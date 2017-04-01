package me.scryptminers.android.incognito.Activity;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import me.scryptminers.android.incognito.Adapter.CustomChatAdapter;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.R;

public class ChatActivity extends AppCompatActivity {
    ImageView buttonSend;
    ListView listViewChat;
    EditText messageToSend;
    CustomChatAdapter customChatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        buttonSend = (ImageView) findViewById(R.id.send);

        listViewChat = (ListView) findViewById(R.id.listView_chat);

        ArrayList<Message> msgs = new ArrayList<Message>();
        msgs.add(new Message("Me","hello","left"));
        msgs.add(new Message("You","bye","left"));
        msgs.add(new Message("Me","hello","right"));
        msgs.add(new Message("You","bye","right"));
        msgs.add(new Message("Me","hello","left"));

        customChatAdapter = new CustomChatAdapter(getApplicationContext(), R.layout.right_message_row,msgs);
        listViewChat.setAdapter(customChatAdapter);

        messageToSend = (EditText) findViewById(R.id.messageToSend);
        messageToSend.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        listViewChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listViewChat.setAdapter(customChatAdapter);

        //to scroll the list view to bottom on data change
        customChatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listViewChat.setSelection(customChatAdapter.getCount() - 1);
            }
        });
    }
    private boolean sendChatMessage() {
        customChatAdapter.add(new Message(messageToSend.getText().toString(),"author","left"));
        messageToSend.setText("");
        return true;
    }
}
