package com.example.hecheng.richengben2.domin;

import java.io.Serializable;

import cn.bmob.v3.BmobUser;

/**
 * Created by HeCheng on 2017/3/23.
 */

public class User extends BmobUser implements Serializable{
    private String id;
    private String password2;

    public String getId() {
        return id;
    }

    public String getPassword2(){
        return this.password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id + "--"+ this.getUsername();
    }
}
