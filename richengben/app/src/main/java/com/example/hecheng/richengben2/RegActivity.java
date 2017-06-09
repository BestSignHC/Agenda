package com.example.hecheng.richengben2;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hecheng.richengben2.DaoImpl.UserDao;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.common.Encript;
import com.example.hecheng.richengben2.common.Errors;
import com.example.hecheng.richengben2.common.ProgressDialogUtil;
import com.example.hecheng.richengben2.common.Verification;
import com.example.hecheng.richengben2.domin.User;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.example.hecheng.richengben2.R.id.btnRequestCode;
import static com.example.hecheng.richengben2.R.id.btn_code;
import static com.example.hecheng.richengben2.R.id.et_account;
import static com.example.hecheng.richengben2.R.id.et_code;
import static com.example.hecheng.richengben2.R.id.et_pwd;
import static com.example.hecheng.richengben2.common.ProgressDialogUtil.showProcessDialog;

/**
 * Created by HeCheng on 2017/3/22.
 */

public class RegActivity extends AppCompatActivity {

    private final String TAG = "RegActivity";

    @BindView(et_account)
    EditText etAccount;
    @BindView(et_pwd)
    EditText etPwd;
    @BindView(R.id.et_re_pwd)
    EditText etRePwd;
    @BindView(R.id.btn_reg)
    Button btnReg;
    @BindView(R.id.tv_login)
    TextView tvLogin;
    @BindView(R.id.et_code)
    EditText etCode;
    @BindView(R.id.btn_code)
    Button btnCode;

    private String account = null;
    private String pwd = null;
    private String rePwd = null;
    private String phoneCode = null;

    private static boolean canReg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
        ButterKnife.bind(this);
        Bmob.initialize(this, Constants.APP_ID);

        changeModule(true);

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //转到登陆界面
                Intent intent = new Intent(RegActivity.this, LoginActivity.class);
                RegActivity.this.startActivity(intent);
                finish();
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                changeModule(false);

                try {
                    initInput(new BaseListener<Exception>() {
                        @Override
                        public void getSuccess(Exception e) {
                            final UserDao dao = new UserDao();
                            dao.queryUserByAccount(account, new BaseListener<User>() {
                                @Override
                                public void getSuccess(User user) {
                                    if (user == null) { //未注册
                                        changeModule(false);
                                        dao.regUser(account, pwd, new BaseListener<User>() {
                                            @Override
                                            public void getSuccess(User user) {//注册成功
                                                Log.d(TAG, "reg success");
                                                Log.d(TAG, user.toString());
                                                Toast.makeText(RegActivity.this, "注册成功。", Toast.LENGTH_LONG).show();
                                                gotoLogin(user);
                                            }

                                            @Override
                                            public void getFailure(Exception e) { //注册失败
                                                Log.e(TAG, "reg error");
                                                changeModule(true);
                                                e.printStackTrace();
                                            }
                                        });
                                    } else {//已经注册
                                        Log.d(TAG, user.toString());
                                        Toast.makeText(RegActivity.this, "您已注册，请直接登陆。", Toast.LENGTH_LONG).show();
                                        gotoLogin(user);
                                    }
                                }

                                @Override
                                public void getFailure(Exception e) {
                                    changeModule(true);
                                    e.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void getFailure(Exception e) {
                            e.printStackTrace();
                            Toast.makeText(RegActivity.this, "验证码错误", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    changeModule(true);
                }
            }
        });
    }

    /**
     * 转到登陆
     *
     * @param user
     */
    private void gotoLogin(User user) {
        Intent intent = new Intent(RegActivity.this, LoginActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        intent.putExtras(bundle);
        RegActivity.this.startActivity(intent);
        finish();
    }

    /**
     * 获取所有输入，并校验输入的格式
     */
    private void initInput(final BaseListener<Exception> listener) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (etAccount.getText() != null) {
            account = etAccount.getText().toString();
        }
        if (etPwd.getText() != null) {
            pwd = etPwd.getText().toString();
        }
        if (etRePwd.getText() != null) {
            rePwd = etRePwd.getText().toString();
        }
        if (etCode.getText() != null) {
            phoneCode = etCode.getText().toString();
        }

        if (!Verification.isPhoneNum(account)) {
            etAccount.setError(Errors.E_PHONE);
            return;
        }

        if (pwd.length() < 6) {
            etPwd.setError(Errors.E_PWD_S);
            return;
        }

        if (pwd.length() > 16) {
            etPwd.setError(Errors.E_PWD_L);
            return;
        }

        if (!Verification.isPwd(pwd)) {
            etPwd.setError(Errors.E_PWD);
            return;
        }

        if (!rePwd.equals(pwd)) {
            etRePwd.setError(Errors.E_R_PWD);
            return;
        }
        pwd = Encript.encriptByMd5(pwd).trim();

        final Dialog d = ProgressDialogUtil.showProcessDialog("验证短信验证码中，请稍后。。。",RegActivity.this);
        d.show();
        BmobSMS.verifySmsCode(account, phoneCode, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                d.dismiss();
                if(e == null) {
                    listener.getSuccess(null);
                }
                else {
                    listener.getFailure(e);
                }
            }
        });
    }

    private void changeModule(boolean isAviable) {
        btnReg.setClickable(isAviable);
        btnReg.setClickable(canReg);
        if (isAviable) {
            btnReg.setText("注册");
        } else {
            btnReg.setText("注册中，请稍后。。。");
        }

        etAccount.setEnabled(isAviable);
        etPwd.setEnabled(isAviable);
    }

    @OnClick(R.id.btn_code)
    public void sendSMS() {
        canReg = true;
        if (etAccount.getText() == null) {
            etAccount.setError("请输入11位大陆手机号！");
            return;
        }
        account = etAccount.getText().toString();
        if (!Verification.isPhoneNum(account)) {
            etAccount.setError("请输入11位大陆手机号！");
            return;
        }
        BmobSMS.requestSMSCode(account, "您请求的验证码是：", new QueryListener<Integer>() {

            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null) {
                    Toast.makeText(RegActivity.this, "验证码已发送，请注查收！", Toast.LENGTH_SHORT).show();
                    btnCode.setClickable(false);
                    new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            btnCode.setText(millisUntilFinished / 1000 + "秒");
                        }

                        @Override
                        public void onFinish() {
                            btnCode.setClickable(true);
                            btnCode.setText("重新发送");
                        }
                    }.start();
                    Log.d("SMS", integer + "");
                } else {
                    e.printStackTrace();
                    Toast.makeText(RegActivity.this, "发送失败，请检查输入手机号和网络！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
