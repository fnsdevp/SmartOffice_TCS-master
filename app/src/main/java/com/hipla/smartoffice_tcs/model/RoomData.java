package com.hipla.smartoffice_tcs.model;

/**
 * Created by FNSPL on 2/22/2018.
 */

public class RoomData {

    public RoomData(String roomName){
        this.roomName = roomName;
    }

    private String roomName;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
