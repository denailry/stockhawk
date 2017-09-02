package com.project.denail.stockhawk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by denail on 17/09/01.
 */

public class BackCache {
    private static BackCache currentBackCache;

    private List<OnBackPressListener> backStack;

    public interface OnBackPressListener {
        void onBackPress();
    }

    public static void newInstance() {
        BackCache newCache = new BackCache();
        newCache.backStack = new ArrayList<>();
        currentBackCache = newCache;
    }

    public static void addToBackCache(OnBackPressListener listener) {
        currentBackCache.add(listener);
    }

    public static void popBackCache() {
        currentBackCache.remove(getSize()-1);
    }

    public static void onBackPress(){
        OnBackPressListener listener = currentBackCache.backStack.get(getSize()-1);
        popBackCache();
        listener.onBackPress();
    }

    public static int getSize() {
        return currentBackCache.backStack.size();
    }

    private void add(OnBackPressListener listener) {
        this.backStack.add(listener);
    }

    private void remove(int position) {
        this.backStack.remove(position);
    }
}
