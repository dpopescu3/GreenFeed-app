package diana.com.Utilis;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;
/**
* Class that stores fragments for tabs
 * */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter{
    private static final String TAG = "SectionsPagerAdapter";

    private final List<Fragment>mFragmentList=new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }


    public void addFragment(Fragment fragment){
        mFragmentList.add(fragment);
    }

}
