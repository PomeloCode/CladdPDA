package com.cladd.entities.model;

import java.util.ArrayList;
import java.util.List;

public class Inventario
{
    public String TituloInventario ;
    public String Operario ;
    public String FechaCierre ;
    public String FechaInicio ;

    public List<InventarioDetalle> Piezas;

    public Inventario() {
        Piezas = new ArrayList<>();
    }

    public String getTituloInventario() {
        return TituloInventario;
    }

    public void setTituloInventario(String tituloInventario) {
        TituloInventario = tituloInventario;
    }

    public String getOperario() {
        return Operario;
    }

    public void setOperario(String operario) {
        Operario = operario;
    }

    public String getFechaCierre() {
        return FechaCierre;
    }

    public void setFechaCierre(String fechaCierre) {
        FechaCierre = fechaCierre;
    }

    public String getFechaInicio() {
        return FechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        FechaInicio = fechaInicio;
    }

    public List<InventarioDetalle> getPiezas() {
        return Piezas;
    }

    public void setPiezas(List<InventarioDetalle> piezas) {
        Piezas = piezas;
    }
}