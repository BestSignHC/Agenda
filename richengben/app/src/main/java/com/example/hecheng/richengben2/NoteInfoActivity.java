package com.example.hecheng.richengben2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.hecheng.richengben2.DaoImpl.GroupDao;
import com.example.hecheng.richengben2.DaoImpl.NoteDao;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.FileUtil;
import com.example.hecheng.richengben2.common.StringUtils;
import com.example.hecheng.richengben2.domin.Group;
import com.example.hecheng.richengben2.domin.Note;
import com.example.hecheng.richengben2.domin.User;
import com.sendtion.xrichtext.RichTextView;
import com.sendtion.xrichtext.SDCardUtil;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 笔记详情
 */
public class NoteInfoActivity extends BaseActivity {

    private TextView tv_note_title;//笔记标题
    private RichTextView tv_note_content;//笔记内容
    private TextView tv_note_time;//笔记创建时间
    private TextView tv_note_group;//选择笔记分类
    //private ScrollView scroll_view;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;
    private String myGroupName;
    private NoteDao noteDao;
    private GroupDao groupDao;

    private ProgressDialog loadingDialog;
    private Subscription subsLoading;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        initView();

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_dialog_info);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        noteDao = new NoteDao(this);
        groupDao = new GroupDao(this);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);

        tv_note_title = (TextView) findViewById(R.id.tv_note_title);//标题
        tv_note_title.setTextIsSelectable(true);
        tv_note_content = (RichTextView) findViewById(R.id.tv_note_content);//内容
        tv_note_time = (TextView) findViewById(R.id.tv_note_time);
        tv_note_group = (TextView) findViewById(R.id.tv_note_group);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");
        user = (User) intent.getSerializableExtra("user");

        myTitle = note.getTitle();
        myContent = note.getContent();
        Group group = groupDao.queryGroupById(note.getGroupId());
        myGroupName = group.getName();

        tv_note_title.setText(myTitle);
        tv_note_content.post(new Runnable() {
            @Override
            public void run() {
                //showEditData(myContent);
                tv_note_content.clearAllLayout();
                showDataSync(note);
            }
        });
        tv_note_time.setText(note.getCreateTime());
        tv_note_group.setText(myGroupName);
        setTitle("笔记详情");

    }

    /**
     * 异步方式显示数据
     * @param note
     */
    private void showDataSync(final Note note){
        loadingDialog.show();

        subsLoading = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                showEditData(subscriber, note);
            }
        })
        .onBackpressureBuffer()
        .subscribeOn(Schedulers.io())//生产事件在io
        .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
        .subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                loadingDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                loadingDialog.dismiss();
                e.printStackTrace();
                showToast("解析错误：图片不存在或已损坏");
            }

            @Override
            public void onNext(String text) {
                if (text.contains(SDCardUtil.getPictureDir())){
                    tv_note_content.addImageViewAtIndex(tv_note_content.getLastIndex(), text);
                } else {
                    tv_note_content.addTextViewAtIndex(tv_note_content.getLastIndex(), text);
                }
            }
        });

    }

    /**
     * 显示数据
     */
    private void showEditData(final Subscriber<? super String> subscriber, Note note) {
        try {
            String html = note.getContent();
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                if (text.contains("<img") && text.contains("src=")) {
                    final String imagePath = StringUtils.getImgSrc(text);
                    if (new File(imagePath).exists()) {
                        subscriber.onNext(imagePath);
                    } else {
                        final String fileName = note.getImgName();
                        String imgLocalPath = note.getLocalImgPath();
                        String imgUrl = note.getImgUrl();
                        FileUtil.downloadBmobFile(fileName, imgUrl, imgLocalPath, new BaseListener<Exception>() {
                            @Override
                            public void getSuccess(Exception e) {
                                subscriber.onNext(imagePath);
                            }

                            @Override
                            public void getFailure(Exception e) {
                                showToast("抱歉，图片"+fileName+"已丢失，请重新插入！");
                            }
                        });
                    }
                } else {
                    subscriber.onNext(text);
                }
            }
            subscriber.onCompleted();
        } catch (Exception e){
            e.printStackTrace();
            subscriber.onError(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_note_edit://编辑笔记
                Intent intent = new Intent(NoteInfoActivity.this, NewNoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                intent.putExtra("flag", 1);//编辑笔记
                intent.putExtra("user", user);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
