
package vista;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.LineBorder;

import controlador.MesaControlador;

import modelo.Mesa;
import modelo.Usuario;

import seguridad.Permisos;

public class MesasPanel extends JPanel
        implements Refrescable {

    private final MesaControlador controlador;

    private final MainFrame marco;
    private final ReservasPanel reservasPanel;
    private final Usuario usuarioActual;

    private JPanel cuadricula;

    private JLabel lblTotal;
    private JLabel lblDisponibles;
    private JLabel lblOcupadas;
    private JLabel lblReservadas;
    private JLabel lblMantenimiento;
    private JLabel lblSeleccion;

    private JButton btnReservar;
    private JButton btnEditar;
    private JButton btnMantenimiento;

    private int idMesaSeleccionada = 0;

    private ArrayList<Mesa> mesas =
            new ArrayList<>();

    // CONSTRUCTOR

    public MesasPanel(
            MainFrame marco,
            ReservasPanel reservasPanel) {

        this.marco = marco;
        this.reservasPanel = reservasPanel;

        this.usuarioActual =
                marco.getUsuarioActual();

        this.controlador =
                new MesaControlador(usuarioActual);

        setLayout(new BorderLayout());

        JPanel contenido =
                new JPanel(new BorderLayout(15, 15));

        contenido.add(
                crearContadores(),
                BorderLayout.NORTH
        );

        contenido.add(
                crearCuadricula(),
                BorderLayout.CENTER
        );

        contenido.add(
                crearAcciones(),
                BorderLayout.SOUTH
        );

        add(
                Tema.marcoModulo(
                        "GESTION DE MESAS",
                        contenido
                ),
                BorderLayout.CENTER
        );

        refrescar();
    }

    // CONTADORES

    private JPanel crearContadores() {

        lblTotal = new JLabel("0");
        lblDisponibles = new JLabel("0");
        lblOcupadas = new JLabel("0");
        lblReservadas = new JLabel("0");
        lblMantenimiento = new JLabel("0");

        JPanel contadores =
                new JPanel(new GridLayout(1, 5, 10, 0));

        contadores.setOpaque(false);

        contadores.add(
                Tema.tarjeta(
                        "Total mesas",
                        lblTotal,
                        Tema.OSCURO
                )
        );

        contadores.add(
                Tema.tarjeta(
                        "Disponibles",
                        lblDisponibles,
                        Tema.VERDE
                )
        );

        contadores.add(
                Tema.tarjeta(
                        "Ocupadas",
                        lblOcupadas,
                        Tema.ROJO
                )
        );

        contadores.add(
                Tema.tarjeta(
                        "Reservadas",
                        lblReservadas,
                        Tema.AMBAR
                )
        );

        contadores.add(
                Tema.tarjeta(
                        "Mantenimiento",
                        lblMantenimiento,
                        Tema.GRIS
                )
        );

        return contadores;
    }

    // CUADRICULA DINAMICA

    private JScrollPane crearCuadricula() {

        cuadricula =
                new JPanel(new GridLayout(0, 3, 15, 15));

        cuadricula.setOpaque(false);

        cuadricula.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10
                )
        );

        JPanel contenedor =
                new JPanel(new BorderLayout());

        contenedor.setOpaque(false);

        contenedor.add(
                cuadricula,
                BorderLayout.NORTH
        );

        JScrollPane scroll =
                new JScrollPane(contenedor);

        scroll.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scroll.setOpaque(false);

        scroll.getViewport().setOpaque(false);

        scroll.getVerticalScrollBar()
                .setUnitIncrement(16);

        return scroll;
    }

    // BOTONES Y ACCIONES

    private JPanel crearAcciones() {

        JPanel acciones =
                new JPanel(new BorderLayout(10, 10));

        acciones.setOpaque(false);

        lblSeleccion = Tema.seccion(
                "SELECCIONA UNA MESA"
        );

        acciones.add(
                lblSeleccion,
                BorderLayout.NORTH
        );

        JPanel botones =
                new JPanel(new FlowLayout(
                        FlowLayout.LEFT, 8, 5
                ));

        botones.setOpaque(false);

        // RESERVAR MESA

        btnReservar = Tema.boton(
                "+ RESERVAR", Tema.AMBAR
        );

        btnReservar.addActionListener(
                e -> irAReservas()
        );

        if (reservasPanel != null
                && Permisos.puedeEditar(
                        usuarioActual, "RESERVAS")) {

            botones.add(btnReservar);
        }

        // REGISTRAR NUEVA MESA

        JButton btnRegistrar = Tema.boton(
                "+ NUEVA MESA", Tema.VERDE
        );

        btnRegistrar.addActionListener(
                e -> registrarMesa()
        );

        // EDITAR MESA

        btnEditar = Tema.boton(
                "EDITAR", Tema.MOSTAZA
        );

        btnEditar.addActionListener(
                e -> editarMesa()
        );

        // MANTENIMIENTO

        btnMantenimiento = Tema.boton(
                "MANTENIMIENTO", Tema.GRIS
        );

        btnMantenimiento.addActionListener(
                e -> cambiarMantenimiento()
        );

        // SOLO ADMINISTRADORES

        if (controlador.puedeAdministrar()) {

            botones.add(btnRegistrar);
            botones.add(btnEditar);
            botones.add(btnMantenimiento);
        }

        // RECARGAR

        JButton btnRecargar = Tema.boton(
                "RECARGAR", Tema.GRIS
        );

        btnRecargar.addActionListener(
                e -> refrescar()
        );

        botones.add(btnRecargar);

        acciones.add(
                botones,
                BorderLayout.CENTER
        );

        return acciones;
    }

    // COLOR SEGUN ESTADO

    private Color obtenerColor(Mesa mesa) {

        return switch (mesa.getEstado()) {

            case "DISPONIBLE" -> Tema.VERDE;

            case "OCUPADA" -> Tema.ROJO;

            case "RESERVADA" -> Tema.AMBAR;

            case "MANTENIMIENTO" -> Tema.GRIS;

            default -> Tema.GRIS;
        };
    }

    // COLOR DE FONDO SEGUN ESTADO

    private Color obtenerFondo(Mesa mesa) {

        return switch (mesa.getEstado()) {

            case "DISPONIBLE" ->
                new Color(0xE8F5E9);

            case "OCUPADA" ->
                new Color(0xFDE8E8);

            case "RESERVADA" ->
                new Color(0xFBEFD5);

            case "MANTENIMIENTO" ->
                new Color(0xE5E7EB);

            default -> Color.WHITE;
        };
    }

    // CREAR TARJETA DE UNA MESA

    private JButton crearTarjeta(Mesa mesa) {

        String texto =
                "<html><center>"
                + "<br><b>MESA "
                + mesa.getNumeroMesa()
                + "</b><br><br>"
                + mesa.getCapacidad()
                + " personas<br><br>"
                + mesa.getEstado()
                + "<br><br></center></html>";

        JButton boton = new JButton(texto);

        boton.setFont(Tema.NEGRITA);

        boton.setPreferredSize(
                new Dimension(180, 145)
        );

        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);

        boton.setForeground(Tema.OSCURO);

        boton.setBackground(
                obtenerFondo(mesa)
        );

        boton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        if (mesa.getIdMesa()
                == idMesaSeleccionada) {

            boton.setBorder(
                    new LineBorder(
                            Tema.OSCURO, 4, true
                    )
            );

        } else {

            boton.setBorder(
                    new LineBorder(
                            obtenerColor(mesa),
                            2,
                            true
                    )
            );
        }

        boton.addActionListener(e -> {

            idMesaSeleccionada =
                    mesa.getIdMesa();

            dibujarMesas();
            actualizarSeleccion();
        });

        return boton;
    }

    // DIBUJAR MESAS

    private void dibujarMesas() {

        cuadricula.removeAll();

        for (Mesa mesa : mesas) {

            cuadricula.add(
                    crearTarjeta(mesa)
            );
        }

        cuadricula.revalidate();
        cuadricula.repaint();
    }

    // OBTENER MESA SELECCIONADA

    private Mesa obtenerSeleccionada() {

        for (Mesa mesa : mesas) {

            if (mesa.getIdMesa()
                    == idMesaSeleccionada) {

                return mesa;
            }
        }

        return null;
    }

    // ACTUALIZAR SELECCION

    private void actualizarSeleccion() {

        Mesa mesa = obtenerSeleccionada();

        if (mesa == null) {

            idMesaSeleccionada = 0;

            lblSeleccion.setText(
                    "SELECCIONA UNA MESA"
            );

            btnReservar.setEnabled(false);
            btnEditar.setEnabled(false);
            btnMantenimiento.setEnabled(false);

            btnMantenimiento.setText(
                    "MANTENIMIENTO"
            );

            return;
        }

        lblSeleccion.setText(
                "MESA " + mesa.getNumeroMesa()
                + " | CAPACIDAD: "
                + mesa.getCapacidad()
                + " PERSONAS | "
                + mesa.getEstado()
        );

        btnReservar.setEnabled(
                reservasPanel != null
                && !mesa.isMantenimiento()
                && Permisos.puedeEditar(
                        usuarioActual, "RESERVAS")
        );

        btnEditar.setEnabled(
                controlador.puedeAdministrar()
                && !mesa.isOcupada()
                && !mesa.isReservada()
        );

        btnMantenimiento.setEnabled(
                controlador.puedeAdministrar()
                && (mesa.isDisponible()
                    || mesa.isMantenimiento())
        );

        if (mesa.isMantenimiento()) {

            btnMantenimiento.setText(
                    "FINALIZAR MANTENIMIENTO"
            );

        } else {

            btnMantenimiento.setText(
                    "MANTENIMIENTO"
            );
        }
    }

    // REGISTRAR NUEVA MESA

    private void registrarMesa() {

        JTextField txtNumero =
                new JTextField();

        JTextField txtCapacidad =
                new JTextField("4");

        JPanel formulario = Tema.formulario(
                new String[]{
                    "Numero de mesa:",
                    "Capacidad:"
                },
                new JComponent[]{
                    txtNumero,
                    txtCapacidad
                }
        );

        int opcion = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Registrar nueva mesa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            int numero = Integer.parseInt(
                    txtNumero.getText().trim()
            );

            int capacidad = Integer.parseInt(
                    txtCapacidad.getText().trim()
            );

            int idMesa =
                    controlador.registrarMesa(
                            numero, capacidad
                    );

            idMesaSeleccionada = idMesa;

            Tema.info(
                    this,
                    "Mesa registrada correctamente."
            );

            refrescar();

        } catch (NumberFormatException e) {

            Tema.error(
                    this,
                    "Ingresa valores numericos validos."
            );

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());

        } catch (SQLException e) {

            mostrarErrorSQL(e);
        }
    }

    // EDITAR MESA

    private void editarMesa() {

        Mesa mesa = obtenerSeleccionada();

        if (mesa == null) {

            Tema.error(
                    this,
                    "Selecciona una mesa."
            );

            return;
        }

        JTextField txtNumero =
                new JTextField(
                    String.valueOf(
                        mesa.getNumeroMesa()
                    )
                );

        JTextField txtCapacidad =
                new JTextField(
                    String.valueOf(
                        mesa.getCapacidad()
                    )
                );

        JPanel formulario = Tema.formulario(
                new String[]{
                    "Numero de mesa:",
                    "Capacidad:"
                },
                new JComponent[]{
                    txtNumero,
                    txtCapacidad
                }
        );

        int opcion = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Editar mesa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            int numero = Integer.parseInt(
                    txtNumero.getText().trim()
            );

            int capacidad = Integer.parseInt(
                    txtCapacidad.getText().trim()
            );

            boolean actualizado =
                    controlador.actualizarMesa(
                            mesa.getIdMesa(),
                            numero,
                            capacidad
                    );

            if (actualizado) {

                Tema.info(
                        this,
                        "Mesa actualizada correctamente."
                );

                refrescar();
            }

        } catch (NumberFormatException e) {

            Tema.error(
                    this,
                    "Ingresa valores numericos validos."
            );

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());

        } catch (SQLException e) {

            mostrarErrorSQL(e);
        }
    }

    // ACTIVAR O FINALIZAR MANTENIMIENTO

    private void cambiarMantenimiento() {

        Mesa mesa = obtenerSeleccionada();

        if (mesa == null) {

            Tema.error(
                    this,
                    "Selecciona una mesa."
            );

            return;
        }

        boolean activar =
                !mesa.isMantenimiento();

        String mensaje = activar
                ? "Enviar la mesa a mantenimiento?"
                : "Finalizar el mantenimiento de la mesa?";

        int opcion = JOptionPane.showConfirmDialog(
                this,
                mensaje,
                "Mantenimiento",
                JOptionPane.YES_NO_OPTION
        );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean resultado;

            if (activar) {

                resultado =
                        controlador.activarMantenimiento(
                                mesa.getIdMesa()
                        );

            } else {

                resultado =
                        controlador.desactivarMantenimiento(
                                mesa.getIdMesa()
                        );
            }

            if (resultado) {

                Tema.info(
                        this,
                        "Estado actualizado correctamente."
                );

                refrescar();
            }

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());

        } catch (SQLException e) {

            mostrarErrorSQL(e);
        }
    }

    // IR AL MODULO DE RESERVAS

    private void irAReservas() {

        Mesa mesa = obtenerSeleccionada();

        if (mesa == null) {

            Tema.error(
                    this,
                    "Selecciona una mesa."
            );

            return;
        }

        if (mesa.isMantenimiento()) {

            Tema.error(
                    this,
                    "La mesa esta en mantenimiento."
            );

            return;
        }

        if (reservasPanel == null
                || !Permisos.puedeEditar(
                        usuarioActual, "RESERVAS")) {

            Tema.error(
                    this,
                    "No tienes permisos para registrar reservas."
            );

            return;
        }

        // PRIMERO NAVEGAR Y DESPUES SELECCIONAR

        marco.ir("RESERVAS");

        reservasPanel.seleccionarMesa(
                mesa.getNumeroMesa()
        );
    }

    // ACTUALIZAR CONTADORES

    private void actualizarContadores() {

        int disponibles = 0;
        int ocupadas = 0;
        int reservadas = 0;
        int mantenimiento = 0;

        for (Mesa mesa : mesas) {

            switch (mesa.getEstado()) {

                case "DISPONIBLE" ->
                    disponibles++;

                case "OCUPADA" ->
                    ocupadas++;

                case "RESERVADA" ->
                    reservadas++;

                case "MANTENIMIENTO" ->
                    mantenimiento++;
            }
        }

        lblTotal.setText(
                String.valueOf(mesas.size())
        );

        lblDisponibles.setText(
                String.valueOf(disponibles)
        );

        lblOcupadas.setText(
                String.valueOf(ocupadas)
        );

        lblReservadas.setText(
                String.valueOf(reservadas)
        );

        lblMantenimiento.setText(
                String.valueOf(mantenimiento)
        );
    }

    // MOSTRAR ERROR MYSQL

    private void mostrarErrorSQL(SQLException e) {

        System.err.println(e.getMessage());

        if (e.getErrorCode() == 1062) {

            Tema.error(
                    this,
                    "Ya existe una mesa con ese numero."
            );

        } else {

            Tema.error(
                    this,
                    "No se pudo completar la operacion en MySQL."
            );
        }
    }

    // REFRESCAR MODULO

    @Override
    public void refrescar() {

        try {

            mesas = controlador.listarMesas();

            dibujarMesas();
            actualizarContadores();
            actualizarSeleccion();

        } catch (SQLException
                | IllegalArgumentException e) {

            mesas.clear();

            idMesaSeleccionada = 0;

            dibujarMesas();
            actualizarSeleccion();

            lblTotal.setText("--");
            lblDisponibles.setText("--");
            lblOcupadas.setText("--");
            lblReservadas.setText("--");
            lblMantenimiento.setText("--");

            if (e instanceof SQLException sql) {

                mostrarErrorSQL(sql);

            } else {

                Tema.error(this, e.getMessage());
            }
        }
    }
}
