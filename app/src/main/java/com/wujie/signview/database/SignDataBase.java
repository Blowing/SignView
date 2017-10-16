package com.wujie.signview.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wujie.signview.model.SignEntity;

/**
 * Created by Troy on 2017-10-16.
 */

public class SignDataBase extends SQLiteOpenHelper{

    private static final String C_sDataBaseName_Sign = "sign.db";
    private static final int C_iDataBaseVersion = 1;
    public static String C_sDataBaseTable_Name = "t_sign";
    private SQLiteDatabase db;

    public  SignDataBase(Context context) {
        super(context, C_sDataBaseTable_Name, null, C_iDataBaseVersion);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("drop table if exists" + C_sDataBaseTable_Name);
        String table_SQL = "create table" + C_sDataBaseTable_Name
                + "(_id ingeger primary key autoincrement,"
                + "userId text not null," + "popType integer not null,"
                + "hasState integer not null," + "expandFild1 text,"
                + "expandFild2 text," + "expandFild3 text);";
        db.execSQL(table_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + C_sDataBaseTable_Name);
        String table_SQL = "create table " + C_sDataBaseTable_Name
                + " (_id integer primary key autoincrement,"
                + "userId text not null," + "popType integer not null,"
                + "hasState integer not null," + "expandFild1 text,"
                + "expandFild2 text," + "expandFild3 text);";
        db.execSQL(table_SQL);
    }

    public void insert(SignEntity entity) {
        db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SignEntityPer.userId, entity.userId);
        contentValues.put(SignEntityPer.popType, entity.popType);
        contentValues.put(SignEntityPer.hasState, entity.hasState);
        db.insert(C_sDataBaseTable_Name, null,contentValues);
        db.close();
    }

    public SignEntity getSignEntityByUserIDAndPopType(String[] userIDAndType) {
        db = getReadableDatabase();
        Cursor cursor = db.query(C_sDataBaseTable_Name, new String[] {
                SignEntityPer.userId, SignEntityPer.popType,
                SignEntityPer.hasState
        }, SignEntityPer.userId + "=? and" + SignEntityPer.popType + "=? ",
                userIDAndType, null, null, null);
        SignEntity entity = null;
        if (cursor != null && cursor.getCount() > 0) {
            entity = new SignEntity();
            cursor.moveToFirst();
            entity.userId = cursor.getString(0);
            entity.popType = cursor.getInt(1);
            entity.hasState = cursor.getInt(2);
        }
        cursor.close();
        db.close();
        return  entity;
    }

    /**
     * 根据用户ID 来修改用户画笔的颜色
     * @param userID
     * @param painColorType
     */
    public void updatePaintColorByUserID(String[] userID, int painColorType) {
        db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SignEntityPer.hasState, painColorType);
        db.update(C_sDataBaseTable_Name, contentValues, SignEntityPer.userId
        +"=? and" + SignEntityPer.popType + "=" + SignEntity.C_iSignPopType_Paint
        , userID);
        db.close();
    }

    class SignEntityPer {
        public final static String Id = "_id";
        public final static String userId = "userId";
        public final static String popType = "popType";
        public final static String hasState = "hasState";
        public final static String expandFild1 = "expandFild1";
        public final static String expandFild2 = "expandFild2";
        public final static String expandFild3 = "expandFild3";
    }
}
