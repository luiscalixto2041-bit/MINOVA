package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

import controlador.ReservaControlador;

public class MesasPanel extends JPanel implements Refrescable {

    private final ReservaControlador controlador = new ReservaControlador();

    private final MainFrame marco;
    private final ReservasPanel reservasPanel;

    private JPanel cuadricula;
    private JLabel lblDisponibles;
    private JLabel lblReservadas;
    private JLabel lblSeleccion;

    private int mesaSeleccionada = 0;

    public MesasPanel(MainFrame marco, ReservasPanel reservasPanel) {

        this.marco = marco;
        this.reservasPanel = reservasPanel;

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(new BorderLayout(15, 15));

        // CONTADORES
        lblDisponibles = new JLabel("0");
        lblReservadas = new JLabel("0");

        JPanel contadores = new JPanel(new GridLayout(1, 2, 15, 0));
        contadores.setOpaque(false);
        contadores.add(Tema.tarjeta("Disponibles hoy", lblDisponibles, Tema.VERDE));
        contadores.add(Tema.tarjeta("Reservadas hoy", lblReservadas, Tema.AMBAR));

        contenido.add(contadores, BorderLayout.NORTH);

        // MESAS
        cuadricula = new JPanel(new GridLayout(2, 5, 12, 12));
        cuadricula.setOpaque(false);

        contenido.add(cuadricula, BorderLayout.CENTER);

        // SELECCION Y BOTON
        lblSeleccion = Tema.seccion("SELECCIONA UNA MESA");

        JButton btnReservar = Tema.boton("+ REGISTRAR RESERVA", Tema.AMBAR);
        btnReservar.addActionListener(e -> irAReservas());

        JPanel sur = new JPanel(new BorderLayout(0, 8));
        sur.setOpaque(false);
        sur.add(lblSeleccion, BorderLayout.NORTH);
        sur.add(Tema.filaBotones(btnReservar), BorderLayout.CENTER);

        contenido.add(sur, BorderLayout.SOUTH);

        add(Tema.marcoModulo("ESTADO DE LAS MESAS", contenido), BorderLayout.CENTER);

        refrescar();
    }

    // IR A RESERVAS CON LA MESA ELEGIDA
    private void irAReservas() {

        if (mesaSeleccionada == 0) {
            Tema.error(this, "Selecciona una mesa.");
            return;
        }

        reservasPanel.seleccionarMesa(mesaSeleccionada);
        marco.ir("RESERVAS");
    }

    @Override
    public void refrescar() {

        cuadricula.removeAll();

        int libres = controlador.mesasLibresHoy();

        for (int mesa = 1; mesa <= ReservaControlador.TOTAL_MESAS; mesa++) {

            final int numero = mesa;
            boolean reservada = controlador.mesaReservadaHoy(mesa);

            String estado = "Disponible";
            Color color = Tema.VERDE;

            if (reservada) {
                estado = "Reservada";
                color = Tema.AMBAR;
            }

            JButton boton = new JButton(
                    "<html><center>MESA " + mesa + "<br>" + estado + "</center></html>");

            boton.setFont(Tema.NEGRITA);
            boton.setOpaque(true);
            boton.setContentAreaFilled(true);
            boton.setFocusPainted(false);
            boton.setBackground(Color.WHITE);
            boton.setForeground(Tema.OSCURO);

            if (mesa == mesaSeleccionada) {
                boton.setBorder(new LineBorder(Tema.ROJO, 4));
            } else {
                boton.setBorder(new LineBorder(color, 2));
            }

            if (reservada) {
                boton.setBackground(new Color(0xFBEFD5));
            }

            boton.addActionListener(e -> {
                mesaSeleccionada = numero;
                lblSeleccion.setText("MESA SELECCIONADA: " + numero);
                refrescar();
            });

            cuadricula.add(boton);
        }

        lblDisponibles.setText(String.valueOf(libres));
        lblReservadas.setText(String.valueOf(ReservaControlador.TOTAL_MESAS - libres));

        cuadricula.revalidate();
        cuadricula.repaint();
    }
}
