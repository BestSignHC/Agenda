package com.example.hecheng.richengben2.DaoImpl;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.hecheng.richengben2.DB.DBHelper;
import com.example.hecheng.richengben2.domin.User;

/**
 *
 * Created by HeCheng on 2017/4/22.
 */

public class UserSDao {

    private SQLiteDatabase dbWrite, dbRead;

    public UserSDao(DBHelper dbHelper){
        dbRead = dbHelper.getReadableDatabase();
        dbWrite = dbHelper.getWritableDatabase();
    }

    public void regUser(User u) {
        ContentValues values = new ContentValues();
        values.put("objectId", u.getObjectId());
        values.put("userName", u.getUsername());
        values.put("password", u.getPassword2());
        values.put("password2", u.getPassword2());
        values.put("id", u.getId());

        dbWrite.insert("_User", null, values);
    }
}
