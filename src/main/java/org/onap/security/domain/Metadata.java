package org.onap.security.domain;

public class Metadata {

    private String username;        //device username

    private String password;        //device password

    private String audioIp;         //The audio's ip

    private String deviceIp;        //The device's ip

    private String esn;             //The serial number of the device

    private String deviceSite;      //The location of the device

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAudioIp() {
        return audioIp;
    }

    public void setAudioIp(String audioIp) {
        this.audioIp = audioIp;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getEsn() {
        return esn;
    }

    public void setEsn(String esn) {
        this.esn = esn;
    }

    public String getDeviceSite() {
        return deviceSite;
    }

    public void setDeviceSite(String deviceSite) {
        this.deviceSite = deviceSite;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", audioIp='" + audioIp + '\'' +
                ", deviceIp='" + deviceIp + '\'' +
                ", esn='" + esn + '\'' +
                ", deviceSite='" + deviceSite + '\'' +
                '}';
    }
}
