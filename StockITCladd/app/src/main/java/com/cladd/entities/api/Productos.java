package com.cladd.entities.api;

public class Productos {
    private int productoID;
    private String productoCod;
    private String descrip;
    private String fecha;

    public int getProductoID() {
        return productoID;
    }

    public void setProductoID(int productoID) {
        this.productoID = productoID;
    }

    public String getProductoCod() {
        return productoCod;
    }

    public void setProductoCod(String productoCod) {
        this.productoCod = productoCod;
    }

    public String getDescrip() {
        return descrip;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
