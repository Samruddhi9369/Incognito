package me.scryptminers.android.incognito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import me.scryptminers.android.incognito.Activity.ChatActivity;
import me.scryptminers.android.incognito.Activity.GroupChatActivity;
import me.scryptminers.android.incognito.Adapter.CustomFriendsAdapter;
import me.scryptminers.android.incognito.Adapter.CustomGroupsAdapter;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.Group;
import me.scryptminers.android.incognito.Model.User;
import me.scryptminers.android.incognito.Service.GroupService;
import me.scryptminers.android.incognito.Util.SharedValues;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class GroupListFragment extends Fragment {
    public static ListView listViewGroups;
    public static List<Group> groups;
    public static CustomGroupsAdapter customGroupsAdapter;
    private BroadcastReceiver broadcastReceiver;
    private boolean isRegistered;
    Intent groupIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);
        // Inflate the layout for this fragment

        listViewGroups = (ListView) view.findViewById(R.id.listViewGroups);
        ChatDatabaseHelper db = new ChatDatabaseHelper(getActivity());
        //User user = new User("sam","kal","email","8978675645","pwd","cpwd");

        /*groupIntent = new Intent(getActivity(), GroupService.class);
        getActivity().startService(groupIntent);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // msgs.clear();
                //msgs = db.getAllMessages(userEmail);
                Log.d("Message","In Onreceive");
                customGroupsAdapter.notifyDataSetChanged();
                //listViewChat.invalidate();
                //listViewChat.setSelection(customChatAdapter.getCount() - 1);
            }
        };*/

        groups = db.getAllGroups();
        customGroupsAdapter = new CustomGroupsAdapter(getContext(), R.layout.custom_row, groups);
        listViewGroups.setAdapter(customGroupsAdapter);
        //listViewGroups.invalidate();
        //customGroupsAdapter.notifyDataSetChanged();
        registerForContextMenu(listViewGroups);
        listViewGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), GroupChatActivity.class);
                intent.putExtra("GROUP_NAME",groups.get(position).getGroupName());
                startActivity(intent);
            }
        });
        return view;
    }

    public void loadGroups(){
        ChatDatabaseHelper db = new ChatDatabaseHelper(getActivity());
        //User user = new User("sam","kal","email","8978675645","pwd","cpwd");

        groups = db.getAllGroups();
        listViewGroups.setAdapter(new CustomGroupsAdapter(getContext(), R.layout.custom_row, groups));
        /*listViewGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent intent = new Intent(getActivity(), ChatActivity.class);
                //intent.putExtra("FRIEND_NAME",groups.get(position).getFirstName());
                //intent.putExtra("FRIEND_EMAIL",groups.get(position).getEmail());
                //startActivity(intent);
            }
        });*/
    }

    /*@Override
    public void onStart() {
        super.onStart();
        if (!isRegistered) {
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter("Groups"));
            isRegistered = true;

*//*            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // msgs.clear();
                    //msgs = db.getAllMessages(userEmail);
                    customChatAdapter.notifyDataSetChanged();
                    listViewChat.invalidate();
                    //listViewChat.setSelection(customChatAdapter.getCount() - 1);
                }
            };*//*

        }

    }


    @Override
    public void onStop() {
        super.onStop();
        if (isRegistered) {
            getActivity().unregisterReceiver(broadcastReceiver);
            isRegistered = false;
        }
        getActivity().stopService(groupIntent);
    }*/

}
