package com.example.hecheng.richengben2.Dao;

import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.domin.Schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HeCheng on 2017/3/24.
 */

public interface IScheduleDao {

    /**
     * 查询
     *
     * @param detail ： 内容
     * @param startDate ： 时间范围开始时间
     * @param endDate ： 时间范围结束时间
     * @param userId ： 用户ID
     * @param listener
     */
    public void quSchedule(String detail, String startDate, String endDate, String userId, List<Integer> status, BaseListener<ArrayList<Schedule>> listener);

    /**
     *根据id查询
     * @param id
     * @return
     */
    public void queryScheduleById(String id, BaseListener<Schedule> listener);

    /**
     * 新增日程
     * @param schedule
     * @return
     */
    public void addSchedule(Schedule schedule, BaseListener<String> listener);

    /**
     * 删除日程，其实是更新操作
     */
    public void deleteSchedule(String scheduleId, String reason, BaseListener<Schedule> listener);

    /**
     * 更新日程
     * @param schedule
     */
    public void updateSchedule(String id, Schedule schedule, BaseListener<Schedule> listener);

    public void querySchedulesByUserId(String userId, BaseListener<List<Schedule>> listener);

    public void querySchedulesByUserIdAndCreateDate(String userId, String createDate, BaseListener<List<Schedule>> listener);



}
