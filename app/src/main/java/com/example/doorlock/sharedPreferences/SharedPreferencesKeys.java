package com.example.doorlock.sharedPreferences;

public enum SharedPreferencesKeys {

    FIRST_RUN("firstRun","true"),

    TXPOWER("txPower","-60"),
    PERIODICITY("periodicity","3"),
    TRANSMIT_RULE("rule","ALWAYS"),

    USER_ACTIVATED("userActivated","false"),
    USER_ID("userID",""),
    USER_MAC("userMAC",""),
    USER_PASSWORD("userPassword",""),
    USER_ACCESS_LVL("userAccessLvl","0"),
    USER_NAME("userName","");

    private final String text;
    private final String defaultVal;

    SharedPreferencesKeys(final String text, final String defaultVal) {
        this.text = text;
        this.defaultVal = defaultVal;
    }

    public String getKey(){
        return text;
    }

    public String getDefaultVal() {
        return defaultVal;
    }
}
