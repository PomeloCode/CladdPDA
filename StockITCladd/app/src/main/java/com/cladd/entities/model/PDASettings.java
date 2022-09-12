package com.cladd.entities.model;


public class PDASettings {

    // Set EPC Baseband Param  lis

    public int baseband;
    public int qValue;
    public int session;
    public int flag;

    // Set Ant Power Param lis

    public int antCount;
    public int antPower;

    //Set Frequency lis

    public int frequency;

    //Tag Upload

    public int repeatTimeFilter;
    public int rssiFilter;

    //Auto Idle Mode
    public boolean isOpen;
    public int timeAutoIdle;

    // Set Ant Power Param

    // Parameter 1:

    public String rfu;  // bit3-bit0: rfu
    public boolean tagFocus;  // bit4: tag focus enable
    public boolean fastID;  // bit5: fast id enable
    public String rfu2;  // bit31-bit6: rfu

    // Parameter 2:
    public String maxQ; //Byte 1:
    public String minQ; //Byte 2:
    public String tmult; //Byte 3:
    public String DynamicStartQenable; //Byte 4:

    // Parameter 3:
    public String antenna; // Byte 1:  switching mode. 0--Switch immediately without tags,1--Running out of resistance time
    public String numberOfRetries; // Byte 2:  of  (Switch immediately without tags mode)
    public String maxAntennaResistancetime; // Byte 3-4: -endian format composes U16,(x10ms)

    // Parameter 4：

    public String waitingTimeAntSwitch; // Byte 1: Waiting time between antenna switching(x10ms)
    public String antennaSwitchingSequence; // Byte 2: antenna switching sequence
    public String antennaProtectionThreshold; // Byte 3: Antenna protection threshold (unit is dBm), set to 0 to disableprotection. Byte 4: reserved

    // Parameter 5：

    public String LBTMode; // 0: disable, 1: listening only, 2: read tag after listening, 3: read tag after meeting RSSI
    public String RSSIMaxVal;

    public int getBaseband() {
        return baseband;
    }

    public void setBaseband(int baseband) {
        this.baseband = baseband;
    }

    public int getqValue() {
        return qValue;
    }

    public void setqValue(int qValue) {
        this.qValue = qValue;
    }

    public int getSession() {
        return session;
    }

    public void setSession(int session) {
        this.session = session;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getAntCount() {
        return antCount;
    }

    public void setAntCount(int antCount) {
        this.antCount = antCount;
    }

    public int getAntPower() {
        return antPower;
    }

    public void setAntPower(int antPower) {
        this.antPower = antPower;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getRepeatTimeFilter() {
        return repeatTimeFilter;
    }

    public void setRepeatTimeFilter(int repeatTimeFilter) {
        this.repeatTimeFilter = repeatTimeFilter;
    }

    public int getRssiFilter() {
        return rssiFilter;
    }

    public void setRssiFilter(int rssiFilter) {
        this.rssiFilter = rssiFilter;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getTimeAutoIdle() {
        return timeAutoIdle;
    }

    public void setTimeAutoIdle(int timeAutoIdle) {
        this.timeAutoIdle = timeAutoIdle;
    }

    public String getRfu() {
        return rfu;
    }

    public void setRfu(String rfu) {
        this.rfu = rfu;
    }

    public boolean isTagFocus() {
        return tagFocus;
    }

    public void setTagFocus(boolean tagFocus) {
        this.tagFocus = tagFocus;
    }

    public boolean isFastID() {
        return fastID;
    }

    public void setFastID(boolean fastID) {
        this.fastID = fastID;
    }

    public String getRfu2() {
        return rfu2;
    }

    public void setRfu2(String rfu2) {
        this.rfu2 = rfu2;
    }

    public String getMaxQ() {
        return maxQ;
    }

    public void setMaxQ(String maxQ) {
        this.maxQ = maxQ;
    }

    public String getMinQ() {
        return minQ;
    }

    public void setMinQ(String minQ) {
        this.minQ = minQ;
    }

    public String getTmult() {
        return tmult;
    }

    public void setTmult(String tmult) {
        this.tmult = tmult;
    }

    public String getDynamicStartQenable() {
        return DynamicStartQenable;
    }

    public void setDynamicStartQenable(String dynamicStartQenable) {
        DynamicStartQenable = dynamicStartQenable;
    }

    public String getAntenna() {
        return antenna;
    }

    public void setAntenna(String antenna) {
        this.antenna = antenna;
    }

    public String getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(String numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public String getMaxAntennaResistancetime() {
        return maxAntennaResistancetime;
    }

    public void setMaxAntennaResistancetime(String maxAntennaResistancetime) {
        this.maxAntennaResistancetime = maxAntennaResistancetime;
    }

    public String getWaitingTimeAntSwitch() {
        return waitingTimeAntSwitch;
    }

    public void setWaitingTimeAntSwitch(String waitingTimeAntSwitch) {
        this.waitingTimeAntSwitch = waitingTimeAntSwitch;
    }

    public String getAntennaSwitchingSequence() {
        return antennaSwitchingSequence;
    }

    public void setAntennaSwitchingSequence(String antennaSwitchingSequence) {
        this.antennaSwitchingSequence = antennaSwitchingSequence;
    }

    public String getAntennaProtectionThreshold() {
        return antennaProtectionThreshold;
    }

    public void setAntennaProtectionThreshold(String antennaProtectionThreshold) {
        this.antennaProtectionThreshold = antennaProtectionThreshold;
    }

    public String getLBTMode() {
        return LBTMode;
    }

    public void setLBTMode(String LBTMode) {
        this.LBTMode = LBTMode;
    }

    public String getRSSIMaxVal() {
        return RSSIMaxVal;
    }

    public void setRSSIMaxVal(String RSSIMaxVal) {
        this.RSSIMaxVal = RSSIMaxVal;
    }
}