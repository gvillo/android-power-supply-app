package villo.com.ar.powersupplynotifier.model;

/**
 * Created by villo on 18/1/16.
 */
public class UpsResponse {
    private UpsValues values;
    private String infoMessage;
    private String errorMessage;

    public UpsValues getValues() {
        return values;
    }

    public void setValues(UpsValues values) {
        this.values = values;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String message) {
        this.infoMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


}
