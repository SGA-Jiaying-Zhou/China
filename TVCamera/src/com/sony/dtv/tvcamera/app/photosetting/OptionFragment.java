package com.sony.dtv.tvcamera.app.photosetting;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.sony.dtv.tvcamera.R;

public class OptionFragment extends Fragment {

    // for view type
    private static final String CURRENT_VIEW_KEY = "current_view";
    private String mCurrentView;

    // for tip
    private static final String SHOW_TOPIC_KEY = "show_topic";
    private boolean mShowTopic;
    private TextView mTopicView;

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;

    private ListView mListView;
    private OptionAdapter mAdapter;

    private static ClickCallback mClickCallback;

    public interface ClickCallback {
        void enterKeyClick(String whichView);
        void backKeyClick();
    }

    public static void registerClickCallback(ClickCallback callback) {
        mClickCallback = callback;
    }

    public static void unregisterClickCallback() {
        mClickCallback = null;
    }

    public static Fragment newInstance(String string, boolean showTopic) {
        Bundle args = new Bundle();
        args.putString(CURRENT_VIEW_KEY, string);
        args.putBoolean(SHOW_TOPIC_KEY, showTopic);
        Fragment fragment = new OptionFragment();
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mSharedPreferences == null) {
            mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences(OptionItem.SP_NAME, Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
        }
        mCurrentView = getArguments().getString(CURRENT_VIEW_KEY);
        mShowTopic = getArguments().getBoolean(SHOW_TOPIC_KEY);
        mAdapter = new OptionAdapter(getActivity(), mCurrentView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.photo_settings_list, null);
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mClickListener);

        mTopicView = (TextView) view.findViewById(R.id.view_topic);
        mTopicView.setText(mAdapter.getTitleString());
        mTopicView.setVisibility(mShowTopic ? View.VISIBLE : View.INVISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView.requestFocus();
        if (!isAllOptionView() && mAdapter.getCount() > 0) {
            int position = mAdapter.getSelectedPosition();
            mListView.setSelection(position == -1 ? 0 : position);
        }
    }

    private AdapterView.OnItemClickListener mClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isAllOptionView()) {
                invokeCallBackEntryKeyClick(view);
            } else {
                invokeCallBackBackKeyClick(view);
            }
        }
    };

    private void invokeCallBackEntryKeyClick(View view) {
        OptionAdapter.ViewHolder vh = (OptionAdapter.ViewHolder) view.getTag();
        mClickCallback.enterKeyClick(vh.viewKey);
    }

    private void invokeCallBackBackKeyClick(View view) {
        OptionAdapter.ViewHolder vh = (OptionAdapter.ViewHolder)view.getTag();
        updateViewState(vh.valueKey, vh.value);
        mClickCallback.backKeyClick();
    }

    public boolean isAllOptionView() {
        return mCurrentView.equals(OptionItem.ALL_OPTION.viewKey);
    }

    private void updateViewState(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }


}
