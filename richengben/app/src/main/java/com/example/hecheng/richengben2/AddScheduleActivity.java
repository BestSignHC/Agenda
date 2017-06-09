package com.example.hecheng.richengben2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.hecheng.richengben2.DB.DBHelper;
import com.example.hecheng.richengben2.DaoImpl.ScheduleDao;
import com.example.hecheng.richengben2.DaoImpl.ScheduleSDao;
import com.example.hecheng.richengben2.common.DateUtil;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.domin.Schedule;
import com.example.hecheng.richengben2.domin.User;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.example.hecheng.richengben2.common.Constants.COLOR_BLACK;
import static com.example.hecheng.richengben2.common.Constants.COLOR_BLUE;
import static com.example.hecheng.richengben2.common.Constants.COLOR_GREEN;
import static com.example.hecheng.richengben2.common.Constants.COLOR_RED;

public class AddScheduleActivity extends AppCompatActivity {

    @BindView(R.id.scheduleAddClose)
    ImageButton btnCloseAddWindow;
    @BindView(R.id.schedule_add_approve)
    ImageButton btnProveAdd;
    @BindView(R.id.schedule_add_color_1)
    Button btnAddType1;
    @BindView(R.id.schedule_add_color_2)
    Button btnAddType2;
    @BindView(R.id.schedule_add_color_3)
    Button btnAddType3;
    @BindView(R.id.schedule_add_color_4)
    Button btnAddType4;
    @BindView(R.id.schedule_detail)
    EditText etScheduleDetail;
    @BindView(R.id.schedule_startTime)
    TextView tvScheduleStartTime;
    @BindView(R.id.schedule_endTime)
    TextView tvScheduleEndTime;
    @BindView(R.id.schedule_remark)
    EditText etScheduleRemark;
    @BindView(R.id.is_notice)
    CheckBox cbIsNotice;
    @BindView(R.id.doneTimeLabel)
    TextView tvDoneTimeLable;
    @BindView(R.id.doneTime)
    TextView tvDoneTime;

    private String scheduleRemark = "";
    private String scheduleDetail = "";
    private String startTime;
    private String endTime;
    private boolean isNotice;

    private User user;
    private Date createDate;
    private Schedule updateSchedule;

    private int COLOR_TYPE = COLOR_BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_schedule_layou);
        ButterKnife.bind(this);
        Bmob.initialize(this, Constants.APP_ID);

        Intent intent = this.getIntent();
        user = (User) intent.getSerializableExtra("user");
        createDate = (Date) intent.getSerializableExtra("date");
        updateSchedule = (Schedule) intent.getSerializableExtra("schedule");

        if (updateSchedule != null) {
            showScheduleData(updateSchedule);
            if(updateSchedule.getStatus() != Constants.SCHEDULE_STATUS.WAIT.getValue()) {
                btnProveAdd.setVisibility(View.INVISIBLE);
            }
        }
    }

    @OnClick(R.id.schedule_add_approve)
    public void doAddSchedule() {

        if (etScheduleDetail.getText() != null) {
            scheduleDetail = etScheduleDetail.getText().toString();
        }
        if(scheduleDetail == null || scheduleDetail.length() == 0) {
            Toast.makeText(AddScheduleActivity.this, "日程内容不能为空", Toast.LENGTH_SHORT).show();
        }

        if (updateSchedule != null && (startTime == null || startTime.length() < 1)) {
            //更新
            startTime = updateSchedule.getStartTime();
        }

        if (updateSchedule != null && (endTime == null || endTime.length() < 1)) {
            //更新
            endTime = updateSchedule.getEndTime();
        }

        //新增 日程
        if (startTime == null || startTime.length() < 1 || endTime == null || endTime.length() < 1 || startTime.compareTo(endTime) > 0) {
            Toast.makeText(AddScheduleActivity.this, "开始或结束时间选择错误", Toast.LENGTH_SHORT).show();
            return;
        }

        Date nowDate2 = new Date();
        String nowDateString2 = DateUtil.getDateMinString(nowDate2);
        String startString = DateUtil.getDateDayString(createDate) + " " + startTime;

        if (startString.compareTo(nowDateString2) < 0) {
            Toast.makeText(AddScheduleActivity.this, "开始时间选择错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etScheduleRemark.getText() != null) {
            scheduleRemark = etScheduleRemark.getText().toString();
        }

        isNotice = cbIsNotice.isChecked();

        String userId = user.getId();
        int status = Constants.SCHEDULE_STATUS.WAIT.getValue();

        if (updateSchedule == null) { //新建
            final Schedule schedule = new Schedule(userId, DateUtil.getDateDayString(createDate), startTime, endTime, scheduleDetail, status, "", COLOR_TYPE, isNotice, scheduleRemark);
            Log.d("add", "start add schedule...");
            ScheduleDao scheduleDao = new ScheduleDao(getApplicationContext());
            scheduleDao.addSchedule(schedule, new BaseListener<String>() {
                @Override
                public void getSuccess(String s) {
                    if (schedule.isNotice()) {
                        String dateString = DateUtil.getDateDayString(createDate) + " " + schedule.getStartTime();
                        Date startDate = DateUtil.parseToTime(dateString);
                        setNotifacationAlarm(startDate, "您有新的日程，请点击查看！", dateString + "   " + schedule.getDetail(), s);
                    }
                    Toast.makeText(getApplicationContext(), "新增日程成功", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void getFailure(Exception e) {
                    Toast.makeText(getApplicationContext(), "新增日程失败", Toast.LENGTH_SHORT).show();
                }
            });
        } else { //更新

            final Schedule newSchedule = new Schedule();

            newSchedule.setRemark(scheduleRemark);
            newSchedule.setColor(COLOR_TYPE);
            newSchedule.setCreateDate(updateSchedule.getCreateDate());
            newSchedule.setStartTime(startTime);
            newSchedule.setEndTime(endTime);
            newSchedule.setDetail(scheduleDetail);
            newSchedule.setNotice(isNotice);
            final String objectId = updateSchedule.getObjectId();
            newSchedule.setObjectId(objectId);
            newSchedule.setCreateDate(updateSchedule.getCreateDate());
            newSchedule.setUserId(updateSchedule.getUserId());
            newSchedule.setStatus(updateSchedule.getStatus());

            Log.d("update", "update schedule : " + newSchedule.toString());
            newSchedule.update(objectId, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        DBHelper dbHelper = new DBHelper(getApplicationContext());
                        ScheduleSDao sdao = new ScheduleSDao(dbHelper);
                        sdao.updateSchedule(newSchedule);
                        Toast.makeText(getApplicationContext(), "更新成功。", Toast.LENGTH_SHORT).show();
                        if (isNotice) {
                            String dateString = DateUtil.getDateDayString(createDate) + " " + startTime;
                            Date startDate = DateUtil.parseToTime(dateString);
                            setNotifacationAlarm(startDate, "您有新的日程，请点击查看！", dateString + "   " + scheduleDetail, objectId);
                        }
                        finish();
                    } else {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "更新失败，请稍后再试。", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }

    }

    @OnClick(R.id.schedule_startTime)
    public void setStartTime() {
        Date now = new Date();
        int hour = now.getHours();
        int min = now.getMinutes();
        TimePickerDialog timePicker = new TimePickerDialog(AddScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                startTime = String.format("%02d:%02d", hourOfDay, minute);
                tvScheduleStartTime.setText(startTime);
            }
            //0,0指的是时间，true表示是否为24小时，true为24小时制
        }, hour, min, true);
        timePicker.setTitle("开始时间");
        timePicker.show();
    }

    @OnClick(R.id.schedule_endTime)
    public void setEndTime() {
        Date now = new Date();
        final int hour = now.getHours();
        final int min = now.getMinutes();

        TimePickerDialog timePicker = new TimePickerDialog(AddScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                endTime = String.format("%02d:%02d", hourOfDay, minute);
                tvScheduleEndTime.setText(endTime);
            }
            //0,0指的是时间，true表示是否为24小时，true为24小时制
        }, hour, min, true);
        timePicker.setTitle("结束时间");
        timePicker.show();
    }

    @OnClick(R.id.scheduleAddClose)
    public void closeAddWindow() {
        finish();
    }

    @OnClick(R.id.schedule_add_color_1)
    public void addScheduleType1() {
        COLOR_TYPE = COLOR_RED;
        btnAddType1.setText(Constants.textMap.get(COLOR_TYPE));
        btnAddType1.setWidth(btnAddType1.getWidth() + 5);
        btnAddType1.setHeight(btnAddType1.getHeight() + 5);
        btnAddType1.setTextColor(Color.parseColor(Constants.colorMap.get(Constants.COLOR_WHITE)));

        btnAddType2.setText("");
        btnAddType3.setText("");
        btnAddType4.setText("");
        etScheduleDetail.setTextColor(Color.RED);
    }

    @OnClick(R.id.schedule_add_color_2)
    public void addScheduleType2() {
        COLOR_TYPE = COLOR_GREEN;
        btnAddType2.setText(Constants.textMap.get(COLOR_TYPE));
        btnAddType2.setTextColor(Color.parseColor(Constants.colorMap.get(Constants.COLOR_WHITE)));

        btnAddType1.setText("");
        btnAddType3.setText("");
        btnAddType4.setText("");
        etScheduleDetail.setTextColor(Color.GREEN);
    }

    @OnClick(R.id.schedule_add_color_3)
    public void addScheduleType3() {
        COLOR_TYPE = COLOR_BLUE;
        btnAddType3.setText(Constants.textMap.get(COLOR_TYPE));
        btnAddType3.setTextColor(Color.parseColor(Constants.colorMap.get(Constants.COLOR_WHITE)));

        btnAddType2.setText("");
        btnAddType1.setText("");
        btnAddType4.setText("");
        etScheduleDetail.setTextColor(Color.BLUE);
    }

    @OnClick(R.id.schedule_add_color_4)
    public void addScheduleType4() {
        COLOR_TYPE = COLOR_BLACK;
        btnAddType4.setText(Constants.textMap.get(COLOR_TYPE));
        btnAddType4.setTextColor(Color.parseColor(Constants.colorMap.get(Constants.COLOR_WHITE)));

        btnAddType2.setText("");
        btnAddType3.setText("");
        btnAddType1.setText("");
        etScheduleDetail.setTextColor(Color.BLACK);
    }

    private void showScheduleData(Schedule schedule) {
        if (schedule.getStatus() != Constants.SCHEDULE_STATUS.WAIT.getValue()) { //查看详情 设置所有控件不可点击
            btnAddType1.setEnabled(false);
            btnAddType2.setEnabled(false);
            btnAddType3.setEnabled(false);
            btnAddType4.setEnabled(false);
            etScheduleDetail.setEnabled(false);
            etScheduleRemark.setEnabled(false);
            tvScheduleStartTime.setEnabled(false);
            tvScheduleEndTime.setEnabled(false);
            cbIsNotice.setEnabled(false);
            btnCloseAddWindow.setEnabled(true);
            btnProveAdd.setVisibility(View.INVISIBLE);

            if (schedule.getStatus() == Constants.SCHEDULE_STATUS.DONE.getValue()) { //显示完成时间
                tvDoneTimeLable.setVisibility(View.VISIBLE);
                tvDoneTime.setVisibility(View.VISIBLE);
                tvDoneTime.setText(schedule.getDoneDate());
            } else { //显示删除理由
                tvDoneTimeLable.setVisibility(View.VISIBLE);
                tvDoneTimeLable.setText("删除理由");
                tvDoneTime.setVisibility(View.VISIBLE);
                tvDoneTime.setText(schedule.getReason());
                tvDoneTime.setTextColor(Color.RED);
            }

        }
        COLOR_TYPE = schedule.getColor();
        switch (COLOR_TYPE) {
            case 1:
                btnAddType1.setText(Constants.textMap.get(COLOR_TYPE));
                btnAddType1.setTextColor(Color.parseColor(Constants.colorMap.get(Constants.COLOR_WHITE)));
                break;
            case 2:
                btnAddType2.setText(Constants.textMap.get(COLOR_TYPE));
                btnAddType2.setTextColor(Color.parseColor(Constants.colorMap.get(Constants.COLOR_WHITE)));
                break;
            case 3:
                btnAddType3.setText(Constants.textMap.get(COLOR_TYPE));
                btnAddType3.setTextColor(Color.parseColor(Constants.colorMap.get(Constants.COLOR_WHITE)));
                break;
            case 4:
                btnAddType4.setText(Constants.textMap.get(COLOR_TYPE));
                btnAddType4.setTextColor(Color.parseColor(Constants.colorMap.get(Constants.COLOR_WHITE)));
                break;
        }

        String startTime = schedule.getStartTime();
        tvScheduleStartTime.setText(startTime);

        String endTime = schedule.getEndTime();
        tvScheduleEndTime.setText(endTime);

        String detail = schedule.getDetail();
        etScheduleDetail.setText(detail);

        String remark = schedule.getRemark();
        etScheduleRemark.setText(remark);

        boolean isNotice = schedule.isNotice();
        cbIsNotice.setChecked(isNotice);
    }

    public void setNotifacationAlarm(final Date startDate, final String title, final String detail, final String scheduleId) {
        AlarmManager manger = (AlarmManager) getApplication().getSystemService(Service.ALARM_SERVICE);
        Intent intent = new Intent("com.example.hecheng.richengben2.service.noticeService");
        Bundle b = new Bundle();
        b.putSerializable("title", title);
        b.putSerializable("detail", detail);
        b.putSerializable("scheduleId", scheduleId);
        intent.putExtras(b);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getService(AddScheduleActivity.this, scheduleId.hashCode(), intent, PendingIntent.FLAG_ONE_SHOT);
        manger.set(AlarmManager.RTC_WAKEUP, startDate.getTime(), pi);
    }
}