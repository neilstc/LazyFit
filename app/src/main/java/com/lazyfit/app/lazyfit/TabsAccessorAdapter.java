package com.lazyfit.app.lazyfit;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
            switch(position){
                case 0 :
                    FeedFragment feedFragment =new FeedFragment();
                    return  feedFragment;
                case 1 :
                    GroupsFragment groupsFragment =new GroupsFragment();
                    return  groupsFragment;
                case 2 :
                    TrainingFragment trainingFragment =new TrainingFragment();
                    return  trainingFragment;
                default:
                    return null;
            }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0 :
               return "Feed";
            case 1 :
                return "Groups";
            case 2 :
                return "Training";
            default:
                return null;
        }
    }
}
