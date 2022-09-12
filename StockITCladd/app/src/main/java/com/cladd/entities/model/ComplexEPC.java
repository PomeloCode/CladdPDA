package com.cladd.entities.model;


import com.pda.rfid.EPCModel;

import java.util.Date;

public class ComplexEPC {
    public Date readedDate;
    public EPCModel epcModel;

    public Date getReadedDate() {
        return readedDate;
    }

    public void setReadedDate(Date readedDate) {
        this.readedDate = readedDate;
    }

    public EPCModel getEpcModel() {
        return epcModel;
    }

    public void setEpcModel(EPCModel epcModel) {
        this.epcModel = epcModel;
    }
}
