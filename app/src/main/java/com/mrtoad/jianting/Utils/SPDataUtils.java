package com.mrtoad.jianting.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SPDataUtils {
    private static final String fileName = "myData";
    private static final Gson gson = new Gson();

    /**
     * 存储键值对信息到本地
     * @param context context
     * @param key Key
     * @param value 值
     */
    public static void storageInformation(Context context , String key , String value){
        SharedPreferences sp = context.getSharedPreferences(fileName , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key , value);
        editor.apply();
    }

    /**
     * 根据 key 从本地获取信息
     * @param context context
     * @param key Key
     * @return 返回对应 Key 的值
     */
    public static String getStorageInformation(Context context , String key){
        String Result = null;

        SharedPreferences sp = context.getSharedPreferences(fileName , Context.MODE_PRIVATE);
        Result = sp.getString(key , null);

        return Result;
    }

    /**
     * 存储键值对信息到本地，Value 为 Map 集合
     * @param context context
     * @param key Key
     * @param mapInfo 对应 Key 的 Map 集合
     */
    public static void storeMapInformation(Context context, String key, Map<String, String> mapInfo) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String mapInfoJson = gson.toJson(mapInfo);

        // 存储 Map 信息的 JSON 字符串
        editor.putString(key, mapInfoJson);
        editor.apply();
    }

    /**
     * 根据 Key 获取 Map 集合信息
     * @param context context
     * @param key Key
     * @return 对应 Key 的 Map 集合
     */
    public static Map<String, String> getMapInformation(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String mapInfoJson = sp.getString(key, null);

        if (mapInfoJson == null) {
            return null;
        }

        return gson.fromJson(mapInfoJson, new TypeToken<Map<String, String>>() {}.getType());
    }

    /**
     * 根据 Key 修改 Map 集合信息
     * @param context context
     * @param key Key
     * @param value 值
     * @param newValue 新的值
     */
    public static void updateMapInformation(Context context, String key, String value, String newValue) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String mapInfoJson = sp.getString(key, null);

        if (mapInfoJson == null) {
            return;
        }

        Map<String, String> mapInfo = gson.fromJson(mapInfoJson, new TypeToken<Map<String, String>>() {}.getType());

        // 修改 Map 中的特定字段
        mapInfo.put(value, newValue);

        // 将修改后的 Map 转换回 JSON 字符串
        String updatedMapInfoJson = gson.toJson(mapInfo);

        // 存储更新后的用户信息
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key , updatedMapInfoJson);
        editor.apply();
    }

    /**
     * 向本地的List中添加一个item
     * @param context context
     * @param listName List 的名称
     * @param item 要添加的item
     */
    public static void addLocalList(Context context, String listName, String item) {
        List<String> list = getLocalList(context, listName);
        if (!list.contains(item)) {
            list.add(item);
            saveLocalList(context, listName, list);
        }
    }

    /**
     * 从本地的List中移除一个item
     * @param context context
     * @param listName List 的名称
     * @param item 要移除的item
     */
    public static void removeLocalList(Context context, String listName, String item) {
        List<String> list = getLocalList(context, listName);
        if (list.contains(item)) {
            list.remove(item);
            saveLocalList(context, listName, list);
        }
    }

    /**
     * 指定本地的某个List，更新对应的某条数据
     * @param context context
     * @param listName List 的名称
     * @param oldItem 旧数据
     * @param newItem 新数据
     */
    public static void updateLocalList(Context context, String listName, String oldItem, String newItem) {
        List<String> list = getLocalList(context, listName);
        int index = list.indexOf(oldItem);
        if (index != -1) {
            list.set(index, newItem);
            saveLocalList(context, listName, list);
        }
    }

    /**
     * 获取指定的本地的某个List
     * @param context context
     * @param listName List 的名称
     * @return List
     */
    public static List<String> getLocalList(Context context, String listName) {
        String json = getStorageInformation(context, listName);
        List<String> list = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 清空指定的本地的某个List的所有数据
     * @param context context
     * @param listName List 的名称
     */
    public static void clearLocalList(Context context, String listName) {
        storageInformation(context, listName, "[]");
    }

    /**
     * 保存指定的本地的某个List的所有数据
     * @param context context
     * @param listName List 的名称
     * @param list List
     */
    private static void saveLocalList(Context context, String listName, List<String> list) {
        JSONArray jsonArray = new JSONArray(list);
        storageInformation(context, listName, jsonArray.toString());
    }
}
