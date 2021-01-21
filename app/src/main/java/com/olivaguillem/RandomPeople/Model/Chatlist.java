package com.olivaguillem.RandomPeople.Model;

import java.util.Date;

public class Chatlist implements Comparable<Chatlist> {

    public String id;
    public long time;
    public int checkpoint;
    public boolean checkpoint1, checkpoint2, checkpoint1Msg, checkpoint2Msg, checkpoint2AcceptMsg,writecorrectly;

    public Chatlist(String id, long time, int checkpoint, boolean checkpoint1, boolean checkpoint2, boolean checkpoint1Msg, boolean checkpoint2Msg, boolean checkpoint2AcceptMsg, boolean writecorrectly) {
        this.id = id;
        this.time = time;
        this.checkpoint = checkpoint;
        this.checkpoint1 = checkpoint1;
        this.checkpoint2 = checkpoint2;
        this.checkpoint1Msg = checkpoint1Msg;
        this.checkpoint2Msg = checkpoint2Msg;
        this.checkpoint2AcceptMsg = checkpoint2AcceptMsg;
        this.writecorrectly = writecorrectly;
    }

    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int compareTo(Chatlist chatlist) {
        return (int) (chatlist.getTime() - getTime());
    }

    public int getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(int checkpoint) {
        this.checkpoint = checkpoint;
    }

    public boolean isCheckpoint1() {
        return checkpoint1;
    }

    public void setCheckpoint1(boolean checkpoint1) {
        this.checkpoint1 = checkpoint1;
    }

    public boolean isCheckpoint2() {
        return checkpoint2;
    }

    public void setCheckpoint2(boolean checkpoint2) {
        this.checkpoint2 = checkpoint2;
    }

    public boolean isCheckpoint1Msg() {
        return checkpoint1Msg;
    }

    public void setCheckpoint1Msg(boolean checkpoint1Msg) {
        this.checkpoint1Msg = checkpoint1Msg;
    }

    public boolean isCheckpoint2Msg() {
        return checkpoint2Msg;
    }

    public void setCheckpoint2Msg(boolean checkpoint2Msg) {
        this.checkpoint2Msg = checkpoint2Msg;
    }

    public boolean isCheckpoint2AcceptMsg() {
        return checkpoint2AcceptMsg;
    }

    public void setCheckpoint2AcceptMsg(boolean checkpoint2AcceptMsg) {
        this.checkpoint2AcceptMsg = checkpoint2AcceptMsg;
    }

    public boolean isWritecorrectly() {
        return writecorrectly;
    }

    public void setWritecorrectly(boolean writecorrectly) {
        this.writecorrectly = writecorrectly;
    }
}
