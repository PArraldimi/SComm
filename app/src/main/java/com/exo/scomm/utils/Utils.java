package com.exo.scomm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.exo.scomm.data.models.User;

public class Utils {



  public static  void storeUser(Context context, String name, String uid, String image ) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString("name", name);
    editor.putString("uid", uid);
    editor.putString("image", image);
    editor.apply();
    Log.e("TAG", ""+name+" "+uid);
  }

  public static User retrieveUser(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    String uid = preferences.getString("uid", null);
    String name = preferences.getString("name", null);
    String image = preferences.getString("image", null);

    if (uid == null && name == null){
      return null;
    }
    User user = new User();
    user.setUsername(name);
    user.setUID(uid);
    user.setImage(image);
    return user;
  }


}
