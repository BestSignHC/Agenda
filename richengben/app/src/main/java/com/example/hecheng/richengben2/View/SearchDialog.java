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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hecheng.richengben2.R;
import com.example.hecheng.richengben2.domin.User;

import java.util.Calendar;

/**
 * 日程搜索对话框
 * Created by HeCheng on 2017/4/14.
 */

public class SearchDialog extends Dialog {

    private ImageButton btnClose;
    private ImageButton btnSearch;
    private EditText etDetail;
    private EditText etTitle;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private CheckBox cbStatusDel;
    private CheckBox cbStatusDWait;
    private CheckBox cbStatusDone;
    private TextView labelTitle;

    private String detail = "";
    private String title = "";
    private String startDate = "";
    private String endDate = "";
    private User user = null;
    private String userId = "";
    private boolean statusDel = true;
    private boolean statusWait = true;
    private boolean statusDone = true;

    private OnSearchBtnClickListener searchBtnClickListener;

    public void setSearchBtnClickListener(OnSearchBtnClickListener searchBtnClickListener) {
        this.searchBtnClickListener = searchBtnClickListener;
    }

    Context context;

    public SearchDialog(Context context, int theme, User user) {
        super(context, theme);
        this.context = context;
        this.user = user;
        this.userId = user.getId();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.search_dialog_layout);

        initView();

        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (searchBtnClickListener != null) {
                    searchBtnClickListener.onSearchBtClick();
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();

        if (etDetail.getText() != null) {
            detail = etDetail.getText().toString();
        }

        if (tvStartDate.getText() != null) {
            startDate = tvStartDate.getText().toString();
            if ("开始时间".equals(startDate)) {
                startDate = "";
            }
        }

        if (tvEndDate.getText() != null) {
            endDate = tvEndDate.getText().toString();
            if ("结束时间".equals(endDate)) {
                endDate = "";
            }
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tvStartDate.setOnClickListener(new View.OnClickListener() {

            Calendar now = Calendar.getInstance();
            final int year = now.get(Calendar.YEAR);
            final int month = now.get(Calendar.MONTH);
            final int day = now.get(Calendar.DAY_OF_MONTH);

            @Override
            public void onClick(View v) {
                DatePickerDialog startDatePicker = new DatePickerDialog(SearchDialog.this.getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String startDateString = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        tvStartDate.setText(startDateString);
                        startDate = startDateString;
                    }
                }, year, month, day);
                startDatePicker.setTitle("请选择开始日期");
                startDatePicker.show();
            }
        });

        tvEndDate.setOnClickListener(new View.OnClickListener() {

            Calendar now = Calendar.getInstance();
            final int year = now.get(Calendar.YEAR);
            final int month = now.get(Calendar.MONTH);
            final int day = now.get(Calendar.DAY_OF_MONTH);

            @Override
            public void onClick(View v) {
                DatePickerDialog endDatePicker = new DatePickerDialog(SearchDialog.this.getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String endDateString = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        tvEndDate.setText(endDateString);
                        endDate = endDateString;
                    }
                }, year, month, day);
                endDatePicker.setTitle("请选择截止日期");
                endDatePicker.show();
            }
        });

        cbStatusDel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                statusDel = !statusDel;
            }
        });
        cbStatusDWait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                statusWait = !statusWait;
            }
        });
        cbStatusDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                statusDone = !statusDone;
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
        btnClose = (ImageButton) findViewById(R.id.scheduleSearchClose);
        btnSearch = (ImageButton) findViewById(R.id.scheduleSearch);
        etDetail = (EditText) findViewById(R.id.schedule_detail);
        etTitle = (EditText) findViewById(R.id.note_search_title);
        tvStartDate = (TextView) findViewById(R.id.schedule_startTime);
        tvEndDate = (TextView) findViewById(R.id.schedule_endTime);
        cbStatusDel = (CheckBox) findViewById(R.id.status_del);
        cbStatusDWait = (CheckBox) findViewById(R.id.status_wait);
        cbStatusDone = (CheckBox) findViewById(R.id.status_done);
        labelTitle = (TextView) findViewById(R.id.label_title);

        etTitle.setVisibility(View.GONE);
        labelTitle.setVisibility(View.GONE);
    }

    public ImageButton getBtnSearch() {
        return btnSearch;
    }

    public interface OnSearchBtnClickListener {
        public void onSearchBtClick();
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getDetail() {
        return detail;
    }

    public TextView getTvEndDate() {
        return tvEndDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isStatusDone() {
        return statusDone;
    }

    public boolean isStatusWait() {
        return statusWait;
    }

    public boolean isStatusDel() {
        return statusDel;
    }

    /**
     * 修改样式为笔记搜索
     */
    public void changeToNoteView(){

        initView();
        LinearLayout statusListLayout = (LinearLayout) findViewById(R.id.status_list);
        TextView labelDetail = (TextView) findViewById(R.id.label_detail);
        TextView labelTime = (TextView) findViewById(R.id.label_time);
        TextView labelStatus = (TextView) findViewById(R.id.label_status);
        statusListLayout.setVisibility(View.GONE);
        labelStatus.setVisibility(View.GONE);
        labelDetail.setText("按笔记内容搜索");
        labelTime.setText("按更新日期搜索");
        etTitle.setVisibility(View.VISIBLE);
        labelTitle.setVisibility(View.VISIBLE);

        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                title = etTitle.getText() == null ? "" : etTitle.getText().toString();
            }
        });
    }
}
