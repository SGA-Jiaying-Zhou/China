package com.sony.dtv.tvcamera.app.photosetting;

import java.util.ArrayList;
import java.util.List;

public class OptionItem {

    // SharedPreferences file name
    protected static final String SP_NAME = PhotoSettingConstants.SP_NAME;

    // don't modify this, it's keep for first view
    public static final OptionItem ALL_OPTION = new OptionItem("ALL_OPTION", "", "", new ArrayList<String>(), new ArrayList<String>(), "");


    // don't modify this ArrayList name, OptionAdapter will use it.
    // please add you custom option to it.
    public static final ArrayList<OptionItem> OptionItemList = new ArrayList<>();


    public String viewKey;
    public String valueKey;
    public String title;
    public List<String> entries;
    public List<String> entryValues;
    public String defValue;
    public boolean needDisable;

    public OptionItem(String viewKey, String valueKey, String title, List<String> entries, List<String> entryValues, String defValue) {
        if (entries.size() != entryValues.size()) throw new IllegalArgumentException();

        this.viewKey = viewKey;
        this.valueKey = valueKey;
        this.title = title;
        this.entries = entries;
        this.entryValues = entryValues;
        this.defValue = defValue;

        needDisable = entries.size() <= 1;
    }

    public static void addToOptionItemList(OptionItem optionItem) {
        OptionItemList.add(optionItem);
    }

    public static void clearOptionItemList() {
        OptionItemList.clear();
    }

    public static void removeFromOptionItemList(OptionItem optionItem) {
        OptionItemList.remove(optionItem);
    }

}
