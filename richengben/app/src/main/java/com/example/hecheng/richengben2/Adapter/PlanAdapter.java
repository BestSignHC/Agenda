package com.example.hecheng.richengben2.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hecheng.richengben2.DaoImpl.PlanDao;
import com.example.hecheng.richengben2.PlanActivity2;
import com.example.hecheng.richengben2.R;
import com.example.hecheng.richengben2.common.DateUtil;
import com.example.hecheng.richengben2.View.TreeView;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.domin.Plan;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/6.
 */

public class PlanAdapter extends BaseExpandableListAdapter implements TreeView.IphoneTreeHeaderAdapter {

    private Context context;
    private LayoutInflater mInflater;
    private TreeView treeView;
    private PlanActivity2.childClickListener listener;

    private Map<Plan, List<Plan>> dataMap = new HashMap<>();
    private Object[] parentList;

    private Map<Integer, Integer> STEP_IMG = new HashMap<>();
    private HashMap<Integer, Integer> groupStatusMap = new HashMap<>();

    public PlanAdapter(LayoutInflater mInflater, TreeView treeView, Map<Plan, List<Plan>> dataMap, Context context, PlanActivity2.childClickListener listener) {
        this.mInflater = mInflater;
        this.treeView = treeView;
        this.dataMap = dataMap;
        this.context = context;
        this.listener = listener;

        initParentList();
        initStepImg();
    }

    public Object getChild(int groupPosition, int childPosition) {
        return dataMap.get(parentList[groupPosition]).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        if(groupPosition < 0 ){
            return 0;
        }
        List<Plan> childs = dataMap.get(parentList[groupPosition]);
        if(childs == null) {
            return 0;
        }
        return childs.size();
    }

    public Object getGroup(int groupPosition) {
        return parentList[groupPosition];
    }

    public int getGroupCount() {
        return parentList.length;
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) { //子plan
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.plan_list_item_view, null);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnChildClick(groupPosition, childPosition);
            }
        });

        Plan parentPlan = (Plan) parentList[groupPosition];
        List<Plan> childs = dataMap.get(parentPlan);
        final Plan child = childs.get(childPosition);
        final int order = child.getOrder();
        int imgIndex = order > 9 ? -1 : order;
        if(order == Integer.MAX_VALUE) {
            imgIndex = 0;
        }

        String childPlanDetail = child.getDetail();
        String childPlanRemark = child.getRemark() == null ? "" : child.getRemark();
        String childPlanEndDate = child.getEndTime() == null ? "" : child.getEndTime();
        int status = child.getStatus();
        String endTime = child.getEndTime();
        String nowTime = DateUtil.getDateDayString(new Date());

        ImageView planStepIcon = (ImageView) convertView.findViewById(R.id.icon);
        planStepIcon.setImageResource(STEP_IMG.get(imgIndex));
        TextView tvChildPlanDetail = (TextView) convertView.findViewById(R.id.child_plan_detail);
        tvChildPlanDetail.setText(childPlanDetail);
        TextView tvChildPlanRemark = (TextView) convertView .findViewById(R.id.child_plan_remark);
        tvChildPlanRemark.setText(childPlanRemark);
        TextView tvChildPlanEndDate = (TextView) convertView .findViewById(R.id.child_plan_end_date);
        tvChildPlanEndDate.setText(childPlanEndDate);
        final ImageButton imgDone = (ImageButton) convertView.findViewById(R.id.child_plan_done);  //完成按钮
        if(status == Constants.PLAN_STATUS.ADD_PLAN.getValue()) {
            imgDone.setVisibility(View.GONE);
        }
        else if(status == Constants.PLAN_STATUS.WAIT.getValue()) {
            if(endTime.compareTo(nowTime) < 0) {
                status = Constants.PLAN_STATUS.DELAY.getValue();
                imgDone.setImageResource(R.mipmap.plan_delay);
                imgDone.setClickable(false);
            }
            else{
                imgDone.setImageResource(R.mipmap.cb_uncheck);
                imgDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // 完成子步骤
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("确认完成了该步骤么？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                child.setStatus(Constants.PLAN_STATUS.DONE.getValue());
                                child.setEndTime(DateUtil.getDateDayString(new Date()));
                                final PlanDao dao = new PlanDao(context);
                                dao.donePlan(child);
                                imgDone.setImageResource(R.mipmap.cb_checked);
                                imgDone.setClickable(false);
                                notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                    }
                });
            }
        }
        else if(status == Constants.PLAN_STATUS.DONE.getValue()){
            imgDone.setImageResource(R.mipmap.cb_checked);
            imgDone.setClickable(false);
        }
        else {
            imgDone.setImageResource(R.mipmap.plan_delay);
            imgDone.setClickable(false);
        }
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {  //父plan
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.plan_list_group_view, null);
        }
        Plan parentPlan =  (Plan)parentList[groupPosition];
        List<Plan> childs = dataMap.get(parentPlan);
        if(parentPlan == null) {
            return convertView;
        }
        TextView parentPlanDetail = (TextView) convertView.findViewById(R.id.group_name);
        parentPlanDetail.setText(parentPlan.getDetail());

        ImageView indicator = (ImageView) convertView.findViewById(R.id.group_indicator);
        TextView doneNum = (TextView) convertView.findViewById(R.id.online_count);
        int chilePlansNum = childs.size() -1;
        int childPlansDoneNum = 0;
        for(Plan child : childs) {
            if(child.getStatus() == Constants.PLAN_STATUS.DONE.getValue()) {
                childPlansDoneNum ++;
            }
        }
        doneNum.setText(childPlansDoneNum + "/" + chilePlansNum);

        if (isExpanded) {
            indicator.setImageResource(R.drawable.indicator_expanded);
        } else {
            indicator.setImageResource(R.drawable.indicator_unexpanded);
        }
        return convertView;
    }

    @Override
    public int getTreeHeaderState(int groupPosition, int childPosition) {
        final int childCount = getChildrenCount(groupPosition);
        if (childPosition == childCount - 1) {
            return PINNED_HEADER_PUSHED_UP;
        } else if (childPosition == -1  && !treeView.isGroupExpanded(groupPosition)) {
            return PINNED_HEADER_GONE;
        } else {
            return PINNED_HEADER_VISIBLE;
        }
    }

    @Override
    public void configureTreeHeader(View header, int groupPosition, int childPosition, int alpha) {
        Plan parentPlan = (Plan)parentList[groupPosition];
        if(parentPlan == null) {
            return ;
        }
        List<Plan> childs = dataMap.get(parentPlan);
        int chilePlansNum = childs.size() -1;
        int childPlansDoneNum = 0;
        for(Plan child : childs) {
            if(child.getStatus() == Constants.PLAN_STATUS.DONE.getValue()) {
                childPlansDoneNum ++;
            }
        }
        ((TextView) header.findViewById(R.id.group_name)).setText(parentPlan.getDetail());
        ((TextView) header.findViewById(R.id.online_count)).setText(childPlansDoneNum + "/"+ chilePlansNum);
    }

    @Override
    public void onHeadViewClick(int groupPosition, int status) {
        groupStatusMap.put(groupPosition, status);
    }

    @Override
    public int getHeadViewClickStatus(int groupPosition) {
        if (groupStatusMap.containsKey(groupPosition)) {
            return groupStatusMap.get(groupPosition);
        } else {
            return 0;
        }
    }

    private void initStepImg() {
        STEP_IMG.put(1, R.mipmap.step1);
        STEP_IMG.put(2, R.mipmap.step2);
        STEP_IMG.put(3, R.mipmap.step3);
        STEP_IMG.put(4, R.mipmap.step4);
        STEP_IMG.put(5, R.mipmap.step5);
        STEP_IMG.put(6, R.mipmap.step6);
        STEP_IMG.put(7, R.mipmap.step7);
        STEP_IMG.put(8, R.mipmap.step8);
        STEP_IMG.put(9, R.mipmap.step9);
        STEP_IMG.put(0, R.mipmap.step_add);
        STEP_IMG.put(-1, R.mipmap.step_more);
    }

    private void initParentList(){
        Object[] parentListTemp = dataMap.keySet().toArray();
        int len = parentListTemp.length;
        parentList = new Object[len];
        for(int i = 0; i < len; i++) {
            parentList[i] = parentListTemp[i];
        }
    }
}
