package com.manishtaraiya.bleterm.Scan;


public class SearchElement {
    private String name;
    private String mac_address;
    private int rssi_value;
    private String raw_data;
    public SearchElement(String name, String mac_address, int rssi_value,String raw_data) {
        this.name = name;
        this.mac_address = mac_address;
        this.rssi_value = rssi_value;
        this.raw_data=raw_data;
    }

    public String getRaw_data() {
        return raw_data;
    }

    public void setRaw_data(String raw_data) {
        this.raw_data = raw_data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public int getRssi_value() {
        return rssi_value;
    }

    public void setRssi_value(int rssi_value) {
        this.rssi_value = rssi_value;
    }
}
