package diana.com.Utilis;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentList=new ArrayList<>();
    private final HashMap<Fragment,Integer>mFragments=new HashMap<>();
    private final HashMap<String,Integer>mFragmentNumbers=new HashMap<>();
    private final HashMap<Integer,String>mFragmentNames=new HashMap<>();


public SectionStatePagerAdapter(FragmentManager fm){
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
    public   void addFragment(Fragment fragment,String fragmentName){
    mFragmentList.add(fragment);
    mFragments.put(fragment,mFragmentList.size()-1);
    mFragmentNumbers.put(fragmentName,mFragmentList.size()-1);
    mFragmentNames.put(mFragmentList.size()-1,fragmentName);
    }

    /**
     * returns the fragment with the name @param
     * @param fragmentName
     * @return
     */
    public Integer getfragmentNumber(String fragmentName){
    if(mFragmentNumbers.containsKey(fragmentName)){
        return mFragmentNumbers.get(fragmentName);
    }
    else{
        return null;
    }
    }
    /**
     * returns the fragment with the name @param
     * @param fragment
     * @return
     */
    public Integer getfragmentNumber(Fragment fragment){
        if(mFragmentNumbers.containsKey(fragment)){
            return mFragmentNumbers.get(fragment);
        }
        else{
            return null;
        }
    }
    /**
     * returns the fragment with the name @param
     * @param fragmentNumber
     * @return
     */
    public Integer getfragmentName(Integer fragmentNumber){
        if(mFragmentNumbers.containsKey(fragmentNumber)){
            return mFragmentNumbers.get(fragmentNumber);
        }
        else{
            return null;
        }
    }
}
