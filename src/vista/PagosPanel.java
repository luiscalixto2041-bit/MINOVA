package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import controlador.PedidoControlador;
import modelo.Pedido;

public class PagosPanel extends JPanel implements Refrescable {

    private final PedidoControlador controlador = new PedidoControlador();

    private JTextField txtBuscar;

    private JLabel lblTotal;
    private JLabel lblCantidad;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public PagosPanel() {

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(new BorderLayout(15, 15));
        contenido.add(crearResumen(), BorderLayout.NORTH);
        contenido.add(crearListado(), BorderLayout.CENTER);

        add(Tema.marcoModulo("PAGOS", contenido), BorderLayout.CENTER);

        refrescar();
    }

    // RESUMEN Y BOTON DE COMPROBANTE
    private JPanel crearResumen() {

        lblTotal = new JLabel("$ 0");
        lblCantidad = new JLabel("0");

        JPanel tarjetas = new JPanel(new GridLayout(1, 2, 15, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(Tema.tarjeta("Total recaudado", lblTotal, Tema.ROJO));
        tarjetas.add(Tema.tarjeta("Pagos registrados", lblCantidad, Tema.OSCURO));

        JButton btnComprobante = Tema.boton("GENERAR COMPROBANTE", Tema.OSCURO);
        btnComprobante.addActionListener(e -> generarComprobante());

        JPanel resumen = new JPanel(new BorderLayout(0, 10));
        resumen.setOpaque(false);
        resumen.add(tarjetas, BorderLayout.CENTER);
        resumen.add(Tema.filaBotones(btnComprobante), BorderLayout.SOUTH);

        return resumen;
    }

    // LISTADO DE PAGOS
    private JPanel crearListado() {

        JPanel listado = new JPanel(new BorderLayout(10, 12));
        listado.setOpaque(false);
        listado.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GRIS));

        txtBuscar = Tema.campo();
        txtBuscar.setToolTipText("Buscar por ID...");

        JPanel panelBusqueda = new JPanel(new BorderLayout(0, 8));
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(Tema.seccion("PAGOS REGISTRADOS"), BorderLayout.NORTH);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

        listado.add(panelBusqueda, BorderLayout.NORTH);

        modeloTabla = Tema.modelo("ID", "Producto", "Método de pago", "Valor");
        tabla = new JTable(modeloTabla);

        listado.add(Tema.tabla(tabla), BorderLayout.CENTER);

        Tema.alCambiar(txtBuscar, () -> actualizarTabla());

        return listado;
    }

    // COMPROBANTE DEL PAGO SELECCIONADO
    private void generarComprobante() {

        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            Tema.error(this, "Selecciona un pago de la tabla.");
            return;
        }

        int id = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
        Pedido pedido = controlador.buscarPedido(id);

        if (pedido == null) {
            Tema.error(this, "No se encontró el pedido.");
            return;
        }

        String texto = "MINOVA - BITS ANGUS BURGER\n"
                + "COMPROBANTE DE PAGO\n\n"
                + "ID del pedido: " + pedido.getIdPedido() + "\n"
                + "Producto: " + pedido.getNombreProducto() + "\n"
                + "Cantidad: " + pedido.getCantidad() + "\n"
                + "Método de pago: " + pedido.getMetodoPago() + "\n"
                + "TOTAL: " + Tema.dinero(pedido.getValorPago()) + "\n";

        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JOptionPane.showMessageDialog(this, area, "Comprobante", JOptionPane.PLAIN_MESSAGE);
    }

    // ACTUALIZAR TABLA
    private void actualizarTabla() {

        modeloTabla.setRowCount(0);

        String busqueda = "";

        if (txtBuscar != null) {
            busqueda = txtBuscar.getText().trim();
        }

        for (Pedido pedido : controlador.listarPedidos()) {

            if (String.valueOf(pedido.getIdPedido()).contains(busqueda)) {

                modeloTabla.addRow(new Object[]{
                    pedido.getIdPedido(),
                    pedido.getNombreProducto(),
                    pedido.getMetodoPago(),
                    Tema.dinero(pedido.getValorPago())
                });
            }
        }
    }

    @Override
    public void refrescar() {

        double total = 0;

        for (Pedido pedido : controlador.listarPedidos()) {
            total = total + pedido.getValorPago();
        }

        lblTotal.setText(Tema.dinero(total));
        lblCantidad.setText(String.valueOf(controlador.listarPedidos().size()));

        actualizarTabla();
    }
}
