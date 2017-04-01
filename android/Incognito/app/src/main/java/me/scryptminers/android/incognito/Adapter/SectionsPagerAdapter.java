package me.scryptminers.android.incognito.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private final String[] sections = {"Friends","Groups"};
    private List<Fragment> fragmentsList;

    public SectionsPagerAdapter(FragmentManager fm, List<Fragment> fragmentsList) {
        super(fm);
        this.fragmentsList = fragmentsList;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return fragmentsList.get(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return sections.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return sections[position];
    }
}
