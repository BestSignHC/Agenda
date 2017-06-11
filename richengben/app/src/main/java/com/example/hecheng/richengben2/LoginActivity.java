package com.example.hecheng.richengben2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hecheng.richengben2.DaoImpl.UserDao;
import com.example.hecheng.richengben2.View.ResetDialog;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.common.Constants;
import com.example.hecheng.richengben2.common.Encript;
import com.example.hecheng.richengben2.common.Errors;
import com.example.hecheng.richengben2.common.Verification;
import com.example.hecheng.richengben2.domin.User;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

import static com.example.hecheng.richengben2.R.id.et_account;
import static com.example.hecheng.richengben2.R.id.et_pwd;

/**
 * 登陆Activity
 */
public class LoginActivity extends AppCompatActivity {

    private final String TAG  = "LoginActivity";

    @BindView(et_account) EditText etAccount;
    @BindView(et_pwd) EditText etPwd;
    @BindView(R.id.btn_login) Button btnLogin;
    @BindView(R.id.rem_pwd) CheckBox remPwd;
    @BindView(R.id.auto_login) CheckBox autoLogin;
    @BindView(R.id.tv_reg) TextView tvReg;
    @BindView(R.id.find_back) TextView tvFindBack;

    private boolean isRemPwd = false;
    private boolean isAutoLogin = false;
    private String account = null;
    private String pwd = null;
    private SharedPreferences sp;
    private  boolean isPwdFromSp = false; //如果密码是从SP读出来的，就不加密

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login_layout);
        ButterKnife.bind(this);
        Bmob.initialize(this, Constants.APP_ID);

        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        Intent intent = this.getIntent();
        User user=(User)intent.getSerializableExtra("user");
        if(user != null) {
            etAccount.setText(user.getUsername());
        }
        else {
            isAutoLogin = sp.getBoolean("isAutoLogin", false);
            autoLogin.setChecked(isAutoLogin);
            User currentUser = BmobUser.getCurrentUser(User.class);
            if( isAutoLogin &&  currentUser != null) {
                gotoDateActivity(currentUser);
                Log.d(TAG, "currentUser = " + currentUser.toString());
            }

            String USER_NAME = sp.getString("USER_NAME", "");
            pwd = sp.getString("PASSWORD", "");
            isRemPwd = sp.getBoolean("isRemPwd",false);
            etAccount.setText(USER_NAME);
            if(isRemPwd){
                etPwd.setText("123456");
            }
            remPwd.setChecked(isRemPwd);
            isPwdFromSp = isRemPwd;
        }

        changeModule(true);

        tvReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //转到注册界面
                Intent intent = new Intent(LoginActivity.this, RegActivity.class);
                LoginActivity.this.startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeModule(false);

                try{
                    if( !initInput()) {
                        changeModule(true);
                        return ;
                    }
                }
                catch(Exception e) {
                    changeModule(true);
                }
                UserDao dao = new UserDao();
                dao.login(account, pwd, new BaseListener<User>() {
                    @Override
                    public void getSuccess(User user) {
                        if(user != null) {
                            if(isRemPwd) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("USER_NAME", user.getUsername());
                                editor.putString("PASSWORD",user.getPassword2());
                                editor.putBoolean("isAutoLogin", isAutoLogin);
                                editor.putBoolean("isRemPwd", isRemPwd);
                                editor.commit();
                            }
                            gotoDateActivity(user);
                        }else {
                            Toast.makeText(LoginActivity.this, "登陆失败，账号或密码错误！", Toast.LENGTH_SHORT).show();
                            changeModule(true);
                        }
                    }

                    @Override
                    public void getFailure(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "登陆失败，账号或密码错误！", Toast.LENGTH_SHORT).show();
                        changeModule(true);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPwdFromSp = false;
    }

    @OnClick(R.id.rem_pwd)
    public void checkIsRemPwd() {
        if( remPwd.isChecked()) {
            isRemPwd = true;
        }
        else{
            isRemPwd = false;
        }
    }

    @OnClick(R.id.auto_login)
    public void checkAutoLogin() {
        if(autoLogin.isChecked()){
            remPwd.setChecked(true);
            isAutoLogin = true;
        }
        else{
            isAutoLogin = false;
        }
    }

    /**
     * 获取所有输入，并校验输入的格式
     */
    private boolean initInput() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (etAccount.getText() != null) {
            account =  etAccount.getText().toString();
        }
        if (!isPwdFromSp && etPwd.getText() != null) {
            pwd =  etPwd.getText().toString();
        }

        if (!Verification.isPhoneNum(account))  {
            etAccount.setError(Errors.E_PHONE);
            return false;
        }

        if(!isPwdFromSp){
            if (pwd.length() < 6) {
                etPwd.setError(Errors.E_PWD_S);
                return false;
            }

            if (pwd.length() > 16) {
                etPwd.setError(Errors.E_PWD_L);
                return false;
            }

            if ( !Verification.isPwd(pwd)) {
                etPwd.setError(Errors.E_PWD);
                return false;
            }
        }

        isRemPwd = remPwd.isChecked();
        isAutoLogin = autoLogin.isChecked();

        if(!isPwdFromSp){
            pwd = Encript.encriptByMd5(pwd).trim();
        }
        return true;
    }

    private void gotoDateActivity (User user){

        Toast.makeText(LoginActivity.this, "欢迎，"+ user.getUsername(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, user.toString());

        Intent intent = new Intent(LoginActivity.this, DateAcitvity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        intent.putExtras(bundle);
        LoginActivity.this.startActivity(intent);
        finish();
    }

    private void changeModule(boolean isAviable){
        btnLogin.setClickable(isAviable);
        if(isAviable){
            btnLogin.setText("登陆");
        }
        else{
            btnLogin.setText("登陆中，请稍后。。。");
        }

        etAccount.setEnabled(isAviable);
        etPwd.setEnabled(isAviable);
        remPwd.setEnabled(isAviable);
        autoLogin.setEnabled(isAviable);
    }

    @OnClick(R.id.find_back)
    public void reSetPwd(){
        String phoneNum = "";
        if (etAccount.getText() == null) {
            etAccount.setError("请输入注册的手机号码");
            return;
        }
        else {
            phoneNum = etAccount.getText().toString();
        }
        if (!Verification.isPhoneNum(phoneNum))  {
            etAccount.setError(Errors.E_PHONE);
            return;
        }

        //检测是否注册
        UserDao dao = new UserDao();
        dao.queryUserByAccount(phoneNum, new BaseListener<User>() {
            @Override
            public void getSuccess(User user) {
                final ResetDialog dialog = new ResetDialog(LoginActivity.this, R.style.SearchDialog, user, sp);
                dialog.show();
            }

            @Override
            public void getFailure(Exception e) {
                Toast.makeText(LoginActivity.this, "该手机号尚未注册，请前往注册后登陆", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
