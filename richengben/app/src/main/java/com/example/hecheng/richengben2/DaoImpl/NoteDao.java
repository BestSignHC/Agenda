package com.example.hecheng.richengben2.DaoImpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.hecheng.richengben2.DB.DBHelper;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.DateUtils;
import com.example.hecheng.richengben2.common.FileUtil;
import com.example.hecheng.richengben2.domin.Note;
import com.example.hecheng.richengben2.domin.Schedule;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.example.hecheng.richengben2.R.mipmap.note;

public class NoteDao {
    private DBHelper helper;
    private SQLiteDatabase dbWrite, dbReader;

    public NoteDao(Context context) {
        helper = new DBHelper(context);
        dbWrite = helper.getWritableDatabase();
    }

    /**
     * 查询所有笔记
     */
    public List<Note> queryNotesAll(int groupId, String userId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            if (groupId > 0) {
                sql = "select * from _Note where n_group_id =" + groupId + " and n_userId = '" + userId + "'" +
                        "order by n_create_time desc";
            } else {
                sql = "select * from _Note where  n_userId = '" + userId + "'";
            }
            cursor = db.rawQuery(sql, null);
            //cursor = db.query("note", null, null, null, null, null, "n_id desc");
            while (cursor.moveToNext()) {
                //循环获得展品信息
                note = new Note();
                note.setObjectId(cursor.getString(cursor.getColumnIndex("n_objectId")));
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setUserId(cursor.getString(cursor.getColumnIndex("n_userId")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setGroupName(cursor.getString(cursor.getColumnIndex("n_group_name")));
                note.setType(cursor.getInt(cursor.getColumnIndex("n_type")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setIsEncrypt(cursor.getInt(cursor.getColumnIndex("n_encrypt")));
                note.setCreateTime(cursor.getString(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getString(cursor.getColumnIndex("n_update_time")));
                note.setImgName(cursor.getString(cursor.getColumnIndex("n_imgName")));
                note.setLocalImgPath(cursor.getString(cursor.getColumnIndex("n_imgLocalPath")));
                note.setImgUrl(cursor.getString(cursor.getColumnIndex("n_imgUrl")));
                noteList.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }

    /**
     * 插入笔记
     */
    public long insertNote(final Note note, final BaseListener<Note> listener) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into _Note(n_title,n_content,n_group_id,n_group_name," +
                "n_type,n_bg_color,n_encrypt,n_create_time,n_update_time, n_userId, n_imgName, n_imgLocalPath, n_imgUrl) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

        long ret = 0;
        //sql = "insert into ex_user(eu_login_name,eu_create_time,eu_update_time) values(?,?,?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {
            stat.bindString(1, note.getTitle());
            stat.bindString(2, note.getContent());
            stat.bindLong(3, note.getGroupId());
            stat.bindString(4, note.getGroupName());
            stat.bindLong(5, note.getType());
            stat.bindString(6, note.getBgColor());
            stat.bindLong(7, note.getIsEncrypt());
            stat.bindString(8, DateUtils.date2string(new Date()));
            stat.bindString(9, DateUtils.date2string(new Date()));
            stat.bindString(10, note.getUserId());
            stat.bindString(11, note.getImgName());
            stat.bindString(12, note.getLocalImgPath());
            stat.bindString(13, note.getImgUrl() == null ? "" : note.getImgUrl());
            ret = stat.executeInsert();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        note.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                note.setObjectId(s);
                updateNote(note);
                listener.getSuccess(note);
            }
        });
        return ret;
    }

    /**
     * 更新笔记
     *
     * @param note
     */
    public void updateNote(Note note) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("n_title", note.getTitle());
        values.put("n_objectId", note.getObjectId());
        values.put("n_content", note.getContent());
        values.put("n_group_id", note.getGroupId());
        values.put("n_group_name", note.getGroupName());
        values.put("n_type", note.getType());
        values.put("n_bg_color", note.getBgColor());
        values.put("n_encrypt", note.getIsEncrypt());
        values.put("n_userId", note.getUserId());
        values.put("n_update_time", DateUtils.date2string(new Date()));
        values.put("n_imgName", note.getImgName());
        values.put("n_imgLocalPath", note.getLocalImgPath());
        values.put("n_imgUrl", note.getImgUrl());
        db.update("_Note", values, "n_id=?", new String[]{note.getId() + ""});
        db.close();
        note.update(note.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                Log.d("NoteDao", "update res:" + e);
            }
        });
    }

    /**
     * 删除笔记
     */
    public int deleteNote(Note deleteNote) {
        final Note del = queryNoteById(deleteNote.getId());
        SQLiteDatabase db = helper.getWritableDatabase();
        int ret = 0;
        try {
            ret = db.delete("_Note", "n_id=?", new String[]{deleteNote.getId() + ""});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

        del.delete(del.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                Log.d("NoteDao", "delete res : " + e);
            }
        });
        return ret;
    }

    public Note queryNoteById(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Note note = null;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from _Note where  n_id = '" + id + "'";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                note = new Note();
                note.setObjectId(cursor.getString(cursor.getColumnIndex("n_objectId")));
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setUserId(cursor.getString(cursor.getColumnIndex("n_userId")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setGroupName(cursor.getString(cursor.getColumnIndex("n_group_name")));
                note.setType(cursor.getInt(cursor.getColumnIndex("n_type")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setIsEncrypt(cursor.getInt(cursor.getColumnIndex("n_encrypt")));
                note.setCreateTime(cursor.getString(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getString(cursor.getColumnIndex("n_update_time")));
                note.setImgName(cursor.getString(cursor.getColumnIndex("n_imgName")));
                note.setLocalImgPath(cursor.getString(cursor.getColumnIndex("n_imgLocalPath")));
                note.setImgUrl(cursor.getString(cursor.getColumnIndex("n_imgUrl")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return note;
    }

    /**
     * 根据条件查询笔记
     */
    public List<Note> searchNotes(int groupId, String userId, String title, String detail, String statrtTime, String endTime) {
        SQLiteDatabase db = helper.getWritableDatabase();

        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            if (groupId > 0) {
                sql = "select * from _Note where n_group_id =" + groupId + " and n_userId = '" + userId + "'" +
                        "order by n_create_time desc";
            } else {
                sql = "select * from _Note where  n_userId = '" + userId + "'";
                if (statrtTime != null && statrtTime.length() > 0) {
                    sql += " and n_update_time >= '" + statrtTime + "'";
                }
                if (endTime != null && endTime.length() > 0) {
                    sql += " and n_update_time <= '" + endTime + "'";
                }
                if (detail != null && detail.length() > 0) {
                    sql += " and n_content like '%" + detail + "%'";
                }
                if (title != null && title.length() > 0) {
                    sql += " and n_title like '%" + title + "%'";
                }
            }
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                note = new Note();
                note.setObjectId(cursor.getString(cursor.getColumnIndex("n_objectId")));
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setUserId(cursor.getString(cursor.getColumnIndex("n_userId")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setGroupName(cursor.getString(cursor.getColumnIndex("n_group_name")));
                note.setType(cursor.getInt(cursor.getColumnIndex("n_type")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setIsEncrypt(cursor.getInt(cursor.getColumnIndex("n_encrypt")));
                note.setCreateTime(cursor.getString(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getString(cursor.getColumnIndex("n_update_time")));
                note.setImgName(cursor.getString(cursor.getColumnIndex("n_imgName")));
                note.setLocalImgPath(cursor.getString(cursor.getColumnIndex("n_imgLocalPath")));
                note.setImgUrl(cursor.getString(cursor.getColumnIndex("n_imgUrl")));
                noteList.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }

    public void synchronizationSchedule(String userId, final BaseListener<Exception> listener) {

        dbWrite.delete("_Note", "n_userId = ?" , new String[]{userId});

        BmobQuery<Note> query = new BmobQuery<>();
        query.addWhereEqualTo("userId", userId);
        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if(e == null) {
                    for(Note s :list) {
                        addNote(s);
                    }
                    listener.getSuccess(e);
                }
                else {
                    listener.getFailure(e);
                }
            }
        });
    }

    private void addNote(final Note note) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into _Note(n_title,n_content,n_group_id,n_group_name," +
                "n_type,n_bg_color,n_encrypt,n_create_time, " +
                "n_update_time, n_userId, n_imgName, n_imgLocalPath," +
                " n_imgUrl,n_objectId) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {
            stat.bindString(1, note.getTitle());
            stat.bindString(2, note.getContent());
            stat.bindLong(3, note.getGroupId());
            stat.bindString(4, note.getGroupName());
            stat.bindLong(5, note.getType());
            stat.bindString(6, note.getBgColor());
            stat.bindLong(7, note.getIsEncrypt());
            stat.bindString(8, DateUtils.date2string(new Date()));
            stat.bindString(9, DateUtils.date2string(new Date()));
            stat.bindString(10, note.getUserId());
            stat.bindString(11, note.getImgName());
            stat.bindString(12, note.getLocalImgPath());
            stat.bindString(13, note.getImgUrl() == null ? "" : note.getImgUrl());
            stat.bindString(14, note.getObjectId());
            stat.executeInsert();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}
