package com.example.classtable.bean;

public class Notify {
    private int id;
    private String time;
    private String info;
private String creat_time;
    private int switch_on;
    private int ifVibrate;
    private int ifSound;

    public Notify(int id, String time, String info, int switch_on, int ifVibrate,int ifSound,String creat_time) {
        this.id = id;
        this.time = time;
        this.info = info;
        this.switch_on = switch_on;
        this.ifVibrate = ifVibrate;
        this.ifSound=ifSound;
        this.creat_time=creat_time;
    }

    public int getIfSound() {
        return ifSound;
    }

    public void setIfSound(int ifSound) {
        this.ifSound = ifSound;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreat_time() {
        return creat_time;
    }

    public void setCreat_time(String creat_time) {
        this.creat_time = creat_time;
    }

    public String getTime() {
        return time;
    }

    public Notify() {
    }

    public int getSwitch_on() {
        return switch_on;
    }

    public void setSwitch_on(int switch_on) {
        this.switch_on = switch_on;
    }

    public int getIfVibrate() {
        return ifVibrate;
    }

    public void setIfVibrate(int ifVibrate) {
        this.ifVibrate = ifVibrate;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
