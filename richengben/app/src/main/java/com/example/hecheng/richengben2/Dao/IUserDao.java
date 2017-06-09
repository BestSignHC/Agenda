package com.example.hecheng.richengben2.Dao;

import com.example.hecheng.richengben2.common.BaseListener;
import com.example.hecheng.richengben2.domin.User;

/**
 * Created by HeCheng on 2017/3/24.
 */

public interface IUserDao {

    public void regUser(String account, String pwd, BaseListener<User> listener);

    public void login(String account, String pwd, BaseListener<User> listener);

    public void queryUserByAccount(String account, BaseListener<User> listener);

    public void queryUserById(String id, BaseListener<User> listener);

    public void queryUserByAccountAndPwd(String account, String pwd, BaseListener<User> listener);
}
