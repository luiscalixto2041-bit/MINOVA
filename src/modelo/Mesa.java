/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package modelo;

public class Mesa {

    private int idMesa;
    private int numeroMesa;
    private int capacidad;
    private String estado = "DISPONIBLE";

    // CONSTRUCTOR VACIO

    public Mesa() {
    }

    // CONSTRUCTOR PARA REGISTRAR MESAS

    public Mesa(int numeroMesa, int capacidad) {

        this.numeroMesa = numeroMesa;
        this.capacidad = capacidad;
        this.estado = "DISPONIBLE";
    }

    // CONSTRUCTOR COMPLETO PARA MYSQL

    public Mesa(
            int idMesa,
            int numeroMesa,
            int capacidad,
            String estado) {

        this.idMesa = idMesa;
        this.numeroMesa = numeroMesa;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    // GETTERS Y SETTERS

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // CONSULTAR DISPONIBILIDAD

    public boolean isDisponible() {
        return "DISPONIBLE".equals(estado);
    }

    public boolean isOcupada() {
        return "OCUPADA".equals(estado);
    }

    public boolean isReservada() {
        return "RESERVADA".equals(estado);
    }

    public boolean isMantenimiento() {
        return "MANTENIMIENTO".equals(estado);
    }

    // VALIDAR CAPACIDAD

    public boolean tieneCapacidad(int personas) {

        return personas > 0
                && personas <= capacidad;
    }

    // REPRESENTACION EN LA INTERFAZ

    @Override
    public String toString() {

        return "Mesa " + numeroMesa
                + " - " + capacidad
                + " personas";
    }
}

