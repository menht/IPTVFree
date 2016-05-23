package it.michelelacorte.iptvfree.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class rapresent Adapter for ViewPager.
 *
 * Created by Michele on 20/04/2016.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    /**
     * Public constructor of Adapter
     * @param manager FragmentManager
     */
    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
        if (manager.getFragments() != null) {
            manager.getFragments().clear();
        }
    }

    /**
     * Get item (Fragment) from position.
     * @param position int
     * @return Fragment
     */
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    /**
     * Get Fragment list size.
     * @return int
     */
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * Add fragment to list
     * @param fragment Fragment
     * @param title String
     */
    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    /**
     * Get page title from position
     * @param position int
     * @return CharSequece
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
