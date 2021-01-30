package com.example.outdoors;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class InvitesActivity extends BaseDrawerActivity {

    private ViewPager mViewPager;

    private SectionsStatePagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_invites, contentLayout);

        mViewPager = (ViewPager) findViewById(R.id.containerInvites);
        setUpViewPager(mViewPager);
    }



    private void setUpViewPager(ViewPager viewPager){
        adapter = new SectionsStatePagesAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new RecInvFrag(), "RecInv");
        adapter.addFragment(new SentInvFrag(), "SentInv");
        adapter.addFragment(new HikeInvFrag(), "HikeInv");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragNum){
        mViewPager.setCurrentItem(fragNum);
    }
}
