package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import controlador.ProductoControlador;
import modelo.Producto;

public class InventarioPanel extends JPanel implements Refrescable {

    private final ProductoControlador controlador = new ProductoControlador();

    private JComboBox<String> cmbProducto;
    private JComboBox<String> cmbTipo;
    private JTextField txtCantidad;
    private JTextField txtBuscar;

    private JLabel lblControlados;
    private JLabel lblBajas;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public InventarioPanel() {

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(new BorderLayout(15, 15));
        contenido.add(crearFormulario(), BorderLayout.NORTH);
        contenido.add(crearListado(), BorderLayout.CENTER);

        add(Tema.marcoModulo("GESTIÓN DE INVENTARIO", contenido), BorderLayout.CENTER);

        refrescar();
    }

    // CONTADORES + FORMULARIO DE MOVIMIENTO
    private JPanel crearFormulario() {

        JPanel formulario = new JPanel(new BorderLayout(10, 12));
        formulario.setOpaque(false);

        lblControlados = new JLabel("0");
        lblBajas = new JLabel("0");

        JPanel contadores = new JPanel(new GridLayout(1, 2, 15, 0));
        contadores.setOpaque(false);
        contadores.add(Tema.tarjeta("Productos controlados", lblControlados, Tema.OSCURO));
        contadores.add(Tema.tarjeta("Existencias bajas", lblBajas, Tema.ROJO));

        formulario.add(contadores, BorderLayout.NORTH);

        // MOVIMIENTO
        cmbProducto = new JComboBox<>();
        cmbTipo = new JComboBox<>(new String[]{"Entrada", "Salida"});
        txtCantidad = new JTextField();

        JPanel campos = new JPanel(new GridLayout(1, 3, 15, 0));
        campos.setOpaque(false);
        campos.add(Tema.campoConEtiqueta("Producto:", cmbProducto));
        campos.add(Tema.campoConEtiqueta("Tipo de movimiento:", cmbTipo));
        campos.add(Tema.campoConEtiqueta("Cantidad:", txtCantidad));

        JButton btnRegistrar = Tema.boton("+ REGISTRAR MOVIMIENTO", Tema.AMBAR);
        btnRegistrar.addActionListener(e -> registrarMovimiento());

        JPanel movimiento = new JPanel(new BorderLayout(0, 8));
        movimiento.setOpaque(false);
        movimiento.add(Tema.seccion("REGISTRAR MOVIMIENTO"), BorderLayout.NORTH);
        movimiento.add(campos, BorderLayout.CENTER);
        movimiento.add(Tema.filaBotones(btnRegistrar), BorderLayout.SOUTH);

        formulario.add(movimiento, BorderLayout.CENTER);

        return formulario;
    }

    // LISTADO DE EXISTENCIAS
    private JPanel crearListado() {

        JPanel listado = new JPanel(new BorderLayout(10, 12));
        listado.setOpaque(false);
        listado.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GRIS));

        txtBuscar = Tema.campo();
        txtBuscar.setToolTipText("Buscar producto...");

        JPanel panelBusqueda = new JPanel(new BorderLayout(0, 8));
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(Tema.seccion("EXISTENCIAS"), BorderLayout.NORTH);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

        listado.add(panelBusqueda, BorderLayout.NORTH);

        modeloTabla = Tema.modelo("Producto", "Existencias", "Estado");
        tabla = new JTable(modeloTabla);

        // COLOR SEGUN EL ESTADO
        tabla.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean seleccionada, boolean foco, int fila, int columna) {

                Component celda = super.getTableCellRendererComponent(
                        t, valor, seleccionada, foco, fila, columna);

                if (!seleccionada) {
                    if ("Agotado".equals(valor)) {
                        celda.setForeground(Tema.ROJO);
                    } else if ("Bajo".equals(valor)) {
                        celda.setForeground(new Color(0xB07800));
                    } else {
                        celda.setForeground(Tema.VERDE);
                    }
                }

                return celda;
            }
        });

        listado.add(Tema.tabla(tabla), BorderLayout.CENTER);

        Tema.alCambiar(txtBuscar, () -> actualizarTabla());

        return listado;
    }

    // REGISTRAR ENTRADA O SALIDA
    private void registrarMovimiento() {

        String nombre = (String) cmbProducto.getSelectedItem();

        if (nombre == null) {
            Tema.error(this, "No hay productos registrados.");
            return;
        }

        int cantidad;

        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException e) {
            Tema.error(this, "La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            Tema.error(this, "La cantidad debe ser mayor que 0.");
            return;
        }

        Producto producto = controlador.buscarProducto(nombre);

        if (producto == null) {
            Tema.error(this, "El producto ya no existe.");
            refrescar();
            return;
        }

        int nuevoStock = producto.getStock() + cantidad;

        if ("Salida".equals(cmbTipo.getSelectedItem())) {
            nuevoStock = producto.getStock() - cantidad;
        }

        if (nuevoStock < 0) {
            Tema.error(this, "Solo hay " + producto.getStock() + " unidades disponibles.");
            return;
        }

        if (controlador.actualizarProducto(nombre, producto.getPrecioProducto(), nuevoStock)) {

            Tema.info(this, "Movimiento registrado. Existencias de " + nombre + ": " + nuevoStock);

            txtCantidad.setText("");
            refrescar();

        } else {

            Tema.error(this, "No se pudo registrar el movimiento.");
        }
    }

    // ACTUALIZAR TABLA
    private void actualizarTabla() {

        modeloTabla.setRowCount(0);

        String busqueda = "";

        if (txtBuscar != null) {
            busqueda = txtBuscar.getText().trim().toLowerCase();
        }

        for (Producto producto : controlador.listarProductos()) {

            if (producto.getNombreProducto().toLowerCase().contains(busqueda)) {

                modeloTabla.addRow(new Object[]{
                    producto.getNombreProducto(),
                    producto.getStock() + " unidades",
                    controlador.estadoStock(producto.getStock())
                });
            }
        }
    }

    @Override
    public void refrescar() {

        Object seleccionado = cmbProducto.getSelectedItem();
        cmbProducto.removeAllItems();

        for (Producto producto : controlador.listarProductos()) {
            cmbProducto.addItem(producto.getNombreProducto());
        }

        if (seleccionado != null) {
            cmbProducto.setSelectedItem(seleccionado);
        }

        lblControlados.setText(String.valueOf(controlador.listarProductos().size()));
        lblBajas.setText(String.valueOf(controlador.productosConAlerta()));

        actualizarTabla();
    }
}
