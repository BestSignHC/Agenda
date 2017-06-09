package com.example.hecheng.richengben2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hecheng.richengben2.Adapter.PlanAdapter;
import com.example.hecheng.richengben2.DaoImpl.PlanDao;
import com.example.hecheng.richengben2.common.DateUtil;
import com.example.hecheng.richengben2.View.TreeView;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.domin.Plan;
import com.example.hecheng.richengben2.domin.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;

/**
 * Created by HeCheng on 2017/3/22.
 */

public class PlanActivity2 extends BaseActivity {

    private User user = null;
    private String userId;
    private TreeView treeView;
    private LayoutInflater mInflater;
    private PlanAdapter adapter;
    private Toolbar toolBar;

    private Map<Plan, List<Plan>> dataMap = new HashMap<>();
    private Map<Plan, List<Plan>> dataMapWeek = new HashMap<>();
    private Map<Plan, List<Plan>> dataMapMonth = new HashMap<>();
    private List<Plan> parentList = new ArrayList<Plan>();

    private String detail = "";
    private String remark = "";
    private String endDate = "";
    private boolean isWeekPlan = true;
    private boolean isMonthPlan = false;
    private int showType = Constants.PLAN_YPE.WEEK.getValue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plan_layout);
        ButterKnife.bind(this);
        Bmob.initialize(this, Constants.APP_ID);
        //获取当前用户
        user = (User) this.getIntent().getSerializableExtra("user");
        userId = user.getId();

        initData(showType);
        initView();
    }

    private void initData(int showType) {
        dataMapWeek.clear();
        dataMapMonth.clear();
        if (showType == Constants.PLAN_YPE.WEEK.getValue()) {
            final PlanDao dao = new PlanDao(getApplicationContext());
            List<Plan> plans = dao.queryPlansAll(userId, showType);
            for (Plan p : plans) {
                List<Plan> childPlans = dao.queryChilePlans(p.getId());
                dataMapWeek.put(p, childPlans);
            }
            dataMap = dataMapWeek;
        } else {
            final PlanDao dao = new PlanDao(getApplicationContext());
            List<Plan> plans = dao.queryPlansAll(userId, showType);
            for (Plan p : plans) {
                List<Plan> childPlans = dao.queryChilePlans(p.getId());
                dataMapMonth.put(p, childPlans);
            }
            dataMap = dataMapMonth;
        }

    }

    private void initView() {
        toolBar = (Toolbar) findViewById(R.id.plan_layout_toolbar);
        setSupportActionBar(toolBar);
        setTitle("计划管理");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mInflater = LayoutInflater.from(this);
        treeView = (TreeView) findViewById(R.id.plan_treeView);
        treeView.setHeaderView(getLayoutInflater().inflate(R.layout.plan_list_head_view, treeView, false));
        treeView.setGroupIndicator(null);

        setAdapter(dataMap);

    }

    @OnClick(R.id.btn_add_plan)
    public void addPlan() {
        View viewAdd = this.getLayoutInflater().inflate(R.layout.add_plan_dialog_layout, null);
        final Plan newPlan = new Plan();
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("添加计划");
        dialog.setContentView(viewAdd);
        dialog.show();

        ImageButton btnAdd = (ImageButton) viewAdd.findViewById(R.id.plan_add_prove);
        ImageButton btnClose = (ImageButton) viewAdd.findViewById(R.id.plan_add_close);
        final EditText etDetail = (EditText) viewAdd.findViewById(R.id.plan_detail);
        final CheckBox cbWeekPlan = (CheckBox) viewAdd.findViewById(R.id.week_plan);
        final CheckBox cbMonthPlan = (CheckBox) viewAdd.findViewById(R.id.month_plan);

        cbWeekPlan.setChecked(isWeekPlan);
        cbMonthPlan.setChecked(isMonthPlan);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        cbWeekPlan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isWeekPlan = isChecked;
                isMonthPlan = !isWeekPlan;
                cbWeekPlan.setChecked(isWeekPlan);
                cbMonthPlan.setChecked(isMonthPlan);
            }
        });

        cbMonthPlan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMonthPlan = isChecked;
                isWeekPlan = !isMonthPlan;
                cbWeekPlan.setChecked(isWeekPlan);
                cbMonthPlan.setChecked(isMonthPlan);
            }
        });

        etDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detail = etDetail.getText() == null ? "" : etDetail.getText().toString();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                endDate = getEndDate(isWeekPlan);

                newPlan.setUserId(userId);
                newPlan.setId(java.util.UUID.randomUUID().toString());
                newPlan.setCreateDate(DateUtil.getDateDayString(new Date()));
                newPlan.setDetail(detail);
                newPlan.setEndTime(endDate);
                newPlan.setOrder(0);
                newPlan.setProcess(0.0f);
                if (isWeekPlan) {
                    newPlan.setType(Constants.PLAN_YPE.WEEK.getValue());
                } else {
                    newPlan.setType(Constants.PLAN_YPE.MONTH.getValue());
                }
                newPlan.setStatus(Constants.PLAN_STATUS.WAIT.getValue());
                doAddPlan(null, newPlan);
                dialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.plan_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView tvText = (TextView) toolBar.findViewById(R.id.switch_plan_text);
        switch (item.getItemId()) {
            case R.id.switch_plan:
                if (showType == Constants.PLAN_YPE.WEEK.getValue()) {
                    item.setIcon(R.mipmap.plan_month);
                    showType = Constants.PLAN_YPE.MONTH.getValue();
                    tvText.setText("月计划");
                    initData(showType);
                    setAdapter(dataMap);
                } else {
                    item.setIcon(R.mipmap.plan_week);
                    showType = Constants.PLAN_YPE.WEEK.getValue();
                    tvText.setText("周计划");
                    initData(showType);
                    setAdapter(dataMap);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getEndDate(boolean isWeekPlan) {
        Calendar now = Calendar.getInstance();
        if (isWeekPlan) {
            now.add(Calendar.DAY_OF_MONTH, 7);
        } else {
            now.add(Calendar.MONTH, 1);
        }
        Date endDate = now.getTime();
        return DateUtil.getDateDayString(endDate);
    }

    private void doAddPlan(final Plan parentPlan,final Plan newPlan) {
        final PlanDao dao = new PlanDao(PlanActivity2.this);
        if (newPlan != null) {
            dao.insertPlan(newPlan, new BaseListener<Plan>() {
                @Override
                public void getSuccess(Plan plan) {
                    if (parentPlan == null) {
                        Plan p = new Plan();
                        p.setParentId(newPlan.getId());
                        p.setId(java.util.UUID.randomUUID().toString());
                        p.setUserId(userId);
                        p.setCreateDate(DateUtil.getDateDayString(new Date()));
                        p.setDetail("点击添加子计划");
                        p.setStatus(Constants.PLAN_STATUS.ADD_PLAN.getValue());
                        p.setOrder(Integer.MAX_VALUE);
                        p.setProcess(0.0f);
                        p.setType(Constants.PLAN_YPE.CHILD.getValue());
                        dao.insertPlan(p, null);
                        initData(showType);
                        setAdapter(dataMap);
                    } else {
                        //子计划
                        initData(showType);
                        setAdapter(dataMap);
                    }
                }

                @Override
                public void getFailure(Exception e) {

                }
            });
        }
    }

    private void setAdapter(final Map<Plan, List<Plan>> dataMap) {
        Object[] plans = dataMap.keySet().toArray();
        parentList.clear();
        for (Object plan : plans) {
            parentList.add((Plan) plan);
        }
        adapter = new PlanAdapter(mInflater, treeView, dataMap, PlanActivity2.this, new childClickListener() {
            @Override
            public void OnChildClick(int groupPosition, int childPosition) {
                Plan clickPlan = dataMap.get(parentList.get(groupPosition)).get(childPosition);
                if (clickPlan.getStatus() == Constants.PLAN_STATUS.ADD_PLAN.getValue()) {  //添加子计划
                    Plan parentPlan = parentList.get(groupPosition);
                    addChildPlan(parentPlan);
                } else {
                    Toast.makeText(PlanActivity2.this, dataMap.get(parentList.get(groupPosition)).get(childPosition).getDetail(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        treeView.setAdapter(adapter);
    }

    public interface childClickListener {
        void OnChildClick(int groupPosition, int childPosition);
    }

    private void addChildPlan(final Plan parentPlan) {

        String parentDetail = parentPlan.getDetail();
        View viewAdd = this.getLayoutInflater().inflate(R.layout.add_child_plan_dialog_layout, null);
        final Plan newPlan = new Plan();
        final Dialog dialog = new Dialog(this);
        dialog.setTitle(parentDetail);
        dialog.setContentView(viewAdd);
        dialog.show();

        ImageButton btnAdd = (ImageButton) viewAdd.findViewById(R.id.plan_add_prove);
        ImageButton btnClose = (ImageButton) viewAdd.findViewById(R.id.plan_add_close);
        final TextView tvEndTime = (TextView) viewAdd.findViewById(R.id.plan_endTime);
        final EditText etDetail = (EditText) viewAdd.findViewById(R.id.plan_detail);
        final EditText etRemark = (EditText) viewAdd.findViewById(R.id.plan_remark);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        etDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detail = etDetail.getText() == null ? "" : etDetail.getText().toString();
            }
        });
        etRemark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                remark = etRemark.getText() == null ? "" : etRemark.getText().toString();
            }
        });

        tvEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                final int year = now.get(Calendar.YEAR);
                final int month = now.get(Calendar.MONTH);
                final int day = now.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog endDatePicker = new DatePickerDialog(dialog.getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String endDateString = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        tvEndTime.setText(endDateString);
                        endDate = endDateString;
                    }
                }, year, month, day);
                endDatePicker.setTitle("请选择截止日期");
                endDatePicker.show();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parentTIme = parentPlan.getEndTime();
                String nowDate = DateUtil.getDateDayString(new Date());
                if (endDate == null) {
                    endDate = parentTIme;
                }

                if (endDate.compareTo(parentTIme) > 0 || endDate.compareTo(nowDate) < 0) {
                    tvEndTime.setError("日期选择错误！");
                    return;
                }

                String parentId = parentPlan.getId();
                List<Plan> childPlans = dataMap.get(parentPlan);
                int order = childPlans.size();

                newPlan.setId(java.util.UUID.randomUUID().toString());
                newPlan.setParentId(parentId);
                newPlan.setCreateDate(DateUtil.getDateDayString(new Date()));
                newPlan.setUserId(userId);
                newPlan.setDetail(detail);
                newPlan.setEndTime(endDate);
                newPlan.setOrder(order);
                newPlan.setRemark(remark);
                newPlan.setEndTime(endDate);
                newPlan.setProcess(0.0f);
                newPlan.setType(parentPlan.getType());
                newPlan.setStatus(Constants.PLAN_STATUS.WAIT.getValue());
                newPlan.setType(Constants.PLAN_YPE.CHILD.getValue());
                doAddPlan(parentPlan, newPlan);
                dialog.dismiss();
            }
        });
    }
}