package com.example.hecheng.richengben2.DaoImpl;

import android.content.Context;
import android.util.Log;

import com.example.hecheng.richengben2.DB.DBHelper;
import com.example.hecheng.richengben2.Dao.IScheduleDao;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.domin.Schedule;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by HeCheng on 2017/3/24.
 */

public class ScheduleDao implements IScheduleDao{

    private static  final String TAG = "ScheduleDao";
    private final ScheduleSDao sdao;

    public ScheduleDao(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        this.sdao = new ScheduleSDao(dbHelper);
    }

    @Override
    public void quSchedule(String detail, String startDate, String endDate, String userId, List<Integer> status, final BaseListener<ArrayList<Schedule>> listener) {
        BmobQuery<Schedule> query1 = new BmobQuery<Schedule>();
        BmobQuery<Schedule> query2 = new BmobQuery<Schedule>();
        BmobQuery<Schedule> query3 = new BmobQuery<Schedule>();
        BmobQuery<Schedule> query5 = new BmobQuery<Schedule>();

        query1.addWhereEqualTo("userId", userId);
        if(startDate != null && !"".equals(startDate)) {
            query2.addWhereGreaterThanOrEqualTo("createDate", startDate);
        }
        if(endDate != null && !"".equals(endDate)) {
            query3.addWhereLessThanOrEqualTo("createDate", endDate);
        }

        query5.addWhereContainedIn("status", status);

        List<BmobQuery<Schedule>> querys = new ArrayList<BmobQuery<Schedule>>();
        querys.add(query1);
        querys.add(query2);
        querys.add(query3);
        querys.add(query5);

        BmobQuery <Schedule> finalQuery = new BmobQuery<Schedule>();
        finalQuery.and(querys);
        finalQuery.order("createDate");

        finalQuery.findObjects(new FindListener<Schedule>() {
            @Override
            public void done(List<Schedule> list, BmobException e) {
                if(e == null) {
                    Log.d(TAG, "query total:" + list.size());
                    ArrayList<Schedule> res = new ArrayList<Schedule>();
                    res.addAll(list);
                    listener.getSuccess(res);
                }
                else{
                    e.printStackTrace();
                    listener.getFailure(e);
                }
            }
        });

    }

    @Override
    public void queryScheduleById(String id, final BaseListener<Schedule> listener) {
        BmobQuery<Schedule> query = new BmobQuery<Schedule>();
        query.getObject(id, new QueryListener<Schedule>() {
            @Override
            public void done(Schedule schedule, BmobException e) {
                if(e == null){
                    listener.getSuccess(schedule);
                }
                else {
                    e.printStackTrace();
                    listener.getFailure(e);
                }
            }
        });
    }

    @Override
    public void addSchedule(final Schedule schedule, final BaseListener<String> listener) {
        Log.d(TAG , "add Schedule:" + schedule.toString());
        schedule.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null) {
                    Log.d(TAG, "return id："+s);
                    schedule.setObjectId(s);
                    sdao.addSchedule(schedule);
                    listener.getSuccess(s);
                }
                else{
                    e.printStackTrace();
                    listener.getFailure(e);
                }
            }
        });
    }

    @Override
    public void deleteSchedule(String scheduleId, String reason, BaseListener<Schedule> listener) {

    }

    @Override
    public void updateSchedule(String id, Schedule schedule, BaseListener<Schedule> listener) {

    }

    @Override
    public void querySchedulesByUserId(String userId, BaseListener<List<Schedule>> listener) {
        BmobQuery<Schedule> query = new BmobQuery<Schedule>();
        query.addWhereEqualTo("userId", userId);


    }

    @Override
    public void querySchedulesByUserIdAndCreateDate(String userId, String createDate, final BaseListener<List<Schedule>> listener) {
        BmobQuery<Schedule> query1 = new BmobQuery<Schedule>();
        query1.addWhereEqualTo("userId", userId);
        BmobQuery<Schedule> query2 = new BmobQuery<Schedule>();
        query2.addWhereEqualTo("createDate", createDate);

        List<BmobQuery<Schedule>> querys = new ArrayList<BmobQuery<Schedule>>();
        querys.add(query1);
        querys.add(query2);

        BmobQuery <Schedule> finalQuery = new BmobQuery<Schedule>();
        finalQuery.and(querys);
        finalQuery.order("status");
        finalQuery.findObjects(new FindListener<Schedule>() {
            @Override
            public void done(List<Schedule> list, BmobException e) {
                if(e == null) {
                    Log.d(TAG, "query by userId and date:" + list.size());
                    listener.getSuccess(list);
                }
                else{
                    e.printStackTrace();
                    listener.getFailure(e);
                }
            }
        });
    }
}
