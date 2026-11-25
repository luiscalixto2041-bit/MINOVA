
package modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reserva {

    private int idReserva;
    private int idMesa;
    private int idCliente;
    private int idUsuario;

    private int numeroMesa;
    private int cantidadPersonas;

    private String documentoCliente;
    private String nombreCliente;
    private String nombreUsuario;

    private LocalDate fechaReserva;
    private LocalTime horaReserva;

    private String estado = "PENDIENTE";
    private String observaciones;

    // CONSTRUCTOR VACIO

    public Reserva() {
    }

    // CONSTRUCTOR PARA REGISTRAR RESERVAS

    public Reserva(
            int idMesa,
            int idCliente,
            int idUsuario,
            LocalDate fechaReserva,
            LocalTime horaReserva,
            int cantidadPersonas,
            String observaciones) {

        this.idMesa = idMesa;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.fechaReserva = fechaReserva;
        this.horaReserva = horaReserva;
        this.cantidadPersonas = cantidadPersonas;
        this.observaciones = observaciones;
        this.estado = "PENDIENTE";
    }

    // CONSTRUCTOR COMPLETO PARA MYSQL

    public Reserva(
            int idReserva,
            int idMesa,
            int idCliente,
            int idUsuario,
            int numeroMesa,
            String documentoCliente,
            String nombreCliente,
            String nombreUsuario,
            LocalDate fechaReserva,
            LocalTime horaReserva,
            int cantidadPersonas,
            String estado,
            String observaciones) {

        this.idReserva = idReserva;
        this.idMesa = idMesa;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.numeroMesa = numeroMesa;
        this.documentoCliente = documentoCliente;
        this.nombreCliente = nombreCliente;
        this.nombreUsuario = nombreUsuario;
        this.fechaReserva = fechaReserva;
        this.horaReserva = horaReserva;
        this.cantidadPersonas = cantidadPersonas;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // GETTERS Y SETTERS

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public int getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(int cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public void setDocumentoCliente(String documentoCliente) {
        this.documentoCliente = documentoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDate fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public LocalTime getHoraReserva() {
        return horaReserva;
    }

    public void setHoraReserva(LocalTime horaReserva) {
        this.horaReserva = horaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // COMPROBAR ESTADO DE LA RESERVA

    public boolean isPendiente() {
        return "PENDIENTE".equals(estado);
    }

    public boolean isConfirmada() {
        return "CONFIRMADA".equals(estado);
    }

    public boolean isCancelada() {
        return "CANCELADA".equals(estado);
    }

    public boolean isFinalizada() {
        return "FINALIZADA".equals(estado);
    }

    public boolean isActiva() {
        return isPendiente() || isConfirmada();
    }

    @Override
    public String toString() {

        return "Reserva #" + idReserva
                + " - Mesa " + numeroMesa;
    }
}
