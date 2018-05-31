package com.example.tsult.sparrow;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.tsult.sparrow.Fragments.ChatFragment;
import com.example.tsult.sparrow.Fragments.FriendFragment;
import com.example.tsult.sparrow.Fragments.RequestFragment;

/**
 * Created by tsult on 25/3/2018.
 */

class SectionPagerAdepter extends FragmentPagerAdapter{
    public SectionPagerAdepter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 2:
                FriendFragment friendFragment = new FriendFragment();
                return friendFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Request";
            case 1:
                return "Chat";
            case 2:
                return "Friend";
            default:
                return null;
        }
    }
}
