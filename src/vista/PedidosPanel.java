
package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;

import controlador.PedidoControlador;
import modelo.Pedido;

import controlador.ProductoControlador;
import modelo.Producto;

public class PedidosPanel extends JPanel implements Refrescable {

    private final Color OSCURO = new Color(34, 34, 34);
    private final Color ROJO = new Color(209, 51, 46);
    private final Color AMBAR = new Color(229, 169, 60);
    private final Color FONDO = new Color(250, 250, 250);
    private final Color GRIS = new Color(102, 102, 102);

    private JTextField txtId;
    private JComboBox<String> cmbProducto;
    private JTextField txtCantidad;
    private JComboBox<String> cmbPago;
    private JTextField txtValor;

    private JLabel lblTotal;
    private JTable tabla;
    private DefaultTableModel modelo;

    private final PedidoControlador controlador;
    
    private final ProductoControlador productoControlador = new ProductoControlador();
    
    public PedidosPanel() {
        
        controlador = new PedidoControlador();

        setLayout(new BorderLayout(15, 15));
        setBackground(OSCURO);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // TITULO
        JLabel titulo = new JLabel("PEDIDOS");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(AMBAR);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(OSCURO);
        encabezado.add(titulo, BorderLayout.WEST);

        add(encabezado, BorderLayout.NORTH);

        // FORMULARIO
        JPanel formulario = new JPanel(new BorderLayout(15, 15));
        formulario.setBackground(FONDO);
        formulario.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel subtitulo = new JLabel("NUEVO PEDIDO");
        subtitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        subtitulo.setForeground(OSCURO);

        formulario.add(subtitulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(0, 2, 12, 12));
        campos.setBackground(FONDO);

        txtId = new JTextField();
        cmbProducto = new JComboBox<>();
        txtCantidad = new JTextField();

        cmbPago = new JComboBox<>(new String[]{
            "Efectivo",
            "Tarjeta",
            "Transferencia"
        });

        txtValor = new JTextField();
        txtValor.setEditable(false);

        agregarCampo(campos, "ID del pedido:", txtId);
        agregarCampo(campos, "Producto:", cmbProducto);
        agregarCampo(campos, "Cantidad:", txtCantidad);
        agregarCampo(campos, "Método de pago:", cmbPago);

        JPanel contenidoFormulario = new JPanel(new BorderLayout(0, 15));
        contenidoFormulario.setBackground(FONDO);

        contenidoFormulario.add(campos, BorderLayout.NORTH);

        // TOTAL
        JPanel panelTotal = new JPanel(new GridLayout(0, 1));
        panelTotal.setBackground(FONDO);

        JLabel textoTotal = new JLabel("TOTAL DEL PEDIDO");
        textoTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        textoTotal.setForeground(GRIS);

        lblTotal = new JLabel("$ 0");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTotal.setForeground(ROJO);

        panelTotal.add(textoTotal);
        panelTotal.add(lblTotal);
        
        contenidoFormulario.add(panelTotal, BorderLayout.CENTER);

        formulario.add(contenidoFormulario, BorderLayout.CENTER);

        // BOTON REGISTRAR
        JButton btnRegistrar = new JButton("+ REGISTRAR PEDIDO");
        btnRegistrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnRegistrar.setBackground(ROJO);
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRegistrar.addActionListener(e -> registrar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.setBackground(FONDO);
        botones.add(btnRegistrar);

        formulario.add(botones, BorderLayout.SOUTH);

        // PANEL DE TABLA
        JPanel lista = new JPanel(new BorderLayout(0, 12));
        lista.setBackground(FONDO);
        lista.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel tituloLista = new JLabel("PEDIDOS REGISTRADOS");
        tituloLista.setFont(new Font("SansSerif", Font.BOLD, 18));
        tituloLista.setForeground(OSCURO);

        lista.add(tituloLista, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
            new String[]{
                "ID", "Producto", "Cantidad",
                "Método de pago", "Valor"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabla.getTableHeader().setFont(
            new Font("SansSerif", Font.BOLD, 13)
        );

        JScrollPane scroll = new JScrollPane(tabla);

        lista.add(scroll, BorderLayout.CENTER);

        // ORGANIZACION PRINCIPAL
        JPanel centro = new JPanel(new BorderLayout(15, 15));
        centro.setBackground(OSCURO);

        centro.add(formulario, BorderLayout.NORTH);
        centro.add(lista, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);

        // CARGAR PEDIDOS EXISTENTES
        actualizarTabla();
        cargarProductos();
        
        cmbProducto.addActionListener(e -> calcularTotal());
        txtCantidad.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                calcularTotal();
            }
        });
    }

    private void agregarCampo(
        JPanel panel, String texto, JComponent campo
    ) {

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 13));
        etiqueta.setForeground(OSCURO);

        panel.add(etiqueta);
        panel.add(campo);
    }

    private void registrar() {

        try {

            int id = Integer.parseInt(txtId.getText().trim());
            String producto = (String) cmbProducto.getSelectedItem();
            if (producto == null) {
                JOptionPane.showMessageDialog(this, "No hay productos registrados.");
                return;
            }
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            String pago = (String) cmbPago.getSelectedItem();

            calcularTotal();
            double valor = Double.parseDouble(txtValor.getText().trim());

            boolean registrado = controlador.registrarPedido(
                id, producto, cantidad, pago, valor
            );

            if (registrado) {

                JOptionPane.showMessageDialog(
                    this,
                    "Pedido registrado correctamente."
                );

                limpiarCampos();
                actualizarTabla();

            } else {

                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo registrar el pedido. Verifica los datos o el ID."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                this,
                "Verifica que el ID, la cantidad y el valor sean números válidos."
            );
        }
    }

    private void actualizarTabla() {

        modelo.setRowCount(0);

        for (Pedido p : controlador.listarPedidos()) {

            modelo.addRow(new Object[]{
                p.getIdPedido(),
                p.getNombreProducto(),
                p.getCantidad(),
                p.getMetodoPago(),
                "$ " + p.getValorPago()
            });
        }
    }

    private void limpiarCampos() {

        txtId.setText("");
        cmbProducto.setSelectedIndex(-1);
        txtCantidad.setText("");
        txtValor.setText("");

        cmbPago.setSelectedIndex(0);
        lblTotal.setText("$ 0");
    }   

    private void cargarProductos() {

    cmbProducto.removeAllItems();

    for (Producto p : productoControlador.listarProductos()) {
        cmbProducto.addItem(p.getNombreProducto());
    }
}
    // permite calcular el total automaticamente
    private void calcularTotal() {

    String producto = (String) cmbProducto.getSelectedItem();

    if (producto == null || txtCantidad.getText().isBlank()) {
        lblTotal.setText("$0");
        return;
    }

    try {
        int cantidad = Integer.parseInt(txtCantidad.getText());

        Producto p = productoControlador.buscarProducto(producto);

        if (p != null && cantidad > 0) {
            double total = p.getPrecioProducto() * cantidad;

            lblTotal.setText("$" + total);
            txtValor.setText(String.valueOf(total));
            
        } else {
            lblTotal.setText("$0");
        }

    } catch (NumberFormatException e) {
        lblTotal.setText("$0");
    }
}

    @Override
    public void refrescar() {
        cargarProductos();
        actualizarTabla();
    }
}
