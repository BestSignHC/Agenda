package com.example.hecheng.richengben2.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HeCheng on 2017/3/24.
 */

public class Constants {

    public static final String DATABASE_PATH = "/data/data/com.hcheng.db/databases/app.db";
    public static final String APP_ID = "6f297ed92cf81ace6568818e75c1a76e";
//    public static final String APP_ID = "0f3a1b39e06c392fa9372da6e5fa7ecb";
//    public static final String APP_ID = "e37b0456122750dd747e4da5c2b74f68";
    public static final int COLOR_RED = 1;
    public static final int COLOR_GREEN = 2;
    public static final int COLOR_BLUE = 3;
    public static final int COLOR_BLACK = 4;
    public static final int COLOR_WHITE = 0;

    //日程状态：0-待完成、1-完成、-1-取消
    public enum SCHEDULE_STATUS {
        WAIT(0), DONE(1), CANCLE(-1);

        private int value;

        SCHEDULE_STATUS(Integer value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    //计划状态： 0-新建 1-完成 -1-点击添加 2-延误
    public enum PLAN_STATUS {
        WAIT(0), DONE(1), ADD_PLAN(-1), DELAY(2);

        private int value;

        PLAN_STATUS(Integer value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    //计划种类： 0-周计划 1-月计划 2-子计划
    public enum PLAN_YPE {
        WEEK(0), MONTH(1), CHILD(2);

        private int value;

        PLAN_YPE(Integer value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static final Map<Integer,String> colorMap;
    public static final Map<Integer, String> textMap;

    static {
        colorMap = new HashMap<Integer, String>();
        colorMap.put(COLOR_RED, "#ff0000");
        colorMap.put(COLOR_GREEN, "#00ff00");
        colorMap.put(COLOR_BLUE, "#0000ff");
        colorMap.put(COLOR_BLACK, "#000000");
        colorMap.put(COLOR_WHITE, "#ffffff");
    }

    static {
        textMap = new HashMap<Integer, String>();
        textMap.put(COLOR_RED, "健康");
        textMap.put(COLOR_GREEN, "私事");
        textMap.put(COLOR_BLUE, "工作");
        textMap.put(COLOR_BLACK, "杂事");
    }

}
