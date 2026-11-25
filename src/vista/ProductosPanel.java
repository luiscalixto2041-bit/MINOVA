
package vista;

import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controlador.ProductoControlador;
import modelo.Producto;
import modelo.Usuario;
import seguridad.Permisos;

public class ProductosPanel extends JPanel implements Refrescable {

    private final ProductoControlador controlador =
            new ProductoControlador();

    private final boolean puedeEditar;

    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtMinimo;
    private JTextField txtMaximo;
    private JTextField txtBuscar;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> ordenador;

    private int idSeleccionado = -1;

    private ArrayList<Producto> productos = new ArrayList<>();

    public ProductosPanel(Usuario usuarioActual) {

        if (usuarioActual == null
                || !Permisos.puedeAcceder(
                        usuarioActual, "PRODUCTOS")) {

            throw new IllegalArgumentException(
                    "No tienes acceso a productos."
            );
        }

        puedeEditar = Permisos.puedeEditar(
                usuarioActual, "PRODUCTOS"
        );

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(
                new BorderLayout(15, 15)
        );

        if (puedeEditar) {

            contenido.add(
                    crearFormulario(), BorderLayout.NORTH
            );

        } else {

            contenido.add(
                    Tema.seccion("CATÁLOGO - SOLO CONSULTA"),
                    BorderLayout.NORTH
            );
        }

        contenido.add(
                crearListado(), BorderLayout.CENTER
        );

        add(Tema.marcoModulo(
                "GESTIÓN DE PRODUCTOS", contenido
        ), BorderLayout.CENTER);

        refrescar();
    }

    // FORMULARIO

    private JPanel crearFormulario() {

        JPanel formulario = new JPanel(
                new BorderLayout(10, 12)
        );

        formulario.setOpaque(false);

        formulario.add(
                Tema.seccion("DATOS DEL PRODUCTO"),
                BorderLayout.NORTH
        );

        txtNombre = new JTextField();
        txtDescripcion = new JTextField();
        txtPrecio = new JTextField();
        txtMinimo = new JTextField("5");
        txtMaximo = new JTextField("100");

        txtPrecio.setToolTipText(
                "Ejemplo: 18000 o 18000.50"
        );

        JPanel campos = new JPanel(
                new GridLayout(2, 3, 10, 10)
        );

        campos.setOpaque(false);

        campos.add(Tema.campoConEtiqueta(
                "Nombre:", txtNombre));

        campos.add(Tema.campoConEtiqueta(
                "Descripción:", txtDescripcion));

        campos.add(Tema.campoConEtiqueta(
                "Precio:", txtPrecio));

        campos.add(Tema.campoConEtiqueta(
                "Stock mínimo:", txtMinimo));

        campos.add(Tema.campoConEtiqueta(
                "Stock máximo:", txtMaximo));

        campos.add(new JLabel(
                "Stock inicial: 0 unidades"
        ));

        formulario.add(campos, BorderLayout.CENTER);

        JButton btnRegistrar = Tema.boton(
                "+ REGISTRAR", Tema.AMBAR
        );

        JButton btnActualizar = Tema.boton(
                "ACTUALIZAR", Tema.OSCURO
        );

        JButton btnActivar = Tema.boton(
                "ACTIVAR", Tema.VERDE
        );

        JButton btnDesactivar = Tema.boton(
                "DESACTIVAR", Tema.ROJO
        );

        JButton btnLimpiar = Tema.boton(
                "LIMPIAR", Tema.GRIS
        );

        JButton btnRecargar = Tema.boton(
                "RECARGAR", Tema.GRIS
        );

        btnRegistrar.addActionListener(e -> guardar(false));
        btnActualizar.addActionListener(e -> guardar(true));
        btnActivar.addActionListener(e -> cambiarEstado(true));
        btnDesactivar.addActionListener(e -> cambiarEstado(false));
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRecargar.addActionListener(e -> refrescar());

        JPanel botones = new JPanel(
                new GridLayout(2, 3, 8, 8)
        );

        botones.setOpaque(false);

        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnLimpiar);
        botones.add(btnActivar);
        botones.add(btnDesactivar);
        botones.add(btnRecargar);

        formulario.add(botones, BorderLayout.SOUTH);

        return formulario;
    }

    // LISTADO

    private JPanel crearListado() {

        JPanel listado = new JPanel(
                new BorderLayout(10, 10)
        );

        listado.setOpaque(false);

        txtBuscar = Tema.campo();

        txtBuscar.setToolTipText(
                "Buscar por nombre o descripción"
        );

        JPanel busqueda = new JPanel(
                new BorderLayout(0, 8)
        );

        busqueda.setOpaque(false);

        busqueda.add(
                Tema.seccion("PRODUCTOS REGISTRADOS"),
                BorderLayout.NORTH
        );

        busqueda.add(txtBuscar, BorderLayout.CENTER);

        listado.add(busqueda, BorderLayout.NORTH);

        modeloTabla = Tema.modelo(
                "ID",
                "Nombre",
                "Descripción",
                "Precio",
                "Stock",
                "Mínimo",
                "Máximo",
                "Estado"
        );

        tabla = new JTable(modeloTabla);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.getTableHeader()
                .setReorderingAllowed(false);

        ordenador = new TableRowSorter<>(modeloTabla);

        tabla.setRowSorter(ordenador);

        listado.add(
                Tema.tabla(tabla), BorderLayout.CENTER
        );

        Tema.alCambiar(txtBuscar, () -> filtrar());

        tabla.getSelectionModel()
                .addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        return listado;
    }

    // BUSCADOR

    private void filtrar() {

        String texto = txtBuscar.getText().trim();

        ordenador.setRowFilter(
                texto.isEmpty()
                ? null
                : RowFilter.regexFilter(
                        "(?iu)" + Pattern.quote(texto))
        );
    }

    // SELECCIONAR PRODUCTO

    private void cargarSeleccion() {

        if (!puedeEditar) {
            return;
        }

        int fila = tabla.getSelectedRow();

        if (fila < 0) {

            idSeleccionado = -1;
            return;
        }

        int filaModelo =
                tabla.convertRowIndexToModel(fila);

        idSeleccionado = Integer.parseInt(
                modeloTabla.getValueAt(
                        filaModelo, 0).toString()
        );

        for (Producto p : productos) {

            if (p.getIdProducto() == idSeleccionado) {

                txtNombre.setText(p.getNombreProducto());

                txtDescripcion.setText(
                        p.getDescripcionProducto() == null
                        ? "" : p.getDescripcionProducto()
                );

                txtPrecio.setText(
                        java.math.BigDecimal.valueOf(
                                p.getPrecioProducto())
                                .toPlainString()
                );

                txtMinimo.setText(
                        String.valueOf(p.getStockMinimo())
                );

                txtMaximo.setText(
                        String.valueOf(p.getStockMaximo())
                );

                break;
            }
        }
    }

    // REGISTRAR O ACTUALIZAR

    private void guardar(boolean actualizar) {

        if (!puedeEditar) {
            return;
        }

        if (actualizar && idSeleccionado <= 0) {

            Tema.error(this,
                    "Selecciona un producto de la tabla.");

            return;
        }

        try {

            String nombre = txtNombre.getText().trim();

            String descripcion =
                    txtDescripcion.getText().trim();

            double precio = Double.parseDouble(
                    txtPrecio.getText().trim()
            );

            int minimo = Integer.parseInt(
                    txtMinimo.getText().trim()
            );

            int maximo = Integer.parseInt(
                    txtMaximo.getText().trim()
            );

            if (nombre.isBlank()
                    || !Double.isFinite(precio)
                    || precio < 0
                    || minimo < 0
                    || maximo < minimo) {

                Tema.error(this,
                        "Verifica el nombre, precio y límites.");

                return;
            }

            boolean resultado;

            if (actualizar) {

                resultado = controlador.actualizarProducto(
                        idSeleccionado,
                        nombre,
                        descripcion,
                        precio,
                        minimo,
                        maximo
                );

            } else {

                resultado = controlador.registrarProducto(
                        nombre,
                        descripcion,
                        precio,
                        minimo,
                        maximo
                );
            }

            if (resultado) {

                Tema.info(this,
                        actualizar
                        ? "Producto actualizado correctamente."
                        : "Producto registrado correctamente.");

                refrescar();

            } else {

                Tema.error(this,
                        "No se pudo guardar el producto.\n"
                        + "Verifica los datos, el estado, "
                        + "la conexión o si el nombre ya existe.");
            }

        } catch (NumberFormatException e) {

            Tema.error(this,
                    "El precio y los límites deben ser numéricos.");
        }
    }

    // ACTIVAR O DESACTIVAR

    private void cambiarEstado(boolean estado) {

        if (!puedeEditar) {
            return;
        }

        if (idSeleccionado <= 0) {

            Tema.error(this,
                    "Selecciona un producto de la tabla.");

            return;
        }

        Producto p = controlador.buscarPorId(
                idSeleccionado
        );

        if (p == null) {

            Tema.error(this,
                    "No se pudo consultar el producto.");

            return;
        }

        if (p.isEstado() == estado) {

            Tema.info(this,
                    "El producto ya tiene ese estado.");

            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas "
                + (estado ? "activar" : "desactivar")
                + " el producto "
                + p.getNombreProducto() + "?",
                "Confirmar operación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        if (controlador.cambiarEstado(
                idSeleccionado, estado)) {

            Tema.info(this,
                    "Estado actualizado correctamente.");

            refrescar();

        } else {

            Tema.error(this,
                    "No se pudo cambiar el estado.");
        }
    }

    // LIMPIAR FORMULARIO

    private void limpiarCampos() {

        idSeleccionado = -1;

        if (puedeEditar) {

            txtNombre.setText("");
            txtDescripcion.setText("");
            txtPrecio.setText("");
            txtMinimo.setText("5");
            txtMaximo.setText("100");
        }

        if (tabla != null) {
            tabla.clearSelection();
        }
    }

    // CONSULTAR MYSQL

    @Override
    public void refrescar() {

        limpiarCampos();

        productos = puedeEditar
                ? controlador.listarTodos()
                : controlador.listarProductos();

        modeloTabla.setRowCount(0);

        for (Producto p : productos) {

            modeloTabla.addRow(new Object[]{
                p.getIdProducto(),
                p.getNombreProducto(),
                p.getDescripcionProducto(),
                Tema.dinero(p.getPrecioProducto()),
                p.getStock(),
                p.getStockMinimo(),
                p.getStockMaximo(),
                p.isEstado() ? "ACTIVO" : "INACTIVO"
            });
        }

        filtrar();
    }
}
