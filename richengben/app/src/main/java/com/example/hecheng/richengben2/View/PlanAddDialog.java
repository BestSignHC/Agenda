package com.example.hecheng.richengben2.View;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.hecheng.richengben2.R;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.domin.Plan;
import com.example.hecheng.richengben2.domin.User;

import java.util.Calendar;

/**
 * Created by Administrator on 2017/4/28.
 */

public class PlanAddDialog extends Dialog {

    private ImageButton btnClose;
    private ImageButton btnAdd;
    private EditText etDetail;
    private TextView tvEndDate;
    private CheckBox cbWeekPlan;
    private CheckBox cbMonthPlan;

    private String detail = "";
    private String endDate = "";
    private User user = null;
    private String userId = "";
    private boolean isWeekPlan = true;
    private boolean isMonthPlan = false;

    private Context context;
    private Plan newPlan;

    public PlanAddDialog(Context context, int theme, User user, Plan newPlan) {
        super(context, theme);
        this.context = context;
        this.user = user;
        this.userId = user.getId();
        this.newPlan = newPlan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.add_plan_dialog_layout);

        initView();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        /*
        tvEndDate.setOnClickListener(new View.OnClickListener() {

            Calendar now = Calendar.getInstance();
            final int year = now.get(Calendar.YEAR);
            final int month = now.get(Calendar.MONTH);
            final int day = now.get(Calendar.DAY_OF_MONTH);

            @Override
            public void onClick(View v) {
                DatePickerDialog startDatePicker = new DatePickerDialog(PlanAddDialog.this.getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String startDateString = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        tvEndDate.setText(startDateString);
                        endDate = startDateString;
                    }
                }, year, month, day);
                startDatePicker.setTitle("请选择截止日期");
                startDatePicker.show();
            }
        });
        */

        cbWeekPlan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isWeekPlan = !isWeekPlan;
            }
        });

        cbMonthPlan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMonthPlan = !isMonthPlan;
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
    }

    private void initView() {
        btnClose = (ImageButton) findViewById(R.id.plan_add_close);
        btnAdd = (ImageButton) findViewById(R.id.plan_add_prove);
        etDetail = (EditText) findViewById(R.id.plan_detail);
//        tvEndDate = (TextView) findViewById(R.id.plan_endTime);
        cbWeekPlan = (CheckBox) findViewById(R.id.week_plan);
        cbMonthPlan = (CheckBox) findViewById(R.id.month_plan);

        cbWeekPlan.setChecked(true);
    }
}
