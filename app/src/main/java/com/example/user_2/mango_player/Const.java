package com.example.user_2.mango_player;

/**
 * Created by User_2 on 21.02.2017.
 */

public class Const {

    public static String LAST_FM_KEY = "c332d034f0b608c3ae3f2327635a9542";

    public static String RADIO_PATH = "http://lin3.ash.fast-serv.com:6026/stream_96";
    public static String TRACK_INFO_URL = "http://199.58.163.11/rr/now_playing.php";

    public static String SERVER_PHOTO_PATH = "http://pavelmikhaylov.ru/radio/";
    public static String PHOTO_TYPE = ".jpg";
    public static int SERVER_PHOTOS_COUNT = 16;

    public static int PHOTO_LOAD_REFRESH_TIME = 20000;

    public static int VIBRATE_TIME = 5;

    public interface ACTION {
        String MAIN_ACTION = "mgc.tockabillyradio.action.main";
        String PLAY_ACTION = "mgc.tockabillyradio.action.play";
        String STARTFOREGROUND_ACTION = "mgc.tockabillyradio.action.startforeground";
        String STOPFOREGROUND_ACTION = "mgc.tockabillyradio.action.stopforeground";
    }
    public static int FOREGROUND_SERVICE = 101;
}
