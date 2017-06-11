package com.example.hecheng.richengben2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hecheng.richengben2.DB.DBHelper;
import com.example.hecheng.richengben2.DaoImpl.NoteDao;
import com.example.hecheng.richengben2.DaoImpl.PlanDao;
import com.example.hecheng.richengben2.DaoImpl.ScheduleDao;
import com.example.hecheng.richengben2.DaoImpl.ScheduleSDao;
import com.example.hecheng.richengben2.common.DateUtil;
import com.example.hecheng.richengben2.View.SearchDialog;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.common.ProgressDialogUtil;
import com.example.hecheng.richengben2.domin.Schedule;
import com.example.hecheng.richengben2.domin.User;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

/**
 * 程序住界面，显示一个日历
 */
public class DateAcitvity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener, OnMonthChangedListener {

    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();

    @BindView(R.id.calendarView)
    MaterialCalendarView widget;
    @BindView(R.id.fab_search)
    FloatingActionButton fabSearch;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_acitvity2);
        ButterKnife.bind(this);
        Bmob.initialize(this, Constants.APP_ID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("主页");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchSchedule(view);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_date_acitvity2);
        TextView headerName = (TextView) headerView.findViewById(R.id.nav_name);

        Intent intent = this.getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user != null) {
            headerName.setText(user.getUsername());
        }

        navigationView.setNavigationItemSelectedListener(this);

        widget.setOnDateChangedListener(this);
        widget.setOnMonthChangedListener(this);
        widget.setSelectedDate(new Date());

    }

    private void searchSchedule(View view) {
        //显示搜索对话框
        final SearchDialog dialog = new SearchDialog(DateAcitvity.this, R.style.SearchDialog, user);
        dialog.setSearchBtnClickListener(new SearchDialog.OnSearchBtnClickListener() {
            @Override
            public void onSearchBtClick() {
                final String startDate = dialog.getStartDate();
                final String endDate = dialog.getEndDate();
                final String detail = dialog.getDetail();
                final boolean statusDel = dialog.isStatusDel();
                final boolean statusWait = dialog.isStatusWait();
                final boolean statusDone = dialog.isStatusDone();
                TextView tvEndDate = dialog.getTvEndDate();
                if(startDate.length() > 0 && endDate.length() > 0 && startDate.compareTo(endDate) > 0) {
                    tvEndDate.setError("日期选择错误！");
                    return ;
                }
                dialog.dismiss();

                final ProgressDialog processDialog = ProgressDialogUtil.showProcessDialog("搜索中。。。", DateAcitvity.this);
                processDialog.show();

                List<Integer> status = new ArrayList<Integer>();
                if(statusDel){
                    status.add(Constants.SCHEDULE_STATUS.CANCLE.getValue());
                }
                if(statusWait){
                    status.add(Constants.SCHEDULE_STATUS.WAIT.getValue());
                }
                if(statusDone){
                    status.add(Constants.SCHEDULE_STATUS.DONE.getValue());
                }

                Log.d("search", detail + "-" + startDate + "-" + endDate +"-" + user.getId());
                ScheduleDao dao = new ScheduleDao(getApplicationContext());
                dao.quSchedule(detail, startDate, endDate, user.getId(), status, new BaseListener<ArrayList<Schedule>>() {
                    @Override
                    public void getSuccess(ArrayList<Schedule> schedules) {
                        processDialog.dismiss();
                        if(schedules.size() == 0 ){
                            Toast.makeText(getApplicationContext(), "查询结果为空！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else {
                            ArrayList<Schedule> res = new ArrayList<Schedule>();
                            if(detail != null && detail.length() > 0) {
                                for(Schedule s : schedules) {
                                    String s_detail = s.getDetail();
                                    if(s_detail.contains(detail)) {
                                        res.add(s);
                                    }
                                }
                            }
                            else {
                                res = schedules;
                            }

                            Intent i = new Intent(getApplicationContext(), ScheduleActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("user", user);
                            bundle.putSerializable("isSearch", true);
                            bundle.putSerializable("schedules", res);
                            i.putExtras(bundle);
                            getApplicationContext().startActivity(i);
                        }
                    }

                    @Override
                    public void getFailure(Exception e) {

                    }
                });
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.date_acitvity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) { //帮助栏
            Intent i = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_schedule) {
            String dateString = getSelectedDatesString();
            dateString = dateString.replace("年","-");
            dateString = dateString.replace("月", "-");
            dateString = dateString.replace("日","");
            Intent i = new Intent(getApplicationContext(), ScheduleActivity.class);
            Bundle bundle = new Bundle();
            Date d = DateUtil.parseToDate(dateString);
            bundle.putSerializable("isSearch", false);
            bundle.putSerializable("date", d);
            bundle.putSerializable("user", user);
            i.putExtras(bundle);
            startActivity(i);
        } else if (id == R.id.nav_plan) {  //计划管理
            Intent intent = new Intent(getApplicationContext(), PlanActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", user);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if(id == R.id.nav_note) { //笔记管理
            Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", user);
            intent.putExtras(bundle);
            startActivity(intent);
        }else if (id == R.id.nav_refresh) { //同步操作
            final ProgressDialog progressDialog = ProgressDialogUtil.showProcessDialog("同步中，请稍后。。。", DateAcitvity.this);
            progressDialog.show();
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            ScheduleSDao sdo = new ScheduleSDao(dbHelper);
            sdo.synchronizationSchedule(user.getId(), new BaseListener<Exception>() {
                @Override
                public void getSuccess(Exception e) {

                    NoteDao noteDao = new NoteDao(getApplicationContext());
                    noteDao.synchronizationSchedule(user.getId(), new BaseListener<Exception>() {
                        @Override
                        public void getSuccess(Exception e) {
                            PlanDao planDao = new PlanDao(getApplicationContext());
                            planDao.synchronizationPlan(user.getId(), new BaseListener<Exception>() {
                                @Override
                                public void getSuccess(Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "同步完成", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void getFailure(Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "同步失败，请稍后再试！", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void getFailure(Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "同步失败，请稍后再试！", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void getFailure(Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "同步失败，请稍后再试！", Toast.LENGTH_LONG).show();
                }
            });
        }else if (id == R.id.nav_logout) {
            BmobUser.logOut();

            SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isAutoLogin", false);
            editor.commit();

            Context context = getApplicationContext();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @Nullable CalendarDay date, boolean selected) {
        String dateString = getSelectedDatesString();
        dateString = dateString.replace("年","-");
        dateString = dateString.replace("月", "-");
        dateString = dateString.replace("日","");
        Intent i = new Intent(getApplicationContext(), ScheduleActivity.class);
        Bundle bundle = new Bundle();
        Date d = DateUtil.parseToDate(dateString);
        bundle.putSerializable("isSearch", false);
        bundle.putSerializable("date", d);
        bundle.putSerializable("user", user);
        i.putExtras(bundle);
        startActivity(i);
    }

    private String getSelectedDatesString() {
        CalendarDay date = widget.getSelectedDate();
        if (date == null) {
            return "No Selection";
        }
        return FORMATTER.format(date.getDate());
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

    }
}
