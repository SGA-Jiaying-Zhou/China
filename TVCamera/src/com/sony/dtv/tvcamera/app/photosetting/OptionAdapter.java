package com.sony.dtv.tvcamera.app.photosetting;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sony.dtv.tvcamera.R;

import java.util.ArrayList;
import java.util.List;

public class OptionAdapter extends BaseAdapter {

    private final ArrayList<OptionItem> mOptionItemList = OptionItem.OptionItemList;
    private OptionItem mCurOptionItem;

    private Context mContext;
    private SharedPreferences mSP;

    private int mSelectedPos = -1;

    public OptionAdapter(Context context, String whichView) {
        mContext = context;
        mSP = mContext.getSharedPreferences(OptionItem.SP_NAME, Context.MODE_PRIVATE);
        setCurrentOptionItem(whichView);
        updateSelectedPosition();
    }

    private void setCurrentOptionItem(String which) {
        for (OptionItem optionItem : mOptionItemList) {
            if (which.equals(optionItem.viewKey)) {
                mCurOptionItem = optionItem;
            }
        }

        if (mCurOptionItem == null) {
            mCurOptionItem = OptionItem.ALL_OPTION;
        }
    }

    private boolean isAllOptionView() {
        return mCurOptionItem.equals(OptionItem.ALL_OPTION);
    }

    @Override
    public int getCount() {
        if (isAllOptionView()) {
            return mOptionItemList.size();
        } else {
            return mCurOptionItem.entries.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (isAllOptionView()) {
            return mOptionItemList.get(position);
        } else {
            return mCurOptionItem.entries;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.photo_settings_item, null);
            vh = new ViewHolder();
            vh.checkMark = (ImageView) convertView.findViewById(R.id.checkmark);
            vh.title = (TextView) convertView.findViewById(R.id.title);
            vh.description = (TextView)convertView.findViewById(R.id.description);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        // ALL_OPTION need SharedPreferences value to update the description. And SecondaryView need the value to update visibility of checkMark.
        String spValue = mSP.getString(getViewValueKey(position), getViewDefState(position));

        if (isAllOptionView()) {
            vh.checkMark.setVisibility(View.GONE);
            OptionItem optionItem = (OptionItem)getItem(position);
            if (optionItem.needDisable) convertView.setAlpha(0.5f);
            vh.title.setText(optionItem.title);
            updateAllOptionViewDescriptionViewState(vh.description, position, spValue);
            vh.valueKey = getViewValueKey(position);
            vh.viewKey = getViewKey(position);
            vh.needDisable = isNeedDisable(position);
        } else {
            boolean isFocused = getSecondaryViewEntryValue(position).equals(spValue);
            vh.checkMark.setVisibility(isFocused ? View.VISIBLE : View.INVISIBLE);
            vh.title.setText(getSecondaryViewEntry(position));
            vh.description.setVisibility(View.GONE);
            vh.valueKey = getViewValueKey(position);
            vh.value = getSecondaryViewEntryValue(position);
            vh.viewKey = getViewKey(position);
            vh.needDisable = false;
        }

        return convertView;
    }

    private void updateAllOptionViewDescriptionViewState(TextView textView, int position, String spValue) {
        String descriptionStr = getAllOptionDescBySPValue(position, spValue);
        if (TextUtils.isEmpty(descriptionStr)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(descriptionStr);
        }
    }

    public int getSelectedPosition() {
        return mSelectedPos;
    }

    public void updateSelectedPosition() {
        if (isAllOptionView()) return;

        String value = mSP.getString(getViewValueKey(0), getViewDefState(0));
        List<String> list = mCurOptionItem.entryValues;
        mSelectedPos = list.indexOf(value);
    }

    public String getTitleString() {
        return mCurOptionItem.title;
    }

    private List<String> getAllOptionViewEntryValues(int position) {
        return mOptionItemList.get(position).entryValues;
    }

    private List<String> getAllOptionViewEntries(int position) {
        return mOptionItemList.get(position).entries;
    }

    private String getAllOptionDescBySPValue(int position, String spValue) {
        List<String> values = getAllOptionViewEntryValues(position);
        if (values.indexOf(spValue) == -1) {
            return "";
        }
        return getAllOptionViewEntries(position).get(values.indexOf(spValue));
    }

    private String getSecondaryViewEntry(int position) {
        return mCurOptionItem.entries.get(position);
    }

    private String getSecondaryViewEntryValue(int position) {
        return mCurOptionItem.entryValues.get(position);
    }

    private boolean isNeedDisable(int position) {
        return mOptionItemList.get(position).needDisable;
    }

    private String getViewKey(int position) {
        if (isAllOptionView()) {
            return mOptionItemList.get(position).viewKey;
        } else {
            return mCurOptionItem.viewKey;
        }
    }


    private String getViewValueKey(int position) {
        if (isAllOptionView()) {
            return mOptionItemList.get(position).valueKey;
        } else {
            return mCurOptionItem.valueKey;
        }
    }

    private String getViewDefState(int position) {
        if (isAllOptionView()) {
            return mOptionItemList.get(position).defValue;
        } else {
            return mCurOptionItem.defValue;
        }
    }

    class ViewHolder {
        public ImageView checkMark;
        public TextView title;
        public TextView description;

        public String viewKey;
        public String valueKey; // for all option view and secondary view
        public String value; // for secondary view
        public boolean needDisable;
    }

}
