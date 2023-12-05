package com.sengsational.ratestation;

import android.os.Binder;
//https://stackoverflow.com/a/37774966/897007
public class ObjectWrapper extends Binder {

    private final Object mData;

    public ObjectWrapper(Object data) {
        mData = data;
    }

    public Object getData() {
        return mData;
    }
}