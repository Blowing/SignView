package com.wujie.signview.model;

/**
 * Created by Troy on 2017-10-16.
 */

public class SignEntity {
    /**
     * 显示的类型 签章提示
     */
    public final static int C_iSignPopType_Sign = 0;
    /**
     * 显示类型 光标提示
     */
    public final static int C_iSignPopType_Cursor = 1;

    /**
     * 选择笔的颜色
     */
    public final static int C_iSignPopType_Paint = 1001;

    /**
     * 笔的颜色 黑色
     */
    public final static int C_iSignPiantColor_Black = 10;
    /**
     * 笔的颜色 红色
     */
    public final static int C_iSignPiantColor_Red = 11;
    /**
     * 笔的颜色 蓝色
     */
    public final static int C_iSignPiantColor_Blue = 12;
    /**
     * userID
     */
    public String userId;
    /**
     * 提示的某一个点
     */
    public int popType;
    /**
     * 是否显示提示 0 为不提示，1为显示 如果 poptype 为 C_iSignPopType_Paint=1001 hasState则代表的是
     * 选择笔的颜色 10 为黑色 11 为红色 12 为蓝色
     */
    public int hasState;

    public SignEntity() {
    }
}

