package com.sony.dtv.tvcamera.app.photosetting;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sony.dtv.tvcamera.R;

public class SettingFragment extends Fragment implements OptionFragment.ClickCallback {

    public static final String EXTRA_IS_VISIBLE = "extra_is_visible";
    private boolean mVisibleArgs;
    private TextView mTipView;

    private FragmentManager mFragmentManager;
    private SharedPreferences mSharedPreferences;

    private UpdateCallBack mCallBack;

    public interface UpdateCallBack {
        void updateOptionItems();
    }

    public void setCallBack (UpdateCallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVisibleArgs = getArguments().getBoolean(EXTRA_IS_VISIBLE, false);
        mSharedPreferences = getActivity().getSharedPreferences(OptionItem.SP_NAME, Context.MODE_PRIVATE);

        mFragmentManager = getFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(mBackStackChangedListener);
        OptionFragment.registerClickCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, null);

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        addAnimations(ft);
        Fragment fragmentOption = mFragmentManager.findFragmentById(R.id.fragment_option);
        if (fragmentOption == null) {
            fragmentOption = OptionFragment.newInstance(OptionItem.ALL_OPTION.viewKey, false);
            ft.add(R.id.fragment_option, fragmentOption);
        }
        ft.commit();

        mTipView = (TextView) view.findViewById(R.id.tip_view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        checkTipViewVisible(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void enterKeyClick(String whichView) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        addAnimations(ft);
        ft.addToBackStack(null);
        ft.replace(R.id.fragment_option, OptionFragment.newInstance(whichView, true));
        ft.commit();
    }

    @Override
    public void backKeyClick() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        addAnimations(ft);
        mCallBack.updateOptionItems();
        mFragmentManager.popBackStack();
    }

    private void checkTipViewVisible(boolean keep) {
        String smileShutterValue = mSharedPreferences.getString(PhotoSettingConstants.SmileShutterKey, getString(R.string.smile_shutter_defValue));
        boolean needShow = smileShutterValue.equals(getString(R.string.smile_shutter_defValue));

        if ( keep && mVisibleArgs && needShow ) {
            mTipView.setVisibility(View.VISIBLE);
        } else {
            mTipView.setVisibility(View.GONE);
        }
    }

    private void addAnimations(FragmentTransaction ft) {
        ft.setCustomAnimations(R.anim.fragment_slide_left_in,
                R.anim.fragment_slide_left_out, R.anim.fragment_slide_right_in,
                R.anim.fragment_slide_right_out);

    }

    private FragmentManager.OnBackStackChangedListener mBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            boolean isAllOptionView = isAllOptionView();
            checkTipViewVisible(isAllOptionView);
        }
    };

    public boolean isAllOptionView() {
        return ((OptionFragment)mFragmentManager.findFragmentById(R.id.fragment_option)).isAllOptionView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OptionFragment.unregisterClickCallback();
    }
}
