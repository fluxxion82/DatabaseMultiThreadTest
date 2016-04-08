package com.test.dbthread.model;

/**
 * Created by salbury on 4/7/16.
 */
public class User {

    private long mId;
    private int mAge;
    private String mName;
    private String mAka;

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        mAge = age;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAka() {
        return mAka;
    }

    public void setAka(String name) {
        mAka = mAka;
    }
}