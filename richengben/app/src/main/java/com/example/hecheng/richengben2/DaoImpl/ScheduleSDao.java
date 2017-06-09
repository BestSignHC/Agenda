package com.example.hecheng.richengben2.DaoImpl;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.hecheng.richengben2.DB.DBHelper;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.domin.Schedule;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 *
 * Created by HeCheng on 2017/4/22.
 */

public class ScheduleSDao {

    private SQLiteDatabase dbWrite, dbReader;

    public ScheduleSDao(DBHelper dbHelper){
        dbReader = dbHelper.getReadableDatabase();
        dbWrite = dbHelper.getWritableDatabase();
    }

    public void addSchedule(Schedule schedule) {
        String userId = schedule.getUserId();
        String createDate = schedule.getCreateDate();        //创建日期
        String startTime = schedule.getStartTime();       //开始时间
        String endTime = schedule.getEndTime();          //截至时间
        String detail = schedule.getDetail();
        int status = schedule.getStatus();
        String reason = schedule.getReason(); //删除或修改的理由
        int color = schedule.getColor();
        boolean isNotice = schedule.isNotice();
        String remark = schedule.getRemark();  //备注
        String doneDate = schedule.getDoneDate(); //完成时间

        ContentValues values = new ContentValues();
        values.put("objectId", schedule.getObjectId());
        values.put("userId", userId);
        values.put("detail", detail);
        values.put("remark", remark);
        values.put("startTime", startTime);
        values.put("endTime", endTime);
        values.put("status", status);
        values.put("color", color);
        values.put("createDate", createDate);
        values.put("doneDate", doneDate);
        values.put("reason", reason);
        values.put("isNotice", isNotice ? 1 : 0);

        dbWrite.insert("_Schedule", null, values);
    }

    public void updateSchedule(Schedule schedule){
        String scheduleId = schedule.getObjectId();
        String userId = schedule.getUserId();
        String createDate = schedule.getCreateDate();        //创建日期
        String startTime = schedule.getStartTime();       //开始时间
        String endTime = schedule.getEndTime();          //截至时间
        String detail = schedule.getDetail();
        int status = schedule.getStatus();
        String reason = schedule.getReason(); //删除或修改的理由
        int color = schedule.getColor();
        boolean isNotice = schedule.isNotice();
        String remark = schedule.getRemark();  //备注
        String doneDate = schedule.getDoneDate(); //完成时间

        ContentValues values = new ContentValues();
        values.put("objectId", schedule.getObjectId());
        values.put("userId", userId);
        values.put("detail", detail);
        values.put("remark", remark);
        values.put("startTime", startTime);
        values.put("endTime", endTime);
        values.put("status", status);
        values.put("color", color);
        values.put("createDate", createDate);
        values.put("doneDate", doneDate);
        values.put("reason", reason);
        values.put("isNotice", isNotice ? 1 : 0);
        dbWrite.update("_Schedule", values, "objectId = ?", new String[]{scheduleId});
    }

    public void synchronizationSchedule(String userId, final BaseListener<Exception> listener) {

        dbWrite.delete("_Schedule", "userId = ?" , new String[]{userId});

        BmobQuery<Schedule> query = new BmobQuery<>();
        query.addWhereEqualTo("userId", userId);
        query.findObjects(new FindListener<Schedule>() {
            @Override
            public void done(List<Schedule> list, BmobException e) {
                if(e == null) {
                    for(Schedule s :list) {
                        addSchedule(s);
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
