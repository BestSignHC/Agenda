package com.example.hecheng.richengben2.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.hecheng.richengben2.common.DateUtils;

import java.util.Date;

/**
 * Created by HeCheng on 2017/4/21.
 */

public class DBHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "database_test";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        String createScheduleTable = "CREATE TABLE IF NOT EXISTS `_Schedule` (" +
                "`objectId`  varchar(255) NOT NULL ," +
                "`userId`  varchar(255) NOT NULL ," +
                "`detail`  varchar(255) NOT NULL ," +
                "`remark`  varchar(255) NULL ," +
                "`startTime`  varchar(255) NOT NULL ," +
                "`endTime`  varchar(255) NOT NULL ," +
                "`status`  int(1) NOT NULL ," +
                "`color`  int(1) NOT NULL ," +
                "`createDate`  varchar(255) NOT NULL ," +
                "`doneDate`  varchar(255) NULL ," +
                "`reason`  varchar(255) NULL ," +
                "`isNotice`  tinyint(1) NOT NULL ," +
                "PRIMARY KEY (`objectId`)" +
                ");";
//        String a = "drop table '_Note'";
//        db.execSQL(a);
        String createNoteTable ="create table  IF NOT EXISTS '_Note' (n_id integer primary key autoincrement, n_userId varchar, n_title varchar, " +
                "n_content varchar, n_group_id integer, n_group_name varchar, n_type integer, " +
                "n_bg_color varchar, n_encrypt integer, n_create_time datetime," +
                "n_update_time datetime,n_objectId varchar,n_imgName varchar,n_imgLocalPath varchar,n_imgUrl varchar )";

//        String a = "drop table '_Plan'";
//        db.execSQL(a);
        String createPlanTable = "CREATE TABLE  IF NOT EXISTS `_Plan` (" +
                "'id' varchar(255) NOT NULL ," +
                "`objectId`  varchar(255) NULL ," +
                "`userId`  varchar(255) NOT NULL ," +
                "`detail`  varchar(255) NOT NULL ," +
                "`status`  tinyint NOT NULL ," +
                "`createDate`  varchar(255) NOT NULL ," +
                "`endDate`  varchar(255) NULL ," +
                "`parentId`  varchar(255) NULL ," +
                "`planOrder`  int(1) NOT NULL ," +
                "`process`  float NULL ," +
                "`remark`  varchar(255) NULL ," +
                "`delReason`  varchar(255) NULL ," +
                "`type`  tinyint NOT NULL ," +
                "PRIMARY KEY (`id`)" +
                ");";

        db.execSQL(createPlanTable);
        db.execSQL(createScheduleTable);
        db.execSQL(createNoteTable);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createGroupTable = "create table IF NOT EXISTS '_Group' (g_id integer primary key autoincrement, " +
                "g_name varchar, g_order integer, g_color varchar, g_encrypt integer," +
                "g_create_time datetime, g_update_time datetime )";
        db.execSQL(createGroupTable);
        db.execSQL("insert into _Group(g_name, g_order, g_color, g_encrypt, g_create_time, g_update_time) " +
                "values(?,?,?,?,?,?)", new String[]{"默认笔记", "1", "#FFFFFF", "0", DateUtils.date2string(new Date()),DateUtils.date2string(new Date())});

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
