package me.scryptminers.android.incognito.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import me.scryptminers.android.incognito.Adapter.SectionsPagerAdapter;
import me.scryptminers.android.incognito.FriendsListFragment;
import me.scryptminers.android.incognito.GroupListFragment;
import me.scryptminers.android.incognito.R;
import me.scryptminers.android.incognito.Service.GroupService;
import me.scryptminers.android.incognito.Service.MessageService;
import me.scryptminers.android.incognito.Util.HashFunctions;
import me.scryptminers.android.incognito.Util.KeyGenerator;
import me.scryptminers.android.incognito.Util.SharedValues;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int activeTab = 0;
    private final int REQUEST_CODE = 333;
    FriendsListFragment friendsFragment;
    GroupListFragment groupFragment;
    Intent groupIntent;
    private BroadcastReceiver broadcastReceiver;
    private boolean isRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //KeyGenerator.generateKeys();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedValues.init(getApplicationContext());
        //Service for get groups
        groupIntent = new Intent(this, GroupService.class);
        startService(groupIntent);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // msgs.clear();
                //msgs = db.getAllMessages(userEmail);
                Log.d("Message","In Onreceive");
                GroupListFragment.customGroupsAdapter.notifyDataSetChanged();
                GroupListFragment.listViewGroups.invalidate();
                //listViewChat.setSelection(customChatAdapter.getCount() - 1);
            }
        };

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activeTab == 0){
                    Intent intent = new Intent(MainActivity.this,QRCodeScannerActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this,CreateGroupActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });

        friendsFragment = new FriendsListFragment();
        groupFragment = new GroupListFragment();
        List<Fragment> fragmentsList = new ArrayList<>();
        fragmentsList.add(friendsFragment);
        fragmentsList.add(groupFragment);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),fragmentsList);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                activeTab = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // refresh the group fragment
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.action_public_key:
                Intent intent = new Intent(this,QRCodeGeneratorActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isRegistered) {
            registerReceiver(broadcastReceiver, new IntentFilter("Groups"));
            isRegistered = true;

/*            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // msgs.clear();
                    //msgs = db.getAllMessages(userEmail);
                    customChatAdapter.notifyDataSetChanged();
                    listViewChat.invalidate();
                    //listViewChat.setSelection(customChatAdapter.getCount() - 1);
                }
            };*/

        }

    }


    @Override
    public void onStop() {
        super.onStop();
        if (isRegistered) {
            unregisterReceiver(broadcastReceiver);
            isRegistered = false;
        }
        stopService(groupIntent);
    }

}
