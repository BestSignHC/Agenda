package com.example.hecheng.richengben2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.hecheng.richengben2.Adapter.MyNoteListAdapter;
import com.example.hecheng.richengben2.DaoImpl.NoteDao;
import com.example.hecheng.richengben2.View.SearchDialog;
import com.example.hecheng.richengben2.View.SpacesItemDecoration;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.domin.Note;
import com.example.hecheng.richengben2.domin.User;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;

/**
 * Created by HeCheng on 2017/3/22.
 */

public class NoteActivity extends BaseActivity {

    private String groupName;
    private int groupId;//分类ID

    private NoteDao noteDao;
    private List<Note> noteList;

    @BindView(R.id.btn_add_note)
    FloatingActionButton btnAddNote;
    private XRecyclerView rv_list_main;
    private MyNoteListAdapter mNoteListAdapter;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_layout);
        ButterKnife.bind(this);
        Bmob.initialize(this, Constants.APP_ID);

        Intent intent = this.getIntent();
        user = (User) intent.getSerializableExtra("user");

        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNoteList();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("笔记管理");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewNoteActivity.class);
                intent.putExtra("groupName", groupName);
                intent.putExtra("flag", 0);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        noteDao = new NoteDao(this);

        rv_list_main = (XRecyclerView) findViewById(R.id.rv_list_main);
        /****************** 设置XRecyclerView属性 **************************/
        rv_list_main.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        rv_list_main.setLayoutManager(layoutManager);

        rv_list_main.setLoadingMoreEnabled(true);//开启上拉加载
        rv_list_main.setPullRefreshEnabled(true);//开启下拉刷新
        rv_list_main.setRefreshProgressStyle(ProgressStyle.SquareSpin);
        rv_list_main.setLoadingMoreProgressStyle(ProgressStyle.BallScale);
        /****************** 设置XRecyclerView属性 **************************/

        mNoteListAdapter = new MyNoteListAdapter();
        mNoteListAdapter.setmNotes(noteList);
        rv_list_main.setAdapter(mNoteListAdapter);

        rv_list_main.setLoadingListener(new NoteActivity.MyLoadingListener());
        mNoteListAdapter.setOnItemClickListener(new MyNoteListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                Intent intent = new Intent(getApplication(), NoteInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
        mNoteListAdapter.setOnItemLongClickListener(new MyNoteListAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final Note note) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定删除笔记？");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret = noteDao.deleteNote(note);
                        if (ret > 0){
                            showToast("删除成功");
                            refreshNoteList();
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
            }
        });
    }

    /** 上拉加载和下拉刷新事件 **/
    private class MyLoadingListener implements XRecyclerView.LoadingListener{

        @Override
        public void onRefresh() {//下拉刷新
            rv_list_main.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rv_list_main.refreshComplete();
                }
            }, 1000);
        }

        @Override
        public void onLoadMore() {//上拉加载
            rv_list_main.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rv_list_main.loadMoreComplete();
                }
            }, 1000);
        }
    }

    private void refreshNoteList(){
        noteList = noteDao.queryNotesAll(groupId, user.getId());
        mNoteListAdapter.setmNotes(noteList);
        mNoteListAdapter.notifyDataSetChanged();
    }

    private void searchNoteList(String userId, String title, String detail, String startTime, String endTime) {
        noteList = noteDao.searchNotes(groupId, userId, title,detail, startTime, endTime);
        mNoteListAdapter.setmNotes(noteList);
        mNoteListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_note: //搜索
                final SearchDialog dialog = new SearchDialog(NoteActivity.this, R.style.SearchDialog, user);
                dialog.show();
                dialog.changeToNoteView();
                dialog.setSearchBtnClickListener(new SearchDialog.OnSearchBtnClickListener() {
                    @Override
                    public void onSearchBtClick() {
                        String startDate = dialog.getStartDate();
                        String endDate = dialog.getEndDate();
                        final String detail = dialog.getDetail();
                        final String title = dialog.getTitle();
                        TextView tvEndDate = dialog.getTvEndDate();

                        if(startDate.length() > 0) {
                            startDate += " 00:00:00";
                        }

                        if(endDate.length() > 0) {
                            endDate += " 23:59:59";
                        }

                        if(startDate.length() > 0 && endDate.length() > 0 && startDate.compareTo(endDate) > 0) {
                            tvEndDate.setError("日期选择错误！");
                            return ;
                        }
                        dialog.dismiss();
                        Log.d("search", detail + "-" + startDate + "-" + endDate +"-" + user.getId() + "--" + title);
                        searchNoteList(user.getId(), title, detail, startDate, endDate);
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}