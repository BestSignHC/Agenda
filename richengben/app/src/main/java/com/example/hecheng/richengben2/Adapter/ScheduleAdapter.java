package com.example.hecheng.richengben2.Adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hecheng.richengben2.R;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.domin.Schedule;

import java.util.List;

/**
 * Created by Administrator on 2017/4/4.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> implements View.OnClickListener{

    public List<Schedule> schedules;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    boolean isDoneSchedule = false;

    public ScheduleAdapter(List<Schedule> schedules) {
        this.schedules = schedules;
    }
    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.schedule_layout_item,viewGroup,false);
        ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(this);
        return vh;
    }
    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        Schedule schedule = schedules.get(position);
        int color = schedule.getColor();
        String colorValue = Constants.colorMap.get(color);
        viewHolder.scheduleColor.setBackgroundColor(Color.parseColor(colorValue));
        viewHolder.scheduleDetail.setText(schedule.getDetail());
        String endTimeString = schedule.getEndTime();
        String startTimeString = schedule.getStartTime() + "";
        viewHolder.scheduleTime.setText(schedule.getCreateDate() + "  " + startTimeString +" ——"+ endTimeString);
        viewHolder.scheduleDetail.setTextColor(Color.parseColor(colorValue));
        viewHolder.scheduleTime.setTextColor(Color.parseColor(colorValue));
        viewHolder.scheduleColor.setText(Constants.textMap.get(color));
        viewHolder.scheduleColor.setTextColor(0xffffffff);
        viewHolder.scheduleId.setText(schedule.getObjectId());
        viewHolder.scheduleStatus.setText(schedule.getStatus() + "");

        if(schedule.getStatus() == Constants.SCHEDULE_STATUS.DONE.getValue()) {
            viewHolder.doneButton.setVisibility(View.VISIBLE);
            viewHolder.doneButton.setImageResource(R.mipmap.done);
        }else if(schedule.getStatus() == Constants.SCHEDULE_STATUS.CANCLE.getValue()) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 20, 10, 0);
            viewHolder.doneButton.setVisibility(View.VISIBLE);
            viewHolder.doneButton.setImageResource(R.mipmap.del);
            viewHolder.doneButton.setLayoutParams(lp);
        }
        else {
            viewHolder.doneButton.setVisibility(View.INVISIBLE);
        }
    }
    //获取数据的数量
    @Override
    public int getItemCount() {
        return schedules.size();
    }

    @Override
    public void onClick(View v) {

        TextView tvScheduleStatus = (TextView) v.findViewById(R.id.schedule_status);
        String scheduleStatus = tvScheduleStatus.getText().toString();
        isDoneSchedule = (scheduleStatus.equals(Constants.SCHEDULE_STATUS.DONE.getValue()+"")) ? true : false;

        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v);
        }
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Button scheduleColor;
        public TextView scheduleDetail;
        public TextView scheduleTime;
        public TextView scheduleStatus;
        public TextView scheduleId;
        public ImageView doneButton;
        public ViewHolder(View view){
            super(view);
            scheduleDetail = (TextView) view.findViewById(R.id.schedule_detail);
            scheduleColor = (Button)view.findViewById(R.id.schedule_color);
            scheduleTime =(TextView) view.findViewById(R.id.schedule_time);
            scheduleId = (TextView) view.findViewById(R.id.schedule_id);
            doneButton = (ImageView) view.findViewById(R.id.schedule_img_done);
            scheduleStatus = (TextView) view.findViewById(R.id.schedule_status);
        }
    }

    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
