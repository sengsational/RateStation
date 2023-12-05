package com.sengsational.ratestation;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class QueryPkg implements Serializable {
    private static final String TAG = QueryPkg.class.getSimpleName();

    public static final String PULL_FIELDS = "prefPullFields";
    public static final String SELECTION_FIELDS = "prefSelectionFields";
    public static final String SELECTION_ARGS = "prefSelectionArgs";
    public static final String ORDER_BY = "prefOrderBy";
    public static final String SECOND_ORDER_BY = "prefSecondOrderBy";
    public static final String FULL_TEXT_SEARCH = "prefFullTextSearch";

    private static final String delimiter = "%;%;";

    public QueryPkg(String table, String[] pullFields, String selectionFields, String[] selectionArgs, Object o, Object o1, String orderBy, boolean hideMixesAndFlights, String fullTextSearch, Context context) {
        setPullFields(pullFields, context);
        setSelectionFields(selectionFields, context);
        setSelectionArgs(selectionArgs, context);
        setOrderBy(orderBy, context);
        setFullTextSearch(fullTextSearch, context);
    }

    public static void setOrderBy(String orderBy, Context context) {
        if (orderBy == null) orderBy = StationDbItem.ID;
        Context contextToUse = context;
        if (contextToUse == null) {
            String result1 = CallerUtility.mySecurityManager.getCallerClassName(1);
            String result2 = CallerUtility.mySecurityManager.getCallerClassName(1);
            Log.e(TAG, "setOrderBy(orderBy, context) was passed a null context.  Ignoring sort. \n" + result1 + "\n" + result2);
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(contextToUse);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(ORDER_BY, orderBy);
        editor.apply();
    }

    public static String getOrderBy(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(ORDER_BY, StationDbItem.ID);
    }


    public static void setSecondSortBy(String secondOrderBy, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(SECOND_ORDER_BY, secondOrderBy);
        editor.apply();
    }

    public static String getSecondOrderBy(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SECOND_ORDER_BY, "");
    }

    public static void setFullTextSearch(String fullTextSearch, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(FULL_TEXT_SEARCH, fullTextSearch);
        editor.apply();
    }

    public static String getFullTextSearch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(FULL_TEXT_SEARCH, "");
    }

    public static void setPullFields(String[] pullFields, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String pullFieldsString = makeString(pullFields);
        Log.v(TAG, "setPullFields in preferences: " + pullFieldsString);
        editor.putString(PULL_FIELDS, pullFieldsString);
        editor.apply();
    }

    public static String[] getPullFields(Context context) {
        String selectionArgsString = PreferenceManager.getDefaultSharedPreferences(context).getString(PULL_FIELDS, "");
        return makeArray(selectionArgsString);
    }

    public static void setSelectionFields(String selectionFields, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(SELECTION_FIELDS, selectionFields);
        editor.apply();
    }

    public static String getSelectionFields(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SELECTION_FIELDS, "");
    }

    public static void appendSelectionFields(String additionalSelection, Context context) { // ie " AND STYLE<>?"
        setSelectionFields(getSelectionFields(context) + additionalSelection, context);
    }


    public static void setSelectionArgs(String[] selectionArgs, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(SELECTION_ARGS, makeString(selectionArgs));
        editor.apply();
    }

    public static String[] getSelectionArgs(Context context) {
        //Log.v(TAG, "context passed in was: " + context.getClass().getSimpleName());
        String selectionArgsString = PreferenceManager.getDefaultSharedPreferences(context).getString(SELECTION_ARGS, "");
        return makeArray(selectionArgsString);
    }

    public static void appendSelectionArgs(String additionalArg, Context context) { // ie "Mix"
        ArrayList<String> selectionArgsArray = new ArrayList<>();
        selectionArgsArray.addAll(Arrays.asList(getSelectionArgs(context)));
        selectionArgsArray.add(additionalArg);
        setSelectionArgs(selectionArgsArray.toArray(new String[0]), context);
    }


    public static boolean includesSelection(String columnName, Context context) {
        return getSelectionFields(context).contains(columnName);
    }

    // Support for saving string array in preferences
    private static String makeString(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            sb.append(stringArray[i]).append(delimiter);
        }
        return sb.toString();
    }

    private static String[] makeArray(String delimitedString) {
        return delimitedString.split(delimiter);
    }

    public static class ObjectSerializerHelper {
        static public String objectToString(Serializable object) {
            String encoded = null;
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(object);
                objectOutputStream.close();
                encoded = new String(Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return encoded;
        }

        @SuppressWarnings("unchecked")
        static public Serializable stringToObject(String string) {
            byte[] bytes = Base64.decode(string, 0);
            Serializable object = null;
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
                object = (Serializable) objectInputStream.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            return object;
        }

    }

    private static class CallerUtility  {
        public String getCallerClassName(int callStackDepth) {
            return mySecurityManager.getCallerClassName(callStackDepth);
        }

        public String getMethodName() {
            return "SecurityManager";
        }

        /**
         * A custom security manager that exposes the getClassContext() information
         */
        static class MySecurityManager extends SecurityManager {
            public String getCallerClassName(int callStackDepth) {
                return getClassContext()[callStackDepth].getName();
            }
        }

        private final static MySecurityManager mySecurityManager =
                new MySecurityManager();
    }
}








