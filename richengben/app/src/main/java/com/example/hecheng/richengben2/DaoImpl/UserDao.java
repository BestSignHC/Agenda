package com.example.hecheng.richengben2.DaoImpl;

import android.util.Log;

import com.example.hecheng.richengben2.Dao.IUserDao;
import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.domin.User;

import java.util.List;
import java.util.UUID;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by HeCheng on 2017/3/23.
 */

public class UserDao implements IUserDao{

    @Override
    public void regUser(String account, String pwd, final BaseListener<User> listener) {
        User regUser = new User();
        regUser.setId(UUID.randomUUID().toString());
        regUser.setUsername(account);
        regUser.setPassword(pwd);
        regUser.setPassword2(pwd);
        regUser.signUp(new SaveListener<User>() {

            @Override
            public void done(User user, BmobException e) {
                if(e == null) {
                    listener.getSuccess(user);
                }else {
                    listener.getFailure(e);
                }
            }
        });
    }

    @Override
    public void login(String account, String pwd, final BaseListener<User> listener) {
        final User loginUser = new User();
        loginUser.setUsername(account);
        loginUser.setPassword(pwd);
        loginUser.setPassword2(pwd);

        Log.d("login", account);
        Log.d("login", pwd);

        loginUser.login(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if(e == null) {
                    listener.getSuccess(user);
                }else {
                    listener.getFailure(e);
                }
            }
        });
    }

    @Override
    public void queryUserByAccount(String account,final BaseListener<User> listener) {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username", account);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e == null){
                    if(object ==null || object.size() < 1){
                        listener.getSuccess(null);
                    }else{
                        listener.getSuccess(object.get(0));
                    }
                }else{
                    listener.getFailure(e);
                }
            }
        });
    }

    @Override
    public void queryUserById(String id, final BaseListener<User> listener) {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("id", id);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e == null){
                    if(object ==null || object.size() < 1){
                        listener.getSuccess(null);
                    }else{
                        listener.getSuccess(object.get(0));
                    }
                }else{
                    listener.getFailure(e);
                }
            }
        });
    }

    @Override
    public void queryUserByAccountAndPwd(String account, String pwd, final BaseListener<User> listener) {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username", account);
        query.addWhereEqualTo("password", pwd);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e == null){
                    if(object ==null || object.size() < 1){
                        listener.getSuccess(null);
                    }else{
                        listener.getSuccess(object.get(0));
                    }
                }else{
                    listener.getFailure(e);
                }
            }
        });
    }
}
