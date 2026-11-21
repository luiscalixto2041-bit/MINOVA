package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import controlador.ProductoControlador;
import modelo.Producto;

public class ProductosPanel extends JPanel implements Refrescable {

    private final ProductoControlador controlador = new ProductoControlador();

    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JTextField txtBuscar;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public ProductosPanel() {

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(new BorderLayout(15, 20));
        contenido.add(crearFormulario(), BorderLayout.NORTH);
        contenido.add(crearListado(), BorderLayout.CENTER);

        add(Tema.marcoModulo("GESTIÓN DE PRODUCTOS", contenido), BorderLayout.CENTER);

        actualizarTabla();
    }

    // FORMULARIO
    private JPanel crearFormulario() {

        JPanel formulario = new JPanel(new BorderLayout(10, 15));
        formulario.setOpaque(false);

        formulario.add(Tema.seccion("DATOS DEL PRODUCTO"), BorderLayout.NORTH);

        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtStock = new JTextField();
        txtPrecio.setToolTipText("Escriba el precio sin puntos ni comas");

        JPanel campos = new JPanel(new GridLayout(1, 3, 15, 0));
        campos.setOpaque(false);
        campos.add(Tema.campoConEtiqueta("Nombre:", txtNombre));
        campos.add(Tema.campoConEtiqueta("Precio:", txtPrecio));
        campos.add(Tema.campoConEtiqueta("Stock:", txtStock));

        formulario.add(campos, BorderLayout.CENTER);

        JButton btnRegistrar = Tema.boton("+ REGISTRAR PRODUCTO", Tema.AMBAR);
        JButton btnActualizar = Tema.boton("ACTUALIZAR DATOS", Tema.OSCURO);
        JButton btnEliminar = Tema.boton("ELIMINAR", Tema.ROJO);
        JButton btnLimpiar = Tema.boton("LIMPIAR", Tema.GRIS);

        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        formulario.add(
                Tema.filaBotones(btnRegistrar, btnActualizar, btnEliminar, btnLimpiar),
                BorderLayout.SOUTH);

        return formulario;
    }

    // LISTADO DE PRODUCTOS
    private JPanel crearListado() {

        JPanel listado = new JPanel(new BorderLayout(10, 12));
        listado.setOpaque(false);
        listado.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GRIS));

        txtBuscar = Tema.campo();
        txtBuscar.setToolTipText("Buscar por nombre...");

        JPanel panelBusqueda = new JPanel(new BorderLayout(0, 8));
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(Tema.seccion("PRODUCTOS REGISTRADOS"), BorderLayout.NORTH);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

        listado.add(panelBusqueda, BorderLayout.NORTH);

        modeloTabla = Tema.modelo("Nombre", "Precio", "Stock");
        tabla = new JTable(modeloTabla);

        listado.add(Tema.tabla(tabla), BorderLayout.CENTER);

        // BUSQUEDA AUTOMATICA
        Tema.alCambiar(txtBuscar, () -> actualizarTabla());

        // SELECCIONAR PRODUCTO
        tabla.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int fila = tabla.getSelectedRow();

                if (fila != -1) {
                    String nombre = modeloTabla.getValueAt(fila, 0).toString();
                    Producto producto = controlador.buscarProducto(nombre);

                    if (producto != null) {
                        txtNombre.setText(producto.getNombreProducto());
                        txtPrecio.setText(String.valueOf(producto.getPrecioProducto()));
                        txtStock.setText(String.valueOf(producto.getStock()));
                    }
                }
            }
        });

        return listado;
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
                    Tema.dinero(producto.getPrecioProducto()),
                    producto.getStock()
                });
            }
        }
    }

    // REGISTRAR PRODUCTO
    private void registrar() {

        String nombre = txtNombre.getText().trim();
        String textoPrecio = txtPrecio.getText().trim();
        String textoStock = txtStock.getText().trim();

        if (nombre.isEmpty() || textoPrecio.isEmpty() || textoStock.isEmpty()) {
            Tema.error(this, "Debes completar todos los campos.");
            return;
        }

        try {
            double precio = Double.parseDouble(textoPrecio);
            int stock = Integer.parseInt(textoStock);

            if (controlador.registrarProducto(nombre, precio, stock)) {

                Tema.info(this, "Producto registrado correctamente.");

                limpiarCampos();
                actualizarTabla();

            } else {

                Tema.error(this,
                        "No se pudo registrar. Verifica los datos o si el producto ya existe.");
            }

        } catch (NumberFormatException e) {

            Tema.error(this, "El precio debe ser un número y el stock un número entero.");
        }
    }

    // ACTUALIZAR PRODUCTO
    private void actualizar() {

        String nombre = txtNombre.getText().trim();
        String textoPrecio = txtPrecio.getText().trim();
        String textoStock = txtStock.getText().trim();

        if (nombre.isEmpty() || textoPrecio.isEmpty() || textoStock.isEmpty()) {
            Tema.error(this, "Debes completar todos los campos.");
            return;
        }

        try {
            double precio = Double.parseDouble(textoPrecio);
            int stock = Integer.parseInt(textoStock);

            if (controlador.actualizarProducto(nombre, precio, stock)) {

                Tema.info(this, "Producto actualizado correctamente.");

                limpiarCampos();
                actualizarTabla();

            } else {

                Tema.error(this,
                        "No se pudo actualizar. Verifica los datos o si el producto existe.");
            }

        } catch (NumberFormatException e) {

            Tema.error(this, "El precio debe ser un número y el stock un número entero.");
        }
    }

    // ELIMINAR PRODUCTO
    private void eliminar() {

        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            Tema.error(this, "Selecciona un producto o ingresa su nombre.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar este producto?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {

            if (controlador.eliminarProducto(nombre)) {

                Tema.info(this, "Producto eliminado correctamente.");

                limpiarCampos();
                actualizarTabla();

            } else {

                Tema.error(this, "No se encontró ningún producto con ese nombre.");
            }
        }
    }

    // LIMPIAR CAMPOS
    private void limpiarCampos() {

        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        tabla.clearSelection();
    }

    @Override
    public void refrescar() {
        actualizarTabla();
    }
}
