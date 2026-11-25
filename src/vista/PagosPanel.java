
package vista;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controlador.PagoControlador;
import modelo.Pago;
import modelo.Pedido;
import modelo.Usuario;

public class PagosPanel extends JPanel implements Refrescable {

    private final PagoControlador controlador;

    private JComboBox<Pedido> cmbPedido;
    private JComboBox<String> cmbMetodo;
    private JComboBox<String> cmbFiltro;

    private JTextField txtRecibido;
    private JTextField txtReferencia;
    private JTextField txtBuscar;

    private JLabel lblCliente;
    private JLabel lblMesa;
    private JLabel lblTotalPedido;
    private JLabel lblCambio;

    private JLabel lblTotal;
    private JLabel lblCantidad;
    private JLabel lblPendientes;

    private JButton btnRegistrar;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> ordenador;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // CONSTRUCTOR

    public PagosPanel(Usuario usuarioActual) {

        controlador = new PagoControlador(usuarioActual);

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(
                new BorderLayout(15, 15)
        );

        contenido.add(
                crearResumen(),
                BorderLayout.NORTH
        );

        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab(
                "REGISTRAR PAGO",
                crearFormulario()
        );

        pestanas.addTab(
                "HISTORIAL DE PAGOS",
                crearListado()
        );

        contenido.add(pestanas, BorderLayout.CENTER);

        add(
                Tema.marcoModulo("GESTION DE PAGOS", contenido),
                BorderLayout.CENTER
        );

        refrescar();
    }

    // FORMATO MONETARIO

    private String dinero(BigDecimal valor) {

        NumberFormat formato = NumberFormat
                .getCurrencyInstance(
                        Locale.forLanguageTag("es-CO")
                );

        formato.setMinimumFractionDigits(0);
        formato.setMaximumFractionDigits(2);

        return formato.format(
                valor == null ? BigDecimal.ZERO : valor
        );
    }

    // TARJETAS DE RESUMEN

    private JPanel crearResumen() {

        lblTotal = new JLabel("$ 0");
        lblCantidad = new JLabel("0");
        lblPendientes = new JLabel("0");

        JPanel tarjetas = new JPanel(
                new GridLayout(1, 3, 15, 0)
        );

        tarjetas.setOpaque(false);

        tarjetas.add(
                Tema.tarjeta(
                        "Total recaudado",
                        lblTotal,
                        Tema.VERDE
                )
        );

        tarjetas.add(
                Tema.tarjeta(
                        "Pagos registrados",
                        lblCantidad,
                        Tema.OSCURO
                )
        );

        tarjetas.add(
                Tema.tarjeta(
                        "Pendientes de pago",
                        lblPendientes,
                        Tema.AMBAR
                )
        );

        return tarjetas;
    }

    // FORMULARIO DE REGISTRO

    private JPanel crearFormulario() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 15)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        cmbPedido = new JComboBox<>();

        cmbMetodo = new JComboBox<>(
                new String[]{
                    "EFECTIVO",
                    "TARJETA",
                    "TRANSFERENCIA"
                }
        );

        txtRecibido = Tema.campo();
        txtReferencia = Tema.campo();

        lblCliente = new JLabel("-");
        lblMesa = new JLabel("-");
        lblTotalPedido = new JLabel("$ 0");
        lblCambio = new JLabel("$ 0");

        lblTotalPedido.setFont(
                new Font("SansSerif", Font.BOLD, 28)
        );

        lblTotalPedido.setForeground(Tema.ROJO);

        lblCambio.setFont(
                new Font("SansSerif", Font.BOLD, 25)
        );

        lblCambio.setForeground(Tema.VERDE);

        // MOSTRAR INFORMACION EN EL COMBO

        cmbPedido.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

                if (value instanceof Pedido pedido) {

                    setText(
                            "Pedido #" + pedido.getIdPedido()
                            + " - " + pedido.getNombreCliente()
                            + " - " + dinero(pedido.getTotal())
                    );

                } else {

                    setText("Selecciona un pedido");
                }

                return this;
            }
        });

        JPanel campos = new JPanel(
                new GridLayout(4, 2, 15, 15)
        );

        campos.setOpaque(false);

        campos.add(
                Tema.campoConEtiqueta(
                        "Pedido pendiente de pago",
                        cmbPedido
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Metodo de pago",
                        cmbMetodo
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Cliente",
                        lblCliente
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Mesa / Tipo de pedido",
                        lblMesa
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Dinero recibido",
                        txtRecibido
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Referencia de transaccion",
                        txtReferencia
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "TOTAL A PAGAR",
                        lblTotalPedido
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "CAMBIO A DEVOLVER",
                        lblCambio
                )
        );

        btnRegistrar = Tema.boton(
                "+ REGISTRAR PAGO",
                Tema.VERDE
        );

        JButton btnLimpiar = Tema.boton(
                "LIMPIAR",
                Tema.GRIS
        );

        JButton btnRecargar = Tema.boton(
                "RECARGAR",
                Tema.OSCURO
        );

        btnRegistrar.addActionListener(
                e -> registrarPago()
        );

        btnLimpiar.addActionListener(
                e -> limpiarFormulario()
        );

        btnRecargar.addActionListener(
                e -> refrescar()
        );

        cmbPedido.addActionListener(
                e -> actualizarPedido()
        );

        cmbMetodo.addActionListener(
                e -> actualizarMetodo()
        );

        Tema.alCambiar(
                txtRecibido,
                () -> actualizarCambio()
        );

        JPanel formulario = new JPanel(
                new BorderLayout(10, 15)
        );

        formulario.setOpaque(false);

        formulario.add(
                Tema.seccion("REGISTRAR NUEVO PAGO"),
                BorderLayout.NORTH
        );

        formulario.add(
                campos,
                BorderLayout.CENTER
        );

        formulario.add(
                Tema.filaBotones(
                        btnRegistrar,
                        btnLimpiar,
                        btnRecargar
                ),
                BorderLayout.SOUTH
        );

        panel.add(formulario, BorderLayout.NORTH);

        return panel;
    }

    // HISTORIAL DE PAGOS

    private JPanel crearListado() {

        JPanel panel = new JPanel(
                new BorderLayout(10, 12)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        txtBuscar = Tema.campo();

        txtBuscar.setToolTipText(
                "Buscar por ID, cliente, empleado o referencia"
        );

        cmbFiltro = new JComboBox<>(
                new String[]{
                    "TODOS",
                    "EFECTIVO",
                    "TARJETA",
                    "TRANSFERENCIA"
                }
        );

        JPanel filtros = new JPanel(
                new GridLayout(1, 2, 15, 0)
        );

        filtros.setOpaque(false);

        filtros.add(
                Tema.campoConEtiqueta(
                        "Buscar pago",
                        txtBuscar
                )
        );

        filtros.add(
                Tema.campoConEtiqueta(
                        "Metodo de pago",
                        cmbFiltro
                )
        );

        JPanel encabezado = new JPanel(
                new BorderLayout(0, 10)
        );

        encabezado.setOpaque(false);

        encabezado.add(
                Tema.seccion("PAGOS REGISTRADOS"),
                BorderLayout.NORTH
        );

        encabezado.add(
                filtros,
                BorderLayout.CENTER
        );

        modeloTabla = Tema.modelo(
                "ID",
                "Pedido",
                "Cliente",
                "Metodo",
                "Valor",
                "Fecha",
                "Empleado",
                "Estado",
                "Referencia"
        );

        tabla = new JTable(modeloTabla);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        ordenador = new TableRowSorter<>(
                modeloTabla
        );

        tabla.setRowSorter(ordenador);

        JButton btnComprobante = Tema.boton(
                "GENERAR COMPROBANTE",
                Tema.OSCURO
        );

        JButton btnRecargar = Tema.boton(
                "RECARGAR",
                Tema.AMBAR
        );

        btnComprobante.addActionListener(
                e -> generarComprobante()
        );

        btnRecargar.addActionListener(
                e -> refrescar()
        );

        cmbFiltro.addActionListener(
                e -> cargarHistorial()
        );

        Tema.alCambiar(
                txtBuscar,
                () -> aplicarBusqueda()
        );

        panel.add(encabezado, BorderLayout.NORTH);

        panel.add(
                Tema.tabla(tabla),
                BorderLayout.CENTER
        );

        panel.add(
                Tema.filaBotones(
                        btnComprobante,
                        btnRecargar
                ),
                BorderLayout.SOUTH
        );

        return panel;
    }

    // PEDIDO SELECCIONADO

    private Pedido pedidoSeleccionado() {

        return (Pedido) cmbPedido.getSelectedItem();
    }

    // ACTUALIZAR INFORMACION DEL PEDIDO

    private void actualizarPedido() {

        Pedido pedido = pedidoSeleccionado();

        txtRecibido.setText("");
        txtReferencia.setText("");

        if (pedido == null) {

            lblCliente.setText("-");
            lblMesa.setText("-");
            lblTotalPedido.setText("$ 0");
            lblCambio.setText("$ 0");

            btnRegistrar.setEnabled(false);

            return;
        }

        lblCliente.setText(
                pedido.getNombreCliente()
        );

        if (pedido.getIdMesa() != null) {

            lblMesa.setText(
                    "Mesa ID: " + pedido.getIdMesa()
            );

        } else {

            lblMesa.setText(
                    pedido.getTipoPedido()
            );
        }

        lblTotalPedido.setText(
                dinero(pedido.getTotal())
        );

        actualizarMetodo();
    }

    // CAMBIAR METODO DE PAGO

    private void actualizarMetodo() {

        boolean efectivo = "EFECTIVO".equals(
                cmbMetodo.getSelectedItem()
        );

        txtRecibido.setEnabled(efectivo);
        txtReferencia.setEnabled(!efectivo);

        if (efectivo) {

            txtReferencia.setText("");

        } else {

            txtRecibido.setText("");
        }

        actualizarCambio();
    }

    // CALCULAR CAMBIO AUTOMATICAMENTE

    private void actualizarCambio() {

        Pedido pedido = pedidoSeleccionado();

        if (pedido == null) {

            lblCambio.setText("$ 0");
            btnRegistrar.setEnabled(false);

            return;
        }

        if (pedido.getTotal() == null
                || pedido.getTotal().signum() <= 0) {

            lblCambio.setText("Total invalido");
            btnRegistrar.setEnabled(false);

            return;
        }

        boolean efectivo = "EFECTIVO".equals(
                cmbMetodo.getSelectedItem()
        );

        if (!efectivo) {

            lblCambio.setText("No aplica");
            lblCambio.setForeground(Tema.OSCURO);
            btnRegistrar.setEnabled(true);

            return;
        }

        if (txtRecibido.getText().isBlank()) {

            lblCambio.setText("$ 0");
            lblCambio.setForeground(Tema.VERDE);
            btnRegistrar.setEnabled(false);

            return;
        }

        try {

            BigDecimal recibido =
                    controlador.convertirValor(
                            txtRecibido.getText()
                    );

            if (recibido.compareTo(
                    pedido.getTotal()) < 0) {

                BigDecimal faltante =
                        pedido.getTotal().subtract(recibido);

                lblCambio.setText(
                        "Faltan " + dinero(faltante)
                );

                lblCambio.setForeground(Tema.ROJO);
                btnRegistrar.setEnabled(false);

                return;
            }

            BigDecimal cambio =
                    controlador.calcularCambio(
                            pedido.getTotal(),
                            recibido
                    );

            lblCambio.setText(dinero(cambio));
            lblCambio.setForeground(Tema.VERDE);
            btnRegistrar.setEnabled(true);

        } catch (IllegalArgumentException e) {

            lblCambio.setText("Valor invalido");
            lblCambio.setForeground(Tema.ROJO);
            btnRegistrar.setEnabled(false);
        }
    }

    // REGISTRAR PAGO EN MYSQL

    private void registrarPago() {

        Pedido pedido = pedidoSeleccionado();

        if (pedido == null) {

            Tema.error(
                    this,
                    "Selecciona un pedido."
            );

            return;
        }

        try {

            String metodo = (String)
                    cmbMetodo.getSelectedItem();

            BigDecimal total = pedido.getTotal();

            BigDecimal recibido = total;
            BigDecimal cambio = BigDecimal.ZERO;

            String referencia =
                    txtReferencia.getText().trim();

            if ("EFECTIVO".equals(metodo)) {

                recibido = controlador.convertirValor(
                        txtRecibido.getText()
                );

                cambio = controlador.calcularCambio(
                        total,
                        recibido
                );

            } else if (referencia.isBlank()) {

                Tema.error(
                        this,
                        "Ingresa la referencia de la transaccion."
                );

                return;
            }

            String mensaje =
                    "Pedido #" + pedido.getIdPedido()
                    + "\nCliente: " + pedido.getNombreCliente()
                    + "\nMetodo: " + metodo
                    + "\nTotal: " + dinero(total);

            if ("EFECTIVO".equals(metodo)) {

                mensaje +=
                        "\nRecibido: " + dinero(recibido)
                        + "\nCambio: " + dinero(cambio);
            }

            mensaje +=
                    "\n\nConfirmas que recibiste el pago?";

            int confirmar =
                    JOptionPane.showConfirmDialog(
                            this,
                            mensaje,
                            "Confirmar pago",
                            JOptionPane.YES_NO_OPTION
                    );

            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }

            int idPago = controlador.registrarPago(
                    pedido.getIdPedido(),
                    metodo,
                    total,
                    recibido,
                    referencia
            );

            Tema.info(
                    this,
                    "Pago registrado correctamente."
                    + "\nComprobante #" + idPago
                    + "\nTotal: " + dinero(total)
                    + "\nCambio: " + dinero(cambio)
            );

            limpiarFormulario();

            refrescar();

            int comprobante =
                    JOptionPane.showConfirmDialog(
                            this,
                            "Deseas ver el comprobante?",
                            "Pago completado",
                            JOptionPane.YES_NO_OPTION
                    );

            if (comprobante == JOptionPane.YES_OPTION) {

                mostrarComprobante(idPago);
            }

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());

        } catch (SQLException e) {

            mostrarErrorSQL(e);
        }
    }

    // CARGAR PEDIDOS PENDIENTES

    private void cargarPedidos() throws SQLException {

        ArrayList<Pedido> pedidos =
                controlador.listarPedidosPendientes();

        cmbPedido.removeAllItems();

        cmbPedido.addItem(null);

        for (Pedido pedido : pedidos) {

            cmbPedido.addItem(pedido);
        }

        cmbPedido.setSelectedIndex(0);

        lblPendientes.setText(
                String.valueOf(pedidos.size())
        );

        actualizarPedido();
    }

    // CARGAR HISTORIAL MYSQL

    private void cargarHistorial() {

        modeloTabla.setRowCount(0);

        try {

            String metodo = (String)
                    cmbFiltro.getSelectedItem();

            for (Pago pago
                    : controlador.listarPorMetodo(metodo)) {

                modeloTabla.addRow(
                        new Object[]{
                            pago.getIdPago(),
                            pago.getIdPedido(),
                            pago.getNombreCliente(),
                            pago.getMetodoPago(),
                            dinero(pago.getValorPago()),
                            pago.getFechaPago()
                                    .format(formatoFecha),
                            pago.getNombreUsuario(),
                            pago.getEstado(),
                            pago.getReferenciaPago() == null
                                    ? "-"
                                    : pago.getReferenciaPago()
                        }
                );
            }

            aplicarBusqueda();

        } catch (SQLException e) {

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }

    // BUSQUEDA EN LA TABLA

    private void aplicarBusqueda() {

        String texto = txtBuscar.getText().trim();

        if (texto.isEmpty()) {

            ordenador.setRowFilter(null);

        } else {

            ordenador.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + Pattern.quote(texto)
                    )
            );
        }
    }

    // GENERAR COMPROBANTE SELECCIONADO

    private void generarComprobante() {

        int fila = tabla.getSelectedRow();

        if (fila < 0) {

            Tema.error(
                    this,
                    "Selecciona un pago de la tabla."
            );

            return;
        }

        int filaModelo =
                tabla.convertRowIndexToModel(fila);

        int idPago = Integer.parseInt(
                modeloTabla.getValueAt(
                        filaModelo, 0
                ).toString()
        );

        mostrarComprobante(idPago);
    }

    // MOSTRAR COMPROBANTE

    private void mostrarComprobante(int idPago) {

        try {

            String texto =
                    controlador.generarComprobante(
                            idPago
                    );

            JTextArea area = new JTextArea(
                    texto, 23, 42
            );

            area.setEditable(false);

            area.setFont(
                    new Font(
                            "Monospaced",
                            Font.PLAIN,
                            13
                    )
            );

            area.setCaretPosition(0);

            JScrollPane scroll =
                    new JScrollPane(area);

            JOptionPane.showMessageDialog(
                    this,
                    scroll,
                    "Comprobante de pago #" + idPago,
                    JOptionPane.PLAIN_MESSAGE
            );

        } catch (SQLException e) {

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }

    // LIMPIAR FORMULARIO

    private void limpiarFormulario() {

        if (cmbPedido.getItemCount() > 0) {

            cmbPedido.setSelectedIndex(0);
        }

        cmbMetodo.setSelectedIndex(0);

        txtRecibido.setText("");
        txtReferencia.setText("");

        actualizarPedido();
        actualizarMetodo();
    }

    // MOSTRAR ERRORES MYSQL

    private void mostrarErrorSQL(SQLException e) {

        System.err.println(
                "Error en PAGOS: " + e.getMessage()
        );

        Tema.error(
                this,
                "No se pudo completar la operacion en MySQL."
        );
    }

    // REFRESCAR MODULO

    @Override
    public void refrescar() {

        try {

            lblTotal.setText(
                    dinero(controlador.totalRecaudado())
            );

            lblCantidad.setText(
                    String.valueOf(
                            controlador.cantidadPagos()
                    )
            );

            cargarPedidos();

            cargarHistorial();

        } catch (SQLException e) {

            lblTotal.setText("--");
            lblCantidad.setText("--");
            lblPendientes.setText("--");

            cmbPedido.removeAllItems();
            modeloTabla.setRowCount(0);

            btnRegistrar.setEnabled(false);

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            lblTotal.setText("--");
            lblCantidad.setText("--");
            lblPendientes.setText("--");

            cmbPedido.removeAllItems();
            modeloTabla.setRowCount(0);

            btnRegistrar.setEnabled(false);

            Tema.error(this, e.getMessage());
        }
    }
}
