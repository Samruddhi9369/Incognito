package me.scryptminers.android.incognito;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import me.scryptminers.android.incognito.Activity.ChatActivity;
import me.scryptminers.android.incognito.Adapter.CustomFriendsAdapter;
import me.scryptminers.android.incognito.Adapter.CustomGroupsAdapter;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.Group;
import me.scryptminers.android.incognito.Model.User;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class GroupListFragment extends Fragment {
    private ListView listViewGroups;
    private List<Group> groups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);
        // Inflate the layout for this fragment

        listViewGroups = (ListView) view.findViewById(R.id.listViewGroups);
        loadGroups();
        registerForContextMenu(listViewGroups);
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

}
