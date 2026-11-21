package vista;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import controlador.PedidoControlador;
import controlador.ProductoControlador;
import controlador.ReservaControlador;
import modelo.Pedido;
import modelo.Producto;
import modelo.Reserva;

public class ReportesPanel extends JPanel implements Refrescable {

    private final PedidoControlador pedidoControlador = new PedidoControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();
    private final ReservaControlador reservaControlador = new ReservaControlador();

    // reportes guardados
    private final ArrayList<String[]> reportes = new ArrayList<>();

    private JComboBox<String> cmbTipo;
    private JTextArea areaResultados;
    private JTextField txtBuscar;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public ReportesPanel() {

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(new BorderLayout(15, 15));
        contenido.add(crearFormulario(), BorderLayout.NORTH);
        contenido.add(crearListado(), BorderLayout.CENTER);

        add(Tema.marcoModulo("GENERAR REPORTES", contenido), BorderLayout.CENTER);

        actualizarTabla();
    }

    private JPanel crearFormulario() {

        JPanel formulario = new JPanel(new BorderLayout(10, 10));
        formulario.setOpaque(false);

        cmbTipo = new JComboBox<>(new String[]{"Ventas", "Inventario", "Reservas"});

        JButton btnGenerar = Tema.boton("GENERAR REPORTE", Tema.AMBAR);
        btnGenerar.addActionListener(e -> generar());

        JPanel fila = new JPanel(new BorderLayout(15, 0));
        fila.setOpaque(false);
        fila.add(Tema.campoConEtiqueta("Tipo de reporte:", cmbTipo), BorderLayout.CENTER);
        fila.add(Tema.filaBotones(btnGenerar), BorderLayout.EAST);

        areaResultados = new JTextArea(7, 40);
        areaResultados.setEditable(false);
        areaResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaResultados.setText("(Aquí aparecen los resultados del reporte)");

        formulario.add(Tema.seccion("NUEVO REPORTE"), BorderLayout.NORTH);
        formulario.add(fila, BorderLayout.CENTER);
        formulario.add(new JScrollPane(areaResultados), BorderLayout.SOUTH);

        return formulario;
    }

    // LISTADO DE REPORTES 
    private JPanel crearListado() {

        JPanel listado = new JPanel(new BorderLayout(10, 12));
        listado.setOpaque(false);
        listado.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GRIS));

        txtBuscar = Tema.campo();
        txtBuscar.setToolTipText("Buscar por ID...");

        JPanel panelBusqueda = new JPanel(new BorderLayout(0, 8));
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(Tema.seccion("REPORTES GENERADOS"), BorderLayout.NORTH);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

        listado.add(panelBusqueda, BorderLayout.NORTH);

        modeloTabla = Tema.modelo("ID", "Tipo", "Fecha");
        tabla = new JTable(modeloTabla);

        listado.add(Tema.tabla(tabla), BorderLayout.CENTER);

        Tema.alCambiar(txtBuscar, () -> actualizarTabla());

        // MOSTRAR EL REPORTE SELECCIONADO
        tabla.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int fila = tabla.getSelectedRow();

                if (fila != -1) {
                    String id = modeloTabla.getValueAt(fila, 0).toString();

                    for (String[] reporte : reportes) {
                        if (reporte[0].equals(id)) {
                            areaResultados.setText(reporte[3]);
                        }
                    }
                }
            }
        });

        return listado;
    }

    // GENERAR REPORTE
    private void generar() {

        String tipo = (String) cmbTipo.getSelectedItem();
        String contenido;

        if (tipo.equals("Ventas")) {
            contenido = reporteVentas();
        } else if (tipo.equals("Inventario")) {
            contenido = reporteInventario();
        } else {
            contenido = reporteReservas();
        }

        String id = String.format("%03d", reportes.size() + 1);
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        reportes.add(new String[]{id, tipo, fecha, contenido});

        areaResultados.setText(contenido);
        actualizarTabla();
    }

    private String reporteVentas() {

        String texto = "REPORTE DE VENTAS\n\n";
        double total = 0;

        for (Pedido pedido : pedidoControlador.listarPedidos()) {

            texto = texto + "Pedido " + pedido.getIdPedido()
                    + " | " + pedido.getNombreProducto()
                    + " x" + pedido.getCantidad()
                    + " | " + pedido.getMetodoPago()
                    + " | " + Tema.dinero(pedido.getValorPago()) + "\n";

            total = total + pedido.getValorPago();
        }

        return texto + "\nPedidos: " + pedidoControlador.listarPedidos().size()
                + "\nTotal vendido: " + Tema.dinero(total);
    }

    private String reporteInventario() {

        String texto = "REPORTE DE INVENTARIO\n\n";

        for (Producto producto : productoControlador.listarProductos()) {

            texto = texto + producto.getNombreProducto()
                    + " | " + producto.getStock() + " unidades"
                    + " | " + productoControlador.estadoStock(producto.getStock()) + "\n";
        }

        return texto + "\nProductos con alerta: " + productoControlador.productosConAlerta();
    }

    private String reporteReservas() {

        String texto = "REPORTE DE RESERVAS\n\n";

        for (Reserva reserva : reservaControlador.listarReservas()) {

            texto = texto + "Mesa " + reserva.getNumeroMesa()
                    + " | " + reserva.getFechaReserva()
                    + " | " + reserva.getHoraReserva()
                    + " | Documento " + reserva.getDocumentoCliente() + "\n";
        }

        return texto + "\nReservas registradas: " + reservaControlador.listarReservas().size();
    }

    private void actualizarTabla() {

        modeloTabla.setRowCount(0);

        String busqueda = "";

        if (txtBuscar != null) {
            busqueda = txtBuscar.getText().trim();
        }

        for (String[] reporte : reportes) {

            if (reporte[0].contains(busqueda)) {
                modeloTabla.addRow(new Object[]{reporte[0], reporte[1], reporte[2]});
            }
        }
    }

    @Override
    public void refrescar() {
        actualizarTabla();
    }
}
