
package controlador;

import modelo.Reserva;
import modelo.Mesa;
import modelo.Usuario;

import repositorio.ReservaRepositorio;
import repositorio.MesaRepositorio;

import seguridad.Permisos;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;

public class ReservaControlador {

    private final ReservaRepositorio repositorio;
    private final MesaRepositorio mesaRepositorio;
    private final Usuario usuarioActual;

    private static final ZoneId ZONA =
            ZoneId.of("America/Bogota");

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm")
                    .withResolverStyle(ResolverStyle.STRICT);

    // CONSTRUCTOR PRINCIPAL

    public ReservaControlador(Usuario usuarioActual) {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "RESERVAS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para acceder a reservas."
            );
        }

        this.usuarioActual = usuarioActual;
        this.repositorio = new ReservaRepositorio();
        this.mesaRepositorio = new MesaRepositorio();
    }

    // VALIDAR ACCESO

    private void validarAcceso() {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "RESERVAS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para consultar reservas."
            );
        }
    }

    // VALIDAR EDICION

    private void validarEdicion() {

        validarAcceso();

        if (!Permisos.puedeEditar(
                usuarioActual, "RESERVAS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para gestionar reservas."
            );
        }
    }

    // CONVERTIR FECHA

    public LocalDate convertirFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {

            throw new IllegalArgumentException(
                    "Ingresa la fecha de la reserva."
            );
        }

        try {

            return LocalDate.parse(
                    fecha.trim(), FORMATO_FECHA
            );

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Fecha invalida. Usa DD/MM/AAAA."
            );
        }
    }

    // CONVERTIR HORA

    public LocalTime convertirHora(String hora) {

        if (hora == null || hora.isBlank()) {

            throw new IllegalArgumentException(
                    "Ingresa la hora de la reserva."
            );
        }

        try {

            return LocalTime.parse(
                    hora.trim(), FORMATO_HORA
            );

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Hora invalida. Usa HH:MM en formato de 24 horas."
            );
        }
    }

    // VALIDAR IDENTIFICADOR DE RESERVA

    private void validarIdReserva(int idReserva) {

        if (idReserva <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona una reserva valida."
            );
        }
    }

    // REGISTRAR NUEVA RESERVA

    public int registrarReserva(
            int idMesa,
            int idCliente,
            String fecha,
            String hora,
            int cantidadPersonas,
            String observaciones)
            throws SQLException {

        validarEdicion();

        if (idMesa <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona una mesa."
            );
        }

        if (idCliente <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona un cliente registrado."
            );
        }

        if (cantidadPersonas <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad de personas debe ser mayor que cero."
            );
        }

        LocalDate fechaReserva = convertirFecha(fecha);

        LocalTime horaReserva = convertirHora(hora);

        LocalDateTime inicio = LocalDateTime.of(
                fechaReserva,
                horaReserva
        );

        if (inicio.isBefore(
                LocalDateTime.now(ZONA))) {

            throw new IllegalArgumentException(
                    "No puedes registrar reservas en el pasado."
            );
        }

        if (observaciones != null) {

            observaciones = observaciones.trim();

            if (observaciones.length() > 255) {

                throw new IllegalArgumentException(
                        "Las observaciones no pueden superar "
                        + "los 255 caracteres."
                );
            }

            if (observaciones.isEmpty()) {

                observaciones = null;
            }
        }

        Reserva reserva = new Reserva(
                idMesa,
                idCliente,
                usuarioActual.getIdUsuario(),
                fechaReserva,
                horaReserva,
                cantidadPersonas,
                observaciones
        );

        return repositorio.registrar(
                usuarioActual, reserva
        );
    }

    // CONSULTAR TODAS LAS RESERVAS

    public ArrayList<Reserva> consultarReservas()
            throws SQLException {

        validarAcceso();

        return repositorio.listar(usuarioActual);
    }

    // BUSCAR RESERVA POR ID

    public Reserva buscarPorId(int idReserva)
            throws SQLException {

        validarAcceso();

        validarIdReserva(idReserva);

        return repositorio.buscarPorId(
                usuarioActual, idReserva
        );
    }

    // FILTRAR RESERVAS POR ESTADO

    public ArrayList<Reserva> listarPorEstado(
            String estado) throws SQLException {

        validarAcceso();

        if (estado == null
                || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado.trim())) {

            return consultarReservas();
        }

        estado = estado.trim()
                .toUpperCase(Locale.ROOT);

        if (!List.of(
                "PENDIENTE",
                "CONFIRMADA",
                "CANCELADA",
                "FINALIZADA"
        ).contains(estado)) {

            throw new IllegalArgumentException(
                    "Estado de reserva invalido."
            );
        }

        return repositorio.listarPorEstado(
                usuarioActual, estado
        );
    }

    // FILTRAR RESERVAS POR FECHA

    public ArrayList<Reserva> listarPorFecha(
            LocalDate fecha) throws SQLException {

        validarAcceso();

        if (fecha == null) {

            throw new IllegalArgumentException(
                    "Selecciona una fecha valida."
            );
        }

        ArrayList<Reserva> resultado =
                new ArrayList<>();

        for (Reserva reserva : consultarReservas()) {

            if (fecha.equals(
                    reserva.getFechaReserva())) {

                resultado.add(reserva);
            }
        }

        return resultado;
    }

    // LISTAR RESERVAS DE HOY

    public ArrayList<Reserva> reservasDeHoy()
            throws SQLException {

        return listarPorFecha(
                LocalDate.now(ZONA)
        );
    }

    // COMPROBAR SI UNA RESERVA ESTA ACTIVA

    private boolean esActiva(Reserva reserva) {

        return "PENDIENTE".equals(
                reserva.getEstado())
                || "CONFIRMADA".equals(
                        reserva.getEstado());
    }

    // LISTAR PROXIMAS RESERVAS

    public ArrayList<Reserva> proximasReservas()
            throws SQLException {

        validarAcceso();

        ArrayList<Reserva> resultado =
                new ArrayList<>();

        LocalDateTime ahora =
                LocalDateTime.now(ZONA);

        for (Reserva reserva : consultarReservas()) {

            LocalDateTime inicio = LocalDateTime.of(
                    reserva.getFechaReserva(),
                    reserva.getHoraReserva()
            );

            if (esActiva(reserva)
                    && !inicio.isBefore(ahora)) {

                resultado.add(reserva);
            }
        }

        resultado.sort(
                Comparator
                        .comparing(Reserva::getFechaReserva)
                        .thenComparing(Reserva::getHoraReserva)
        );

        return resultado;
    }

    // LISTAR MESAS REGISTRADAS

    public ArrayList<Mesa> listarMesas()
            throws SQLException {

        validarAcceso();

        return mesaRepositorio.listar(usuarioActual);
    }

    // CONSULTAR MESAS COMPATIBLES CON UNA RESERVA

    public ArrayList<Mesa> listarMesasDisponibles(
            String fecha,
            String hora,
            int cantidadPersonas)
            throws SQLException {

        validarAcceso();

        if (cantidadPersonas <= 0) {

            throw new IllegalArgumentException(
                    "Ingresa una cantidad de personas valida."
            );
        }

        LocalDateTime inicio = LocalDateTime.of(
                convertirFecha(fecha),
                convertirHora(hora)
        );

        LocalDateTime ahora =
                LocalDateTime.now(ZONA);

        if (inicio.isBefore(ahora)) {

            throw new IllegalArgumentException(
                    "El horario seleccionado ya paso."
            );
        }

        LocalDateTime fin = inicio.plusMinutes(
                ReservaRepositorio.DURACION_MINUTOS
        );

        ArrayList<Mesa> disponibles =
                new ArrayList<>();

        ArrayList<Reserva> reservas =
                consultarReservas();

        for (Mesa mesa : listarMesas()) {

            // EXCLUIR MESAS EN MANTENIMIENTO
            // O SIN CAPACIDAD SUFICIENTE

            if (mesa.isMantenimiento()
                    || !mesa.tieneCapacidad(
                            cantidadPersonas)) {

                continue;
            }

            // EVITAR RESERVAS INMEDIATAS
            // EN MESAS OCUPADAS O RESERVADAS

            if ((mesa.isOcupada()
                    || mesa.isReservada())
                    && inicio.isBefore(
                            ahora.plusMinutes(
                                ReservaRepositorio.DURACION_MINUTOS
                            ))) {

                continue;
            }

            boolean cruce = false;

            // COMPROBAR HORARIOS CRUZADOS

            for (Reserva reserva : reservas) {

                if (reserva.getIdMesa()
                        != mesa.getIdMesa()
                        || !esActiva(reserva)) {

                    continue;
                }

                LocalDateTime inicioExistente =
                        LocalDateTime.of(
                            reserva.getFechaReserva(),
                            reserva.getHoraReserva()
                        );

                LocalDateTime finExistente =
                        inicioExistente.plusMinutes(
                            ReservaRepositorio.DURACION_MINUTOS
                        );

                if (inicio.isBefore(finExistente)
                        && fin.isAfter(inicioExistente)) {

                    cruce = true;
                    break;
                }
            }

            if (!cruce) {

                disponibles.add(mesa);
            }
        }

        return disponibles;
    }

    // CONFIRMAR RESERVA

    public boolean confirmarReserva(int idReserva)
            throws SQLException {

        validarEdicion();

        validarIdReserva(idReserva);

        return repositorio.confirmar(
                usuarioActual, idReserva
        );
    }

    // CANCELAR RESERVA

    public boolean cancelarReserva(int idReserva)
            throws SQLException {

        validarEdicion();

        validarIdReserva(idReserva);

        return repositorio.cancelar(
                usuarioActual, idReserva
        );
    }

    // FINALIZAR RESERVA

    public boolean finalizarReserva(int idReserva)
            throws SQLException {

        validarEdicion();

        validarIdReserva(idReserva);

        return repositorio.finalizar(
                usuarioActual, idReserva
        );
    }

    // TOTAL DE RESERVAS

    public int totalReservas()
            throws SQLException {

        return consultarReservas().size();
    }

    // TOTAL DE RESERVAS PENDIENTES

    public int reservasPendientes()
            throws SQLException {

        return listarPorEstado(
                "PENDIENTE"
        ).size();
    }

    // TOTAL DE RESERVAS CONFIRMADAS

    public int reservasConfirmadas()
            throws SQLException {

        return listarPorEstado(
                "CONFIRMADA"
        ).size();
    }

    // TOTAL DE RESERVAS CANCELADAS

    public int reservasCanceladas()
            throws SQLException {

        return listarPorEstado(
                "CANCELADA"
        ).size();
    }

    // TOTAL DE RESERVAS FINALIZADAS

    public int reservasFinalizadas()
            throws SQLException {

        return listarPorEstado(
                "FINALIZADA"
        ).size();
    }

    // TOTAL DE RESERVAS ACTIVAS PARA HOY

    public int reservasActivasHoy()
            throws SQLException {

        int total = 0;

        for (Reserva reserva : reservasDeHoy()) {

            if (esActiva(reserva)) {

                total++;
            }
        }

        return total;
    }
}
