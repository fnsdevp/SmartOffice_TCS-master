package com.hipla.smartoffice_tcs.networking;

/**
 * Created by Admin on 10/19/2016.
 */
public class NetworkUtility {

    //public static final String BASEURL="http://eegrab.com/ezapp/beta/";
    public static final String BASEURL="http://gohipla.com/tcs_ws/";
    public static final String IMAGE_BASEURL="http://edu.gohipla.com/admin/apps/webcam/";

    public static final String LOGIN = "login.php";
    public static final String SIGNUP = "register.php";
    public static final String DEVICE_UPDATE = "get_regkey.php";
    public static final String CHECK_TIME = "checkTime.php";
    public static final String GET_AVAILABLE_TIME_BY_USERID = "get_time_by_dateNuserid.php";
    public static final String GUEST_GET_AVAILABLE_TIME_BY_PHONE = "get_time_by_numberNdate.php";
    public static final String GUEST_GET_AVAILABLE_ROOMS = "RoomsAvailable.php";
    public static final String UPCOMING_MEETINGS = "upcoming_appointments.php";
    public static final String UPCOMING_MEETINGS_BY_DATE = "get_todaysMeetings.php";
    public static final String BOOK_MEETING_FIXED = "bookNew.php";
    public static final String GUEST_ALL_APPOINTMENTS = "all_appointments.php";
    public static final String GUEST_REQUESTED_APPOINTMENTS = "all_request_appointments.php";
    public static final String SET_MEETING_STATUS = "set_status.php";
    public static final String CREATE_REVIEW = "createReview.php";
    public static final String GET_ALL_REVIEW = "all_reviewByapp.php";
    public static final String GET_ALL_MESSAGES = "all_messages.php";
    public static final String CREATE_MESSAGES = "createMsg.php";
    public static final String UPDATE_MESSAGES_STATUS = "set_read.php";
    public static final String CHANGE_PASSWORD = "changepassword.php";
    public static final String GET_AVAILABLE_TIME = "get_time.php";
    public static final String SET_AVAILABLE_TIME = "set_avail.php";
    public static final String OPEN_DOOR = "door_status.php";
    public static final String OPEN_DOOR_THIRED_FLOOR = "door_3rd_floor.php";
    public static final String RESCHEDULE_MEETING = "reschedule.php";
    public static final String REGISTER_GEO_FENCING = "get_distance.php";
    public static final String ADD_BELONGINGS = "add_belongings.php";
    public static final String ADD_RECIPIENTS = "add_recp.php";
    public static final String DELETE_BELONGINGS = "delete_belongings.php";
    public static final String LIGHT_OPEN_ONE = "http://192.168.1.16/control?cmd=GPIO,12,1";
    public static final String LIGHT_OPEN_TWO = "http://192.168.1.16/control?cmd=GPIO,4,1";
    public static final String LIGHT_OPEN_THREE = "http://192.168.1.16/control?cmd=GPIO,5,1";
    public static final String LIGHT_CLOSE_ONE = "http://192.168.1.16/control?cmd=GPIO,12,0";
    public static final String LIGHT_CLOSE_TWO = "http://192.168.1.16/control?cmd=GPIO,4,0";
    public static final String LIGHT_CLOSE_THREE = "http://192.168.1.16/control?cmd=GPIO,5,0";

    public static final String DEVICE_TYPE = "Android";
    public static final String TOKEN = "deviceToken";
    public static final String USER_INFO = "userInfo";
}
