
package pi_e1_g327._minova;
import java.time.LocalDate;
import java.time.LocalTime;
public class Reserva {
    private int numeroMesa;          //[cite: 1]
    private LocalDate fechaReserva;  //[cite: 1]
    private LocalTime horaReserva;   //[cite: 1]
    private String documentoCliente; //[cite: 1]

    public Reserva() {}

    public Reserva(int numeroMesa, LocalDate fechaReserva, LocalTime horaReserva, String documentoCliente) {
        this.numeroMesa = numeroMesa;
        this.fechaReserva = fechaReserva;
        this.horaReserva = horaReserva;
        this.documentoCliente = documentoCliente;
    }

    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }

    public LocalDate getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDate fechaReserva) { this.fechaReserva = fechaReserva; }

    public LocalTime getHoraReserva() { return horaReserva; }
    public void setHoraReserva(LocalTime horaReserva) { this.horaReserva = horaReserva; }

    public String getDocumentoCliente() { return documentoCliente; }
    public void setDocumentoCliente(String documentoCliente) { this.documentoCliente = documentoCliente; }
}
