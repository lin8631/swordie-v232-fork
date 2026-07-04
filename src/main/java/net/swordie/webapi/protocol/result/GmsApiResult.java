package net.swordie.webapi.protocol.result;

public class GmsApiResult {

    public static final int SUCCESS_CODE = 20000;

    public int code;
    public String message;
    public Object data;

    public static GmsApiResult success(Object data) {
        GmsApiResult result = new GmsApiResult();
        result.code = SUCCESS_CODE;
        result.message = "success";
        result.data = data;
        return result;
    }

    public static GmsApiResult success() {
        return success(null);
    }

    public static GmsApiResult error(int code, String message) {
        GmsApiResult result = new GmsApiResult();
        result.code = code;
        result.message = message;
        result.data = null;
        return result;
    }
}
