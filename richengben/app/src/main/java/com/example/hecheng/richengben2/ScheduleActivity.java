package com.example.hecheng.richengben2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hecheng.richengben2.Adapter.ScheduleAdapter;
import com.example.hecheng.richengben2.DB.DBHelper;
import com.example.hecheng.richengben2.DaoImpl.ScheduleDao;
import com.example.hecheng.richengben2.DaoImpl.ScheduleSDao;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.common.DateUtil;
import com.example.hecheng.richengben2.common.ProgressDialogUtil;
import com.example.hecheng.richengben2.domin.Schedule;
import com.example.hecheng.richengben2.domin.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 日程管理Activity
 * Created by HeCheng on 2017/4/4.
 */

public class ScheduleActivity extends AppCompatActivity {

    private static final String TAG = "ScheduleActivity";

    @BindView(R.id.grid_recycler)
    public RecyclerView recyclerView;
    @BindView(R.id.onlyWaitSchedule)
    public ImageButton cbOnlyWaitSchedule;
    @BindView(R.id.onlyWaitLabel)
    public TextView tvOnlyWaitLabel;
    @BindView(R.id.sechdule_btn_back)
    public ImageButton btnBack;
    @BindView(R.id.btn_add_schedule)
    public FloatingActionButton btnAddSchedule;
    @BindView(R.id.grid_swipe_refresh)
    public SwipeRefreshLayout swipeRefresh;

    private ScheduleAdapter scheduleAdapter;
    private Date nowDate;
    private User user;

    private List<Schedule> scheduleLists = new ArrayList<Schedule>();
    private List<Schedule> scheduleWaitList = new ArrayList<>();
    private Map<String, Schedule> scheduleMap = new HashMap<String, Schedule>();

    private int windowWidth;
    boolean isSearch = false;
    boolean isOnlyWait = true;
    private String updateScheduleId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.schedule_layout);
        ButterKnife.bind(this);
        Bmob.initialize(this, Constants.APP_ID);

        WindowManager wm = this.getWindowManager();
        windowWidth = wm.getDefaultDisplay().getWidth();

        Intent intent = this.getIntent();
        isSearch = (Boolean) intent.getSerializableExtra("isSearch");
        if (!isSearch) {
            nowDate = (Date) intent.getSerializableExtra("date");

            Date _nowDate = new Date(System.currentTimeMillis());
            _nowDate.setHours(0);
            _nowDate.setMinutes(0);
            _nowDate.setSeconds(0);

            if(!_nowDate.after(nowDate)) {
                btnAddSchedule.setVisibility(View.INVISIBLE);
            }

            user = (User) intent.getSerializableExtra("user");
        } else {
            isOnlyWait = false;
            user = (User) intent.getSerializableExtra("user");
            scheduleLists = (List<Schedule>) intent.getSerializableExtra("schedules");
            scheduleWaitList.clear();
            for (Schedule temp : scheduleLists) {
                if (temp.getStatus() == Constants.SCHEDULE_STATUS.WAIT.getValue()) {
                    scheduleWaitList.add(temp);
                }
            }
        }
    }

    //根据显示模式切换数据
    private void initMap() {
        if (isOnlyWait) {
            for (Schedule s : scheduleWaitList) {
                scheduleMap.put(s.getObjectId(), s);
            }
        } else {
            for (Schedule s : scheduleLists) {
                scheduleMap.put(s.getObjectId(), s);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //创建默认的线性LayoutManager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        recyclerView.setHasFixedSize(true);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(false);
            }
        });

        //创建并设置Adapter
        if (!isSearch) {
            getUserScheduleNowDay();
        } else {
            //更新修改的schedule
            if(updateScheduleId != null && updateScheduleId.length() > 0) {
                ScheduleDao dao = new ScheduleDao(getApplicationContext());
                dao.queryScheduleById(updateScheduleId, new BaseListener<Schedule>() {
                    @Override
                    public void getSuccess(Schedule schedule) {
                        for(Schedule temp : scheduleLists){
                            if(temp.getObjectId().equals(updateScheduleId)) {
                                int index = scheduleLists.indexOf(temp);
                                scheduleLists.remove(index);
                                scheduleLists.add(index, schedule);
                                break;
                            }
                        }
                        scheduleAdapter = new ScheduleAdapter(scheduleLists);
                        initMap();
                        scheduleAdapter.setOnItemClickListener(new ScheduleAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view) {
                                LinearLayout layout = (LinearLayout) view;
                                TextView tvScheduleId = (TextView) layout.findViewById(R.id.schedule_id);
                                TextView tvScduleTime = (TextView) layout.findViewById(R.id.schedule_time);
                                String nowDateString = tvScduleTime.getText().toString().substring(0, 10);
                                nowDate = DateUtil.parseToDate(nowDateString);

                                updateScheduleId = tvScheduleId.getText().toString();
                                Log.d(TAG, "update scedule id = " + updateScheduleId);
                                updateSchedule(updateScheduleId, nowDate);
                            }
                        });
                        recyclerView.setAdapter(scheduleAdapter);
                    }

                    @Override
                    public void getFailure(Exception e) {

                    }
                });
            }
            else {
                scheduleAdapter = new ScheduleAdapter(scheduleLists);
                initMap();
                scheduleAdapter.setOnItemClickListener(new ScheduleAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view) {
                        LinearLayout layout = (LinearLayout) view;
                        TextView tvScheduleId = (TextView) layout.findViewById(R.id.schedule_id);
                        TextView tvScduleTime = (TextView) layout.findViewById(R.id.schedule_time);
                        String nowDateString = tvScduleTime.getText().toString().substring(0, 10);
                        nowDate = DateUtil.parseToDate(nowDateString);

                       updateScheduleId  = tvScheduleId.getText().toString();
                        Log.d(TAG, "update scedule id = " + updateScheduleId);
                        updateSchedule(updateScheduleId, nowDate);
                    }
                });
                recyclerView.setAdapter(scheduleAdapter);
            }
            btnAddSchedule.setVisibility(View.INVISIBLE);
            cbOnlyWaitSchedule.setVisibility(View.INVISIBLE);
            tvOnlyWaitLabel.setVisibility(View.INVISIBLE);
        }
        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(recyclerView);

        cbOnlyWaitSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOnlyWait = !isOnlyWait;
                initMap();
                if (isOnlyWait) {
                    cbOnlyWaitSchedule.setImageDrawable(getResources().getDrawable(R.mipmap.square_ok));
                    scheduleAdapter = new ScheduleAdapter(scheduleWaitList);
                } else {
                    cbOnlyWaitSchedule.setImageDrawable(getResources().getDrawable(R.mipmap.square));
                    scheduleAdapter = new ScheduleAdapter(scheduleLists);
                }
                scheduleAdapter.setOnItemClickListener(new ScheduleAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view) {
                        LinearLayout layout = (LinearLayout) view;
                        TextView tvScheduleId = (TextView) layout.findViewById(R.id.schedule_id);
                        TextView tvScduleTime = (TextView) layout.findViewById(R.id.schedule_time);
                        String nowDateString = tvScduleTime.getText().toString().substring(0, 10);
                        nowDate = DateUtil.parseToDate(nowDateString);

                        updateScheduleId = tvScheduleId.getText().toString();
                        Log.d(TAG, "update scedule id = " + updateScheduleId);
                        updateSchedule(updateScheduleId, nowDate);
                    }
                });
                recyclerView.setAdapter(scheduleAdapter);
            }
        });
    }

    private void getUserScheduleNowDay() {

        final ProgressDialog progressDialog = ProgressDialogUtil.showProcessDialog("查询中。。。", ScheduleActivity.this);
        progressDialog.show();

        ScheduleDao scheduleDao = new ScheduleDao(getApplicationContext());
        scheduleDao.querySchedulesByUserIdAndCreateDate(user.getId(), DateUtil.getDateDayString(nowDate), new BaseListener<List<Schedule>>() {
            @Override
            public void getSuccess(List<Schedule> schedules) {
                progressDialog.dismiss();
                Log.d(TAG, "gquery user schedule list total:" + schedules.size());
                scheduleLists = schedules;
                scheduleWaitList.clear();
                for (Schedule temp : scheduleLists) {
                    if (temp.getStatus() == Constants.SCHEDULE_STATUS.WAIT.getValue()) {
                        scheduleWaitList.add(temp);
                    }
                }
                if (isOnlyWait) {
                    scheduleAdapter = new ScheduleAdapter(scheduleWaitList);
                } else {
                    scheduleAdapter = new ScheduleAdapter(scheduleLists);
                }
                initMap();
                scheduleAdapter.setOnItemClickListener(new ScheduleAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view) {
                        LinearLayout layout = (LinearLayout) view;
                        TextView tvScheduleId = (TextView) layout.findViewById(R.id.schedule_id);
                        TextView tvScduleTime = (TextView) layout.findViewById(R.id.schedule_time);
                        String scheduleid = tvScheduleId.getText().toString();
                        String nowDateString = tvScduleTime.getText().toString().substring(0, 10);
                        nowDate = DateUtil.parseToDate(nowDateString);
                        Log.d(TAG, "update scedule id = " + scheduleid);
                        updateSchedule(scheduleid, nowDate);
                    }
                });
                recyclerView.setAdapter(scheduleAdapter);

                if(isOnlyWait && scheduleWaitList.size() == 0) { //无数据
                    ProgressDialogUtil.showNoticeScheduleDialog("您的日程空空如也!", ScheduleActivity.this);
                }else if( (!isOnlyWait) && scheduleLists.size() == 0){ //无数据
                    ProgressDialogUtil.showNoticeScheduleDialog("您的日程空空如也!", ScheduleActivity.this);
                }
            }

            @Override
            public void getFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @OnClick(R.id.sechdule_btn_back)
    public void backToDate() {
        finish();
    }

    @OnClick(R.id.btn_add_schedule)
    public void addSchedule() {
        //弹出一个添加日程的弹窗
        Intent intent = new Intent(getApplicationContext(), AddScheduleActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        bundle.putSerializable("date", nowDate);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void updateSchedule(String scheduleId, Date nowDate) {

        Schedule updateSchedule = scheduleMap.get(scheduleId);
        Intent intent = new Intent(getApplicationContext(), AddScheduleActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        bundle.putSerializable("date", nowDate);
        bundle.putSerializable("schedule", updateSchedule);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //实现日程项的滑动操作
    private ItemTouchHelper getItemTouchHelper() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {

            //用于设置拖拽和滑动的方向
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = 0, swipeFlags = 0;
                if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager || recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    //网格式布局有4个方向
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                    //线性式布局有2个方向
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

                    swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END; //设置侧滑方向为从两个方向都可以
                }
                return makeMovementFlags(dragFlags, swipeFlags);//swipeFlags 为0的话item不滑动
            }

            //长摁item拖拽时会回调这个方法
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                int from = viewHolder.getAdapterPosition();
//                int to = target.getAdapterPosition();
//
//                if (isOnlyWait) {
//                    Schedule moveItem = scheduleWaitList.get(from);
//                    scheduleWaitList.remove(moveItem);
//                    scheduleWaitList.add(to, moveItem);
//                } else {
//                    Schedule moveItem = scheduleLists.get(from);
//                    scheduleLists.remove(moveItem);
//                    scheduleLists.add(to, moveItem);
//                }
//                scheduleAdapter.notifyItemMoved(from, to);//更新适配器中item的位置
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //这里处理滑动删除
                final int position = viewHolder.getAdapterPosition();
                final Schedule swipSchedule;
                if (isOnlyWait) {
                    swipSchedule = scheduleWaitList.get(position);
                } else {
                    swipSchedule = scheduleLists.get(position);
                }
                if (swipSchedule.getStatus() != Constants.SCHEDULE_STATUS.WAIT.getValue()) {
                    if (isOnlyWait) {
                        scheduleWaitList.remove(position);
                        scheduleWaitList.add(position, swipSchedule);
                    } else {
                        scheduleLists.remove(position);
                        scheduleLists.add(position, swipSchedule);
                    }
                    scheduleAdapter.notifyItemChanged(position);
                    return;
                }
                if (direction == 16) {
                    //左划完成
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                    builder.setTitle("确认完成了该日程么？");
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (isOnlyWait) {
                                scheduleWaitList.remove(position);
                                scheduleWaitList.add(position, swipSchedule);
                            } else {
                                scheduleLists.remove(position);
                                scheduleLists.add(position, swipSchedule);
                            }
                            scheduleAdapter.notifyItemChanged(position);
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            final Schedule removeSchedule;
                            if (isOnlyWait) {
                                removeSchedule = scheduleWaitList.remove(position);
                                scheduleLists.remove(removeSchedule);
                            } else {
                                removeSchedule = scheduleLists.remove(position);
                                scheduleWaitList.remove(removeSchedule);
                            }
                            String scheduleId = removeSchedule.getObjectId();

                            final Schedule newSchedule = new Schedule();
                            newSchedule.setRemark(removeSchedule.getRemark());
                            newSchedule.setColor(removeSchedule.getColor());
                            newSchedule.setCreateDate(removeSchedule.getCreateDate());
                            newSchedule.setStartTime(removeSchedule.getStartTime());
                            newSchedule.setEndTime(removeSchedule.getEndTime());
                            newSchedule.setDetail(removeSchedule.getDetail());
                            newSchedule.setNotice(removeSchedule.isNotice());
                            newSchedule.setObjectId(scheduleId);
                            newSchedule.setCreateDate(removeSchedule.getCreateDate());
                            newSchedule.setUserId(removeSchedule.getUserId());
                            newSchedule.setStatus(Constants.SCHEDULE_STATUS.DONE.getValue());
                            newSchedule.setDoneDate(DateUtil.getDateSecondString(new Date()));

                            newSchedule.update(scheduleId, new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        Log.d(TAG, "###set schedule done:" + newSchedule.toString());

                                        DBHelper dbHelper = new DBHelper(getApplicationContext());
                                        ScheduleSDao sdao = new ScheduleSDao(dbHelper);
                                        sdao.updateSchedule(newSchedule);

                                        scheduleAdapter.notifyItemRemoved(position);
                                        dialog.dismiss(); // 让窗口消失
                                        Toast.makeText(getApplicationContext(), "太棒了，恭喜你完成了日程。", Toast.LENGTH_SHORT).show();
                                        if (isOnlyWait) {
                                            scheduleAdapter.notifyItemChanged(position);
                                        }
                                        scheduleLists.add(newSchedule);
                                        scheduleAdapter.notifyItemChanged(scheduleLists.size());
                                    } else {
                                        e.printStackTrace();
                                        if (isOnlyWait) {
                                            scheduleWaitList.add(position, removeSchedule);
                                        } else {
                                            scheduleLists.add(position, removeSchedule);
                                        }
                                        scheduleAdapter.notifyItemChanged(position);
                                    }
                                }
                            });
                        }
                    });
                    builder.create().show();
                } else {
                    //右划删除

                    final EditText inputReason = new EditText(ScheduleActivity.this);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                    builder.setTitle("请输入删除原由");
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setView(inputReason);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            //删除
                            String delReason = "";
                            if (inputReason.getText() != null) {
                                delReason = inputReason.getText().toString();
                            }
                            if (delReason == null || delReason.length() < 1) {
                                Toast.makeText(getApplicationContext(), "请输入删除缘由！", Toast.LENGTH_SHORT).show();
                                if (isOnlyWait) {
                                    Schedule s = scheduleWaitList.get(position);
                                    scheduleWaitList.remove(position);
                                    scheduleWaitList.add(position, s);
                                } else {
                                    Schedule s = scheduleLists.get(position);
                                    scheduleLists.remove(position);
                                    scheduleLists.add(position, s);
                                }
                                scheduleAdapter.notifyItemChanged(position);
                                return;
                            }

                            final Schedule removeSchedule;
                            if (isOnlyWait) {
                                removeSchedule = scheduleWaitList.remove(position);
                                scheduleLists.remove(removeSchedule);
                            } else {
                                removeSchedule = scheduleLists.remove(position);
                                scheduleWaitList.remove(removeSchedule);
                            }
                            String scheduleId = removeSchedule.getObjectId();

                            final Schedule newSchedule = new Schedule();
                            newSchedule.setRemark(removeSchedule.getRemark());
                            newSchedule.setColor(removeSchedule.getColor());
                            newSchedule.setCreateDate(removeSchedule.getCreateDate());
                            newSchedule.setStartTime(removeSchedule.getStartTime());
                            newSchedule.setEndTime(removeSchedule.getEndTime());
                            newSchedule.setDetail(removeSchedule.getDetail());
                            newSchedule.setNotice(removeSchedule.isNotice());
                            newSchedule.setObjectId(scheduleId);
                            newSchedule.setCreateDate(removeSchedule.getCreateDate());
                            newSchedule.setUserId(removeSchedule.getUserId());
                            newSchedule.setStatus(Constants.SCHEDULE_STATUS.CANCLE.getValue());
                            newSchedule.setReason(delReason);

                            newSchedule.update(scheduleId, new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        Log.d(TAG, "###set schedule cancel:" + newSchedule.toString());

                                        DBHelper dbHelper = new DBHelper(getApplicationContext());
                                        ScheduleSDao sdao = new ScheduleSDao(dbHelper);
                                        sdao.updateSchedule(newSchedule);

                                        scheduleAdapter.notifyItemRemoved(position);
                                        dialog.dismiss(); // 让窗口消失
                                        Toast.makeText(getApplicationContext(), "删除成功，请谨慎制定日程！", Toast.LENGTH_SHORT).show();
                                        if (isOnlyWait) {
                                            scheduleAdapter.notifyItemChanged(position);
                                        }
                                        scheduleLists.add(newSchedule);
                                        scheduleAdapter.notifyItemChanged(scheduleLists.size());
                                    } else {
                                        e.printStackTrace();
                                        if (isOnlyWait) {
                                            scheduleWaitList.add(position, removeSchedule);
                                        } else {
                                            scheduleLists.add(position, removeSchedule);
                                        }
                                        scheduleAdapter.notifyItemChanged(position);
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //不删除
                            if (isOnlyWait) {
                                Schedule s = scheduleWaitList.get(position);
                                scheduleWaitList.remove(position);
                                scheduleWaitList.add(position, s);
                            } else {
                                Schedule s = scheduleLists.get(position);
                                scheduleLists.remove(position);
                                scheduleLists.add(position, s);
                            }
                            scheduleAdapter.notifyItemChanged(position);
                        }
                    });
                    builder.create().show();
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;//返回true则为所有item都设置可以拖拽
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder.itemView.setBackgroundColor(Color.LTGRAY);//拖拽时设置背景色为灰色
                }
            }

            //当item拖拽完成时调用
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(Color.WHITE);//拖拽停止时设置背景色为白色
            }

            //当item视图变化时调用
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                //根据item滑动偏移的值修改item透明度。screenwidth是我提前获得的屏幕宽度
                viewHolder.itemView.setAlpha(1 - Math.abs(dX) / windowWidth);
            }

        });
        return itemTouchHelper;
    }
}
