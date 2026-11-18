/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import java.time.LocalDate;
import java.time.LocalTime;
import modelo.Reserva;
import repositorio.ReservaRepositorio;

public class ReservaControlador {

    private final ReservaRepositorio repositorio =
            new ReservaRepositorio();

    public boolean registrarReserva(
            int numeroMesa,
            String fecha,
            String hora,
            String documentoCliente) {

        if (numeroMesa <= 0 ||
            documentoCliente == null ||
            documentoCliente.isBlank()) {

            return false;
        }

        try {

            LocalDate fechaReserva = LocalDate.parse(fecha.trim());
            LocalTime horaReserva = LocalTime.parse(hora.trim());

            Reserva reserva = new Reserva(
                    numeroMesa,
                    fechaReserva,
                    horaReserva,
                    documentoCliente.trim()
            );

            return repositorio.registrar(reserva);

        } catch (java.time.format.DateTimeParseException |
                 NullPointerException e) {

            return false;
        }
    }

    public java.util.ArrayList<Reserva> listarReservas() {
        return repositorio.listar();
    }
}

