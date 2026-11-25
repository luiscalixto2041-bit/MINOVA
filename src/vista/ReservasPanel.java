
package vista;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controlador.ClienteControlador;
import controlador.ReservaControlador;

import modelo.Cliente;
import modelo.Mesa;
import modelo.Reserva;
import modelo.Usuario;

public class ReservasPanel extends JPanel
        implements Refrescable {

    private final ReservaControlador controlador;

    private final ClienteControlador clienteControlador =
            new ClienteControlador();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    private JComboBox<Cliente> cmbCliente;
    private JComboBox<Mesa> cmbMesa;

    private JTextField txtFecha;
    private JTextField txtHora;
    private JTextField txtPersonas;
    private JTextField txtObservaciones;

    private JComboBox<String> cmbEstado;

    private JLabel lblRegistradas;
    private JLabel lblDisponibilidad;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private ArrayList<Reserva> reservas =
            new ArrayList<>();

    // CONSTRUCTOR PRINCIPAL

    public ReservasPanel(Usuario usuarioActual) {

        controlador =
                new ReservaControlador(usuarioActual);

        setLayout(new BorderLayout());

        JPanel contenido =
                new JPanel(new BorderLayout(15, 15));

        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab(
                "NUEVA RESERVA",
                crearFormulario()
        );

        pestanas.addTab(
                "RESERVAS REGISTRADAS",
                crearListado()
        );

        contenido.add(
                pestanas,
                BorderLayout.CENTER
        );

        add(
                Tema.marcoModulo(
                        "GESTION DE RESERVAS",
                        contenido
                ),
                BorderLayout.CENTER
        );

        refrescar();
    }

    // FORMULARIO

    private JPanel crearFormulario() {

        JPanel formulario =
                new JPanel(new BorderLayout(15, 15));

        formulario.setOpaque(false);

        formulario.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20
                )
        );

        formulario.add(
                Tema.seccion("REGISTRAR NUEVA RESERVA"),
                BorderLayout.NORTH
        );

        cmbCliente = new JComboBox<>();
        cmbMesa = new JComboBox<>();

        txtFecha = new JTextField(
                LocalDate.now(
                    ZoneId.of("America/Bogota")
                ).format(formatoFecha)
        );

        txtHora = new JTextField("19:00");
        txtPersonas = new JTextField("2");
        txtObservaciones = new JTextField();

        txtFecha.setToolTipText("DD/MM/AAAA");

        txtHora.setToolTipText(
                "Formato de 24 horas: HH:MM"
        );

        JPanel campos =
                new JPanel(new GridLayout(3, 2, 15, 15));

        campos.setOpaque(false);

        campos.add(
                Tema.campoConEtiqueta(
                        "Cliente:", cmbCliente
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Cantidad de personas:", txtPersonas
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Fecha (DD/MM/AAAA):", txtFecha
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Hora (HH:MM):", txtHora
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Mesa:", cmbMesa
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Observaciones:", txtObservaciones
                )
        );

        lblDisponibilidad = Tema.subtitulo(
                "Consulta la disponibilidad antes de reservar."
        );

        JButton btnBuscar = Tema.boton(
                "BUSCAR MESAS", Tema.MOSTAZA
        );

        JButton btnRegistrar = Tema.boton(
                "+ REGISTRAR RESERVA", Tema.AMBAR
        );

        JButton btnLimpiar = Tema.boton(
                "LIMPIAR", Tema.GRIS
        );

        btnBuscar.addActionListener(
                e -> buscarMesas()
        );

        btnRegistrar.addActionListener(
                e -> registrar()
        );

        btnLimpiar.addActionListener(
                e -> limpiarFormulario()
        );

        JPanel centro =
                new JPanel(new BorderLayout(10, 15));

        centro.setOpaque(false);

        centro.add(
                campos,
                BorderLayout.NORTH
        );

        centro.add(
                lblDisponibilidad,
                BorderLayout.CENTER
        );

        centro.add(
                Tema.filaBotones(
                        btnBuscar,
                        btnRegistrar,
                        btnLimpiar
                ),
                BorderLayout.SOUTH
        );

        formulario.add(
                centro,
                BorderLayout.CENTER
        );

        // INVALIDAR RESULTADOS SI CAMBIA EL HORARIO

        Tema.alCambiar(
                txtFecha,
                this::invalidarDisponibilidad
        );

        Tema.alCambiar(
                txtHora,
                this::invalidarDisponibilidad
        );

        Tema.alCambiar(
                txtPersonas,
                this::invalidarDisponibilidad
        );

        return formulario;
    }

    // INVALIDAR DISPONIBILIDAD

    private void invalidarDisponibilidad() {

        cmbMesa.removeAllItems();

        lblDisponibilidad.setText(
                "Consulta nuevamente las mesas disponibles."
        );
    }

    // BUSCAR MESAS DISPONIBLES

    private void buscarMesas() {

        cmbMesa.removeAllItems();

        try {

            int personas = Integer.parseInt(
                    txtPersonas.getText().trim()
            );

            ArrayList<Mesa> mesas =
                    controlador.listarMesasDisponibles(
                            txtFecha.getText(),
                            txtHora.getText(),
                            personas
                    );

            for (Mesa mesa : mesas) {

                cmbMesa.addItem(mesa);
            }

            lblDisponibilidad.setText(
                    mesas.size()
                    + " mesas disponibles para este horario."
            );

            if (mesas.isEmpty()) {

                Tema.error(
                        this,
                        "No hay mesas disponibles "
                        + "para ese horario y cantidad de personas."
                );
            }

        } catch (NumberFormatException e) {

            lblDisponibilidad.setText(
                    "Cantidad de personas invalida."
            );

            Tema.error(
                    this,
                    "Ingresa una cantidad de personas valida."
            );

        } catch (IllegalArgumentException e) {

            lblDisponibilidad.setText(
                    "No se pudo consultar la disponibilidad."
            );

            Tema.error(this, e.getMessage());

        } catch (SQLException e) {

            lblDisponibilidad.setText(
                    "Error al consultar las mesas."
            );

            mostrarErrorSQL(e);
        }
    }

    // REGISTRAR RESERVA

    private void registrar() {

        Cliente cliente =
                (Cliente) cmbCliente.getSelectedItem();

        Mesa mesa =
                (Mesa) cmbMesa.getSelectedItem();

        if (cliente == null) {

            Tema.error(
                    this,
                    "Primero debes seleccionar un cliente."
            );

            return;
        }

        if (mesa == null) {

            Tema.error(
                    this,
                    "Busca y selecciona una mesa disponible."
            );

            return;
        }

        try {

            int personas = Integer.parseInt(
                    txtPersonas.getText().trim()
            );

            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "Cliente: "
                    + cliente.getNombreCliente()
                    + "\nMesa: "
                    + mesa.getNumeroMesa()
                    + "\nFecha: "
                    + txtFecha.getText()
                    + "\nHora: "
                    + txtHora.getText()
                    + "\nPersonas: "
                    + personas
                    + "\nDuracion estimada: 2 horas"
                    + "\n\nConfirmar registro?",
                    "Registrar reserva",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }

            int idReserva =
                    controlador.registrarReserva(
                            mesa.getIdMesa(),
                            cliente.getIdCliente(),
                            txtFecha.getText(),
                            txtHora.getText(),
                            personas,
                            txtObservaciones.getText()
                    );

            Tema.info(
                    this,
                    "Reserva #" + idReserva
                    + " registrada correctamente."
            );

            limpiarFormulario();
            cargarReservas();

        } catch (NumberFormatException e) {

            Tema.error(
                    this,
                    "Cantidad de personas invalida."
            );

        } catch (IllegalArgumentException e) {

            invalidarDisponibilidad();

            Tema.error(this, e.getMessage());

        } catch (SQLException e) {

            invalidarDisponibilidad();

            mostrarErrorSQL(e);
        }
    }

    // CREAR LISTADO

    private JPanel crearListado() {

        JPanel listado =
                new JPanel(new BorderLayout(10, 12));

        listado.setOpaque(false);

        listado.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        lblRegistradas = Tema.subtitulo("");

        cmbEstado = new JComboBox<>(
                new String[]{
                    "TODOS",
                    "PENDIENTE",
                    "CONFIRMADA",
                    "CANCELADA",
                    "FINALIZADA"
                }
        );

        JButton btnRecargar = Tema.boton(
                "RECARGAR", Tema.GRIS
        );

        btnRecargar.addActionListener(
                e -> refrescar()
        );

        cmbEstado.addActionListener(
                e -> cargarReservas()
        );

        JPanel encabezado =
                new JPanel(new BorderLayout(10, 10));

        encabezado.setOpaque(false);

        encabezado.add(
                Tema.seccion("RESERVAS REGISTRADAS"),
                BorderLayout.NORTH
        );

        JPanel filtros =
                new JPanel(new FlowLayout(
                        FlowLayout.LEFT
                ));

        filtros.setOpaque(false);

        filtros.add(new JLabel("Estado:"));
        filtros.add(cmbEstado);
        filtros.add(btnRecargar);
        filtros.add(lblRegistradas);

        encabezado.add(
                filtros,
                BorderLayout.CENTER
        );

        listado.add(
                encabezado,
                BorderLayout.NORTH
        );

        modeloTabla = Tema.modelo(
                "ID",
                "Mesa",
                "Cliente",
                "Fecha",
                "Hora",
                "Personas",
                "Estado",
                "Empleado"
        );

        tabla = new JTable(modeloTabla);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.setRowSorter(
                new TableRowSorter<>(modeloTabla)
        );

        listado.add(
                Tema.tabla(tabla),
                BorderLayout.CENTER
        );

        JButton btnDetalle = Tema.boton(
                "VER DETALLE", Tema.GRIS
        );

        JButton btnConfirmar = Tema.boton(
                "CONFIRMAR", Tema.VERDE
        );

        JButton btnCancelar = Tema.boton(
                "CANCELAR", Tema.ROJO
        );

        JButton btnFinalizar = Tema.boton(
                "FINALIZAR", Tema.AMBAR
        );

        btnDetalle.addActionListener(
                e -> verDetalle()
        );

        btnConfirmar.addActionListener(
                e -> cambiarEstado("CONFIRMADA")
        );

        btnCancelar.addActionListener(
                e -> cambiarEstado("CANCELADA")
        );

        btnFinalizar.addActionListener(
                e -> cambiarEstado("FINALIZADA")
        );

        listado.add(
                Tema.filaBotones(
                        btnDetalle,
                        btnConfirmar,
                        btnCancelar,
                        btnFinalizar
                ),
                BorderLayout.SOUTH
        );

        return listado;
    }

    // CARGAR CLIENTES

    private void cargarClientes() {

        Cliente seleccionado =
                (Cliente) cmbCliente.getSelectedItem();

        int idSeleccionado = seleccionado == null
                ? 0 : seleccionado.getIdCliente();

        cmbCliente.removeAllItems();

        for (Cliente cliente :
                clienteControlador.listarClientes()) {

            cmbCliente.addItem(cliente);

            if (cliente.getIdCliente()
                    == idSeleccionado) {

                cmbCliente.setSelectedItem(cliente);
            }
        }
    }

    // CARGAR RESERVAS

    private void cargarReservas() {

        if (modeloTabla == null) {
            return;
        }

        modeloTabla.setRowCount(0);
        reservas.clear();

        try {

            String estado =
                    (String) cmbEstado.getSelectedItem();

            reservas =
                    controlador.listarPorEstado(estado);

            for (Reserva reserva : reservas) {

                modeloTabla.addRow(new Object[]{
                    reserva.getIdReserva(),
                    reserva.getNumeroMesa(),
                    reserva.getNombreCliente(),
                    reserva.getFechaReserva()
                            .format(formatoFecha),
                    reserva.getHoraReserva()
                            .format(formatoHora),
                    reserva.getCantidadPersonas(),
                    reserva.getEstado(),
                    reserva.getNombreUsuario()
                });
            }

            lblRegistradas.setText(
                    reservas.size() + " reservas"
            );

        } catch (SQLException e) {

            lblRegistradas.setText(
                    "Error de consulta"
            );

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            lblRegistradas.setText(
                    "Consulta no disponible"
            );

            Tema.error(this, e.getMessage());
        }
    }

    // OBTENER RESERVA SELECCIONADA

    private Reserva reservaSeleccionada() {

        int fila = tabla.getSelectedRow();

        if (fila < 0) {

            Tema.error(
                    this,
                    "Selecciona una reserva de la tabla."
            );

            return null;
        }

        int filaModelo =
                tabla.convertRowIndexToModel(fila);

        if (filaModelo < 0
                || filaModelo >= reservas.size()) {

            Tema.error(
                    this,
                    "La reserva seleccionada no es valida."
            );

            return null;
        }

        return reservas.get(filaModelo);
    }

    // VER DETALLE

    private void verDetalle() {

        Reserva seleccionada =
                reservaSeleccionada();

        if (seleccionada == null) {
            return;
        }

        try {

            Reserva reserva =
                    controlador.buscarPorId(
                            seleccionada.getIdReserva()
                    );

            if (reserva == null) {

                Tema.error(
                        this,
                        "La reserva ya no existe."
                );

                cargarReservas();
                return;
            }

            String observaciones =
                    reserva.getObservaciones();

            if (observaciones == null
                    || observaciones.isBlank()) {

                observaciones = "Sin observaciones";
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Reserva #"
                    + reserva.getIdReserva()
                    + "\nCliente: "
                    + reserva.getNombreCliente()
                    + "\nDocumento: "
                    + reserva.getDocumentoCliente()
                    + "\nMesa: "
                    + reserva.getNumeroMesa()
                    + "\nFecha: "
                    + reserva.getFechaReserva()
                            .format(formatoFecha)
                    + "\nHora: "
                    + reserva.getHoraReserva()
                            .format(formatoHora)
                    + "\nPersonas: "
                    + reserva.getCantidadPersonas()
                    + "\nEstado: "
                    + reserva.getEstado()
                    + "\nEmpleado: "
                    + reserva.getNombreUsuario()
                    + "\nObservaciones: "
                    + observaciones,
                    "Detalle de reserva",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }

    // CAMBIAR ESTADO

    private void cambiarEstado(String nuevoEstado) {

        Reserva reserva = reservaSeleccionada();

        if (reserva == null) {
            return;
        }

        // VALIDAR TRANSICION ANTES DE CONSULTAR MYSQL

        boolean permitido = switch (nuevoEstado) {

            case "CONFIRMADA" ->
                reserva.isPendiente();

            case "CANCELADA" ->
                reserva.isActiva();

            case "FINALIZADA" ->
                reserva.isConfirmada();

            default -> false;
        };

        if (!permitido) {

            Tema.error(
                    this,
                    "No se permite cambiar una reserva "
                    + reserva.getEstado()
                    + " a " + nuevoEstado + "."
            );

            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "Cambiar la reserva #"
                + reserva.getIdReserva()
                + " a " + nuevoEstado + "?",
                "Cambiar estado",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean resultado = switch (nuevoEstado) {

                case "CONFIRMADA" ->
                    controlador.confirmarReserva(
                            reserva.getIdReserva()
                    );

                case "CANCELADA" ->
                    controlador.cancelarReserva(
                            reserva.getIdReserva()
                    );

                case "FINALIZADA" ->
                    controlador.finalizarReserva(
                            reserva.getIdReserva()
                    );

                default -> false;
            };

            if (resultado) {

                Tema.info(
                        this,
                        "Estado actualizado correctamente."
                );

                invalidarDisponibilidad();
                cargarReservas();

            } else {

                Tema.error(
                        this,
                        "No se pudo actualizar la reserva."
                );
            }

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());

            cargarReservas();

        } catch (SQLException e) {

            mostrarErrorSQL(e);
        }
    }

    // SELECCIONAR MESA DESDE MESASPANEL

    public void seleccionarMesa(int numeroMesa) {

        buscarMesas();

        for (int i = 0;
                i < cmbMesa.getItemCount(); i++) {

            Mesa mesa = cmbMesa.getItemAt(i);

            if (mesa.getNumeroMesa() == numeroMesa) {

                cmbMesa.setSelectedIndex(i);
                return;
            }
        }

        cmbMesa.setSelectedIndex(-1);

        lblDisponibilidad.setText(
                "La mesa " + numeroMesa
                + " no es compatible con el horario "
                + "o la cantidad de personas."
        );
    }

    // LIMPIAR FORMULARIO

    private void limpiarFormulario() {

        txtFecha.setText(
                LocalDate.now(
                    ZoneId.of("America/Bogota")
                ).format(formatoFecha)
        );

        txtHora.setText("19:00");
        txtPersonas.setText("2");
        txtObservaciones.setText("");

        invalidarDisponibilidad();
    }

    // MOSTRAR ERROR SQL

    private void mostrarErrorSQL(SQLException e) {

        System.err.println(e.getMessage());

        Tema.error(
                this,
                "No se pudo completar la operacion en MySQL."
        );
    }

    // REFRESCAR MODULO

    @Override
    public void refrescar() {

        cargarClientes();
        invalidarDisponibilidad();
        cargarReservas();
    }
}
