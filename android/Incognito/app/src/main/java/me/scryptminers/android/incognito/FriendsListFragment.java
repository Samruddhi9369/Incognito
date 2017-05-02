package me.scryptminers.android.incognito;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import me.scryptminers.android.incognito.Activity.ChatActivity;
import me.scryptminers.android.incognito.Adapter.CustomFriendsAdapter;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.User;


public class FriendsListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private  ListView listViewFriends;
    private List<User> friends;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        // Inflate the layout for this fragment

        listViewFriends = (ListView) view.findViewById(R.id.listViewFriends);
        loadFriends();
        registerForContextMenu(listViewFriends);
        return view;
    }

    public void loadFriends(){
        ChatDatabaseHelper db = new ChatDatabaseHelper(getActivity());
        //User user = new User("sam","kal","email","8978675645","pwd","cpwd");

        friends = db.getAllUsers();
        // friends.add(user);
        listViewFriends.setAdapter(new CustomFriendsAdapter(getContext(), R.layout.custom_row, friends));
        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("FRIEND_NAME",friends.get(position).getFirstName());
                intent.putExtra("FRIEND_EMAIL",friends.get(position).getEmail());
                startActivity(intent);
            }
        });

    }

}
