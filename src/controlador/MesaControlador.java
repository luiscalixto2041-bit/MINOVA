/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import modelo.Mesa;
import modelo.Usuario;
import repositorio.MesaRepositorio;
import seguridad.Permisos;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MesaControlador {

    private final MesaRepositorio repositorio;
    private final Usuario usuarioActual;

    // CONSTRUCTOR

    public MesaControlador(Usuario usuarioActual) {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "MESAS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para acceder a mesas."
            );
        }

        this.usuarioActual = usuarioActual;
        this.repositorio = new MesaRepositorio();
    }

    // VALIDAR ACCESO AL MODULO

    private void validarAcceso() {

        if (!usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "MESAS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para consultar mesas."
            );
        }
    }

    // COMPROBAR SI ES ADMINISTRADOR

    public boolean puedeAdministrar() {

        return usuarioActual.isEstado()
                && "ADMINISTRADOR".equalsIgnoreCase(
                        usuarioActual.getRol()
                );
    }

    // VALIDAR PERMISOS ADMINISTRATIVOS

    private void validarAdministrador() {

        validarAcceso();

        if (!puedeAdministrar()) {

            throw new IllegalArgumentException(
                    "Solo el administrador puede "
                    + "modificar la configuracion de mesas."
            );
        }
    }

    // VALIDAR DATOS

    private void validarDatos(
            int numeroMesa,
            int capacidad) {

        if (numeroMesa <= 0) {

            throw new IllegalArgumentException(
                    "El numero de mesa debe ser mayor que cero."
            );
        }

        if (capacidad <= 0) {

            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que cero."
            );
        }
    }

    // REGISTRAR NUEVA MESA

    public int registrarMesa(
            int numeroMesa,
            int capacidad) throws SQLException {

        validarAdministrador();

        validarDatos(numeroMesa, capacidad);

        Mesa existente = repositorio.buscarPorNumero(
                usuarioActual, numeroMesa
        );

        if (existente != null) {

            throw new IllegalArgumentException(
                    "Ya existe una mesa con ese numero."
            );
        }

        Mesa mesa = new Mesa(
                numeroMesa, capacidad
        );

        return repositorio.registrar(
                usuarioActual, mesa
        );
    }

    // BUSCAR MESA POR ID

    public Mesa buscarPorId(int idMesa)
            throws SQLException {

        validarAcceso();

        if (idMesa <= 0) {

            throw new IllegalArgumentException(
                    "El ID de la mesa no es valido."
            );
        }

        return repositorio.buscarPorId(
                usuarioActual, idMesa
        );
    }

    // BUSCAR MESA POR NUMERO

    public Mesa buscarPorNumero(int numeroMesa)
            throws SQLException {

        validarAcceso();

        if (numeroMesa <= 0) {

            throw new IllegalArgumentException(
                    "El numero de mesa no es valido."
            );
        }

        return repositorio.buscarPorNumero(
                usuarioActual, numeroMesa
        );
    }

    // ACTUALIZAR MESA

    public boolean actualizarMesa(
            int idMesa,
            int numeroMesa,
            int capacidad) throws SQLException {

        validarAdministrador();

        validarDatos(numeroMesa, capacidad);

        Mesa mesa = buscarPorId(idMesa);

        if (mesa == null) {

            throw new IllegalArgumentException(
                    "La mesa seleccionada no existe."
            );
        }

        Mesa existente = buscarPorNumero(
                numeroMesa
        );

        if (existente != null
                && existente.getIdMesa() != idMesa) {

            throw new IllegalArgumentException(
                    "Ese numero pertenece a otra mesa."
            );
        }

        mesa.setNumeroMesa(numeroMesa);
        mesa.setCapacidad(capacidad);

        return repositorio.actualizar(
                usuarioActual, mesa
        );
    }

    // ENVIAR MESA A MANTENIMIENTO

    public boolean activarMantenimiento(
            int idMesa) throws SQLException {

        validarAdministrador();

        if (idMesa <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona una mesa valida."
            );
        }

        return repositorio.cambiarMantenimiento(
                usuarioActual,
                idMesa,
                true
        );
    }

    // FINALIZAR MANTENIMIENTO

    public boolean desactivarMantenimiento(
            int idMesa) throws SQLException {

        validarAdministrador();

        if (idMesa <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona una mesa valida."
            );
        }

        return repositorio.cambiarMantenimiento(
                usuarioActual,
                idMesa,
                false
        );
    }

    // LISTAR TODAS LAS MESAS

    public ArrayList<Mesa> listarMesas()
            throws SQLException {

        validarAcceso();

        return repositorio.listar(usuarioActual);
    }

    // LISTAR MESAS POR ESTADO

    public ArrayList<Mesa> listarPorEstado(
            String estado) throws SQLException {

        validarAcceso();

        if (estado == null
                || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado)) {

            return listarMesas();
        }

        estado = estado.trim()
                .toUpperCase(Locale.ROOT);

        if (!List.of(
                "DISPONIBLE",
                "OCUPADA",
                "RESERVADA",
                "MANTENIMIENTO"
        ).contains(estado)) {

            throw new IllegalArgumentException(
                    "Estado de mesa invalido."
            );
        }

        return repositorio.listarPorEstado(
                usuarioActual, estado
        );
    }

    // LISTAR MESAS DISPONIBLES

    public ArrayList<Mesa> listarDisponibles()
            throws SQLException {

        return listarPorEstado("DISPONIBLE");
    }

    // LISTAR MESAS CON CAPACIDAD SUFICIENTE
    // No comprueba cruces de horarios.

    public ArrayList<Mesa> listarPorCapacidad(
            int cantidadPersonas) throws SQLException {

        validarAcceso();

        if (cantidadPersonas <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad de personas debe "
                    + "ser mayor que cero."
            );
        }

        ArrayList<Mesa> resultado =
                new ArrayList<>();

        for (Mesa mesa : listarMesas()) {

            if (!mesa.isMantenimiento()
                    && mesa.tieneCapacidad(
                            cantidadPersonas)) {

                resultado.add(mesa);
            }
        }

        return resultado;
    }

    // TOTAL DE MESAS

    public int totalMesas() throws SQLException {

        return listarMesas().size();
    }

    // TOTAL DE MESAS DISPONIBLES

    public int mesasDisponibles()
            throws SQLException {

        return listarPorEstado(
                "DISPONIBLE"
        ).size();
    }

    // TOTAL DE MESAS OCUPADAS

    public int mesasOcupadas()
            throws SQLException {

        return listarPorEstado(
                "OCUPADA"
        ).size();
    }

    // TOTAL DE MESAS RESERVADAS

    public int mesasReservadas()
            throws SQLException {

        return listarPorEstado(
                "RESERVADA"
        ).size();
    }

    // TOTAL DE MESAS EN MANTENIMIENTO

    public int mesasEnMantenimiento()
            throws SQLException {

        return listarPorEstado(
                "MANTENIMIENTO"
        ).size();
    }

    // CAPACIDAD TOTAL DEL RESTAURANTE

    public long capacidadTotal()
            throws SQLException {

        validarAcceso();

        return repositorio.capacidadTotal(
                usuarioActual
        );
    }

    // CAPACIDAD ACTUALMENTE DISPONIBLE

    public long capacidadDisponible()
            throws SQLException {

        long capacidad = 0;

        for (Mesa mesa : listarDisponibles()) {

            capacidad += mesa.getCapacidad();
        }

        return capacidad;
    }

    // PORCENTAJE DE MESAS OCUPADAS

    public double porcentajeOcupacion()
            throws SQLException {

        ArrayList<Mesa> mesas = listarMesas();

        int operativas = 0;
        int ocupadas = 0;

        for (Mesa mesa : mesas) {

            if (!mesa.isMantenimiento()) {

                operativas++;

                if (mesa.isOcupada()) {
                    ocupadas++;
                }
            }
        }

        if (operativas == 0) {
            return 0;
        }

        return ocupadas * 100.0 / operativas;
    }
}

