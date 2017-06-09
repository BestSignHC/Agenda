package com.example.hecheng.richengben2.View;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hecheng.richengben2.R;
import com.example.hecheng.richengben2.common.Encript;
import com.example.hecheng.richengben2.common.Errors;
import com.example.hecheng.richengben2.common.Verification;
import com.example.hecheng.richengben2.domin.User;

import org.w3c.dom.Text;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static cn.bmob.v3.BmobUser.getCurrentUser;

/**
 * Created by HeCheng on 2017/4/14.
 */

public class ResetDialog extends Dialog {

    private ImageButton btnClose;
    private ImageButton btnProve;
    private EditText etCode;
    private Button btnRequestCode;
    private TextView textView;
    private EditText etResPwd;

    private User user;
    private String phoneNum = "";
    private String code = "";
    private String resPwd = "123456";
    private SharedPreferences sp;

    Context context;

    public ResetDialog(Context context) {
        super(context);
        this.context = context;
    }

    public ResetDialog(Context context, int theme, User user, SharedPreferences sp) {
        super(context, theme);
        this.context = context;
        this.user = user;
        this.phoneNum = user.getUsername();
        this.sp = sp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.resetpwd_dialog_layout);
        initView();

    }

    @Override
    public void show() {
        super.show();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initView() {
        btnClose = (ImageButton) findViewById(R.id.res_cancel);
        btnProve = (ImageButton) findViewById(R.id.res_prove);
        etCode = (EditText) findViewById(R.id.res_code);
        btnRequestCode = (Button) findViewById(R.id.btnRequestCode);
        textView = (TextView) findViewById(R.id.res_account);
        etResPwd = (EditText) findViewById(R.id.res_pwd);
        textView.setText(phoneNum);

        btnRequestCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etResPwd.getText() == null) {
                    etResPwd.setError("请输入新密码");
                    return;
                }
                resPwd = etResPwd.getText().toString();
                if (resPwd.length() < 6) {
                    etResPwd.setError(Errors.E_PWD_S);
                    return;
                }

                if (resPwd.length() > 16) {
                    etResPwd.setError(Errors.E_PWD_L);
                    return;
                }

                if (!Verification.isPwd(resPwd)) {
                    etResPwd.setError(Errors.E_PWD);
                    return;
                }

                BmobSMS.requestSMSCode(phoneNum, "您请求的验证码是：", new QueryListener<Integer>() {

                    @Override
                    public void done(Integer integer, BmobException e) {
                        if (e == null) {
                            Toast.makeText(context, "验证码已发送，请注查收！", Toast.LENGTH_SHORT).show();
                            btnRequestCode.setClickable(false);
                            new CountDownTimer(60000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    btnRequestCode.setText(millisUntilFinished / 1000 + "秒");
                                }

                                @Override
                                public void onFinish() {
                                    btnRequestCode.setClickable(true);
                                    btnRequestCode.setText("重新发送");
                                }
                            }.start();
                            Log.d("SMS", integer + "");
                        } else {
                            e.printStackTrace();
                            Toast.makeText(context, "发送失败，请检查网络设置！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnProve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etResPwd.getText() == null) {
                    etResPwd.setError("请输入新密码");
                    return;
                }
                resPwd = etResPwd.getText().toString();
                if (resPwd.length() < 6) {
                    etResPwd.setError(Errors.E_PWD_S);
                    return;
                }

                if (resPwd.length() > 16) {
                    etResPwd.setError(Errors.E_PWD_L);
                    return;
                }

                if (!Verification.isPwd(resPwd)) {
                    etResPwd.setError(Errors.E_PWD);
                    return;
                }

                if (etCode.getText() == null) {
                    etCode.setError("请输入验证码");
                    return;
                } else {
                    code = etCode.getText().toString();
                    BmobSMS.verifySmsCode(phoneNum, code, new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    User u = new User();
                                    u.setUsername(user.getUsername());
                                    u.setPassword(user.getPassword2());
                                    u.login(new SaveListener<Object>() {
                                        @Override
                                        public void done(Object o, BmobException e) {
                                            BmobUser bu = BmobUser.getCurrentUser();
                                            if (e == null) {
                                                final String pwd = resPwd;
                                                try {
                                                    resPwd = Encript.encriptByMd5(resPwd).trim();
                                                } catch (Exception e1) {
                                                }
                                                user.setPassword(resPwd);
                                                user.setPassword2(resPwd);
                                                User newUser = new User();
                                                newUser.setPassword(resPwd);
                                                newUser.setSessionToken(bu.getSessionToken());
                                                newUser.setPassword2(resPwd);
                                                newUser.update(user.getObjectId(), new UpdateListener() {
                                                    @Override
                                                    public void done(BmobException e) {
                                                        if (e == null) {
                                                            SharedPreferences.Editor editor = sp.edit();
                                                            editor.putString("USER_NAME", user.getUsername());
                                                            editor.putString("PASSWORD", user.getPassword2());
                                                            editor.commit();
                                                            Toast.makeText(context, "密码已重置为 " + pwd + "，请登录！", Toast.LENGTH_SHORT).show();
                                                            dismiss();
                                                        } else {
                                                            e.printStackTrace();
                                                            Toast.makeText(context, "密码重置失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                e.printStackTrace();
                                                Toast.makeText(context, "验证码错误！", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                    );
                }
            }
        });
    }
}
