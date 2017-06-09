package com.example.hecheng.richengben2.domin;

import cn.bmob.v3.BmobObject;

/**
 * Created by HeCheng on 2017/3/23.
 */

public class Schedule extends BmobObject {
    private String userId;
    private String createDate;        //创建日期
    private String startTime;       //开始时间
    private String endTime;          //截至时间
    private String detail;
    private int status;
    private String reason; //删除或修改的理由
    private int color;
    private boolean isNotice;
    private String remark;  //备注
    private String doneDate; //完成时间

    public Schedule(){
        super();
    }

    public Schedule(String userId, String createDate, String startTime, String endTime, String detail, int status, String reason, int color, boolean isNotice, String remark) {
        this.userId = userId;
        this.createDate = createDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.detail = detail;
        this.status = status;
        this.reason = reason;
        this.color = color;
        this.isNotice = isNotice;
        this.remark = remark;
    }

    public String getDoneDate() {
        return doneDate;
    }

    public void setDoneDate(String doneDate) {
        this.doneDate = doneDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isNotice() {
        return isNotice;
    }

    public void setNotice(boolean notice) {
        isNotice = notice;
    }

    @Override
    public String toString() {
        return this.getObjectId() +" "+ this.userId +" "+ this.detail + " "+this.startTime + "-->" + this.endTime + "  "+ this.color + "  " + this.remark + "  " + this.isNotice();
    }
}