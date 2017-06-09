package com.example.hecheng.richengben2.DaoImpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.hecheng.richengben2.DB.DBHelper;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.common.DateUtils;
import com.example.hecheng.richengben2.domin.Note;
import com.example.hecheng.richengben2.domin.Plan;
import com.example.hecheng.richengben2.domin.Schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class PlanDao {
    private DBHelper helper;
    private SQLiteDatabase dbWrite, dbReader;

    public PlanDao(Context context) {
        helper = new DBHelper(context);
        dbWrite = helper.getWritableDatabase();
    }

    /**
     * 查询所有计划
     */
    public List<Plan> queryPlansAll(String userId, int type) {
        SQLiteDatabase db = helper.getWritableDatabase();

        List<Plan> planList = new ArrayList<>();
        Plan plan;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from _Plan where userId = '" + userId + "' and type = " + type +
                    " order by createDate desc";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                //循环获得展品信息
                plan = new Plan();
                plan.setId(cursor.getString(cursor.getColumnIndex("id")));
                plan.setObjectId(cursor.getString(cursor.getColumnIndex("objectId")));
                plan.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                plan.setDetail(cursor.getString(cursor.getColumnIndex("detail")));
                plan.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                plan.setCreateDate(cursor.getString(cursor.getColumnIndex("createDate")));
                plan.setEndTime(cursor.getString(cursor.getColumnIndex("endDate")));
                plan.setParentId(cursor.getString(cursor.getColumnIndex("parentId")));
                plan.setOrder(cursor.getInt(cursor.getColumnIndex("planOrder")));
                plan.setProcess(cursor.getFloat(cursor.getColumnIndex("process")));
                plan.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
                plan.setDelReason(cursor.getString(cursor.getColumnIndex("delReason")));
                plan.setType(cursor.getInt(cursor.getColumnIndex("type")));
                planList.add(plan);
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
        return planList;
    }

    public List<Plan> queryChilePlans(String parentId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        List<Plan> planList = new ArrayList<>();
        Plan plan;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from _Plan where parentId = '" + parentId + "' and type = " + Constants.PLAN_YPE.CHILD.getValue() +
                    " order by planOrder";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                //循环获得展品信息
                plan = new Plan();
                plan.setId(cursor.getString(cursor.getColumnIndex("id")));
                plan.setObjectId(cursor.getString(cursor.getColumnIndex("objectId")));
                plan.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                plan.setDetail(cursor.getString(cursor.getColumnIndex("detail")));
                plan.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                plan.setCreateDate(cursor.getString(cursor.getColumnIndex("createDate")));
                plan.setEndTime(cursor.getString(cursor.getColumnIndex("endDate")));
                plan.setParentId(cursor.getString(cursor.getColumnIndex("parentId")));
                plan.setOrder(cursor.getInt(cursor.getColumnIndex("planOrder")));
                plan.setProcess(cursor.getFloat(cursor.getColumnIndex("process")));
                plan.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
                plan.setDelReason(cursor.getString(cursor.getColumnIndex("delReason")));
                plan.setType(cursor.getInt(cursor.getColumnIndex("type")));
                planList.add(plan);
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
        return planList;
    }

    /**
     * 插入笔记
     */
    public void insertPlan(final Plan plan, final BaseListener<Plan> listener) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", plan.getId());
        values.put("userId", plan.getUserId());
        values.put("detail", plan.getDetail());
        values.put("status", plan.getStatus());
        values.put("createDate", plan.getCreateDate());
        values.put("endDate", plan.getEndTime());
        values.put("parentId", plan.getParentId());
        values.put("planOrder", plan.getOrder());
        values.put("process", plan.getProcess());
        values.put("remark", plan.getRemark());
        values.put("delReason", plan.getDelReason());
        values.put("type", plan.getType());
        db.insert("_Plan", null, values);
        db.close();
        plan.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                Log.d("PlanDao", s);
                plan.setObjectId(s);
                donePlan(plan);
                if(listener != null) {
                    listener.getSuccess(plan);
                }
            }
        });
    }

    public void donePlan(Plan plan) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", plan.getId());
        values.put("objectId", plan.getObjectId());
        values.put("userId", plan.getUserId());
        values.put("detail", plan.getDetail());
        values.put("status", plan.getStatus());
        values.put("createDate", plan.getCreateDate());
        values.put("endDate", plan.getEndTime());
        values.put("parentId", plan.getParentId());
        values.put("planOrder", plan.getOrder());
        values.put("process", plan.getProcess());
        values.put("remark", plan.getRemark());
        values.put("delReason", plan.getDelReason());
        values.put("type", plan.getType());
        db.update("_Plan", values, "id=?", new String[]{plan.getId() + ""});
        db.close();
        plan.update(plan.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                Log.d("PlanDao","");
            }
        });
    }

    private void addPlan(final Plan plan) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", plan.getId());
        values.put("userId", plan.getUserId());
        values.put("detail", plan.getDetail());
        values.put("status", plan.getStatus());
        values.put("createDate", plan.getCreateDate());
        values.put("endDate", plan.getEndTime());
        values.put("parentId", plan.getParentId());
        values.put("planOrder", plan.getOrder());
        values.put("process", plan.getProcess());
        values.put("remark", plan.getRemark());
        values.put("delReason", plan.getDelReason());
        values.put("type", plan.getType());
        values.put("objectId", plan.getObjectId());
        db.insert("_Plan", null, values);
        db.close();
    }

    public void synchronizationPlan(String userId, final BaseListener<Exception> listener) {

        dbWrite.delete("_Plan", "userId = ?" , new String[]{userId});

        BmobQuery<Plan> query = new BmobQuery<>();
        query.addWhereEqualTo("userId", userId);
        query.findObjects(new FindListener<Plan>() {
            @Override
            public void done(List<Plan> list, BmobException e) {
                if(e == null) {
                    for(Plan s :list) {
                        addPlan(s);
                    }
                    listener.getSuccess(e);
                }
                else {
                    listener.getFailure(e);
                }
            }
        });
    }
}