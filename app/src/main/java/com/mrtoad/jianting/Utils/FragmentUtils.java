package com.mrtoad.jianting.Utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class FragmentUtils {
    /**
     * 加载 Fragment
     *
     * @param fragmentManager fragmentManager
     * @param resId           容器的 ID
     * @param fragment        Fragment
     * @return
     */
    public static boolean loadFragment(FragmentManager fragmentManager , int resId , Fragment fragment) {
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(resId , fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * 显示 Fragment
     * @param fragmentManager fragmentManager
     * @param fragment Fragment
     */
    public static void showFragment(FragmentManager fragmentManager , Fragment fragment) {
        fragmentManager.beginTransaction()
                .show(fragment)
                .commit();
    }

    /**
     * 隐藏 Fragment
     * @param fragmentManager fragmentManager
     * @param fragment Fragment
     */
    public static void hideFragment(FragmentManager fragmentManager , Fragment fragment) {
        fragmentManager.beginTransaction()
                .hide(fragment)
                .commit();
    }
}
