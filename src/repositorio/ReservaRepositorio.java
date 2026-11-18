/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import java.util.ArrayList;
import modelo.Reserva;

public class ReservaRepositorio {

   private static final ArrayList<Reserva> reservas = new ArrayList<>();

    public boolean registrar(Reserva reserva) {

        if (reserva == null) {
            return false;
        }

        if (reserva.getNumeroMesa() <= 0 ||
            reserva.getFechaReserva() == null ||
            reserva.getHoraReserva() == null ||
            reserva.getDocumentoCliente() == null ||
            reserva.getDocumentoCliente().isBlank()) {

            return false;
        }

        for (Reserva existente : reservas) {

            if (existente.getNumeroMesa() == reserva.getNumeroMesa()
                    && existente.getFechaReserva().equals(reserva.getFechaReserva())
                    && existente.getHoraReserva().equals(reserva.getHoraReserva())) {

                return false;
            }
        }

        reservas.add(reserva);
        return true;
    }

    public ArrayList<Reserva> listar() {
        return new ArrayList<>(reservas);
    }
}

