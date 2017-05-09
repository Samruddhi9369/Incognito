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

        // Load groups in the groups adapter
        groups = db.getAllGroups();
        customGroupsAdapter = new CustomGroupsAdapter(getContext(), R.layout.custom_row, groups);
        listViewGroups.setAdapter(customGroupsAdapter);
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

}
