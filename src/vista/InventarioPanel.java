
package vista;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import controlador.InventarioControlador;
import modelo.Producto;
import modelo.Usuario;
import modelo.MovimientoInventario;

public class InventarioPanel extends JPanel
        implements Refrescable {

    private final InventarioControlador controlador;

    private JComboBox<Producto> cmbProducto;
    private JComboBox<String> cmbTipo;

    private JTextField txtCantidad;
    private JTextField txtMotivo;
    private JTextField txtBuscar;

    private JLabel lblControlados;
    private JLabel lblUnidades;
    private JLabel lblBajas;

    private JTable tabla;
    private JTable tablaHistorial;

    private DefaultTableModel modeloTabla;
    private DefaultTableModel modeloHistorial;

    private TableRowSorter<DefaultTableModel> ordenador;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm:ss"
            );

    public InventarioPanel(Usuario usuarioActual) {

        controlador = new InventarioControlador(
                usuarioActual
        );

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(
                new BorderLayout(15, 15)
        );

        contenido.add(
                crearFormulario(), BorderLayout.NORTH
        );

        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab(
                "EXISTENCIAS", crearListado()
        );

        pestanas.addTab(
                "HISTORIAL DE MOVIMIENTOS",
                crearHistorial()
        );

        contenido.add(pestanas, BorderLayout.CENTER);

        add(Tema.marcoModulo(
                "GESTIÓN DE INVENTARIO", contenido
        ), BorderLayout.CENTER);

        refrescar();
    }

    // FORMULARIO Y CONTADORES

    private JPanel crearFormulario() {

        JPanel formulario = new JPanel(
                new BorderLayout(10, 12)
        );

        formulario.setOpaque(false);

        lblControlados = new JLabel("0");
        lblUnidades = new JLabel("0");
        lblBajas = new JLabel("0");

        JPanel contadores = new JPanel(
                new GridLayout(1, 3, 10, 0)
        );

        contadores.setOpaque(false);

        contadores.add(Tema.tarjeta(
                "Productos", lblControlados, Tema.OSCURO
        ));

        contadores.add(Tema.tarjeta(
                "Unidades", lblUnidades, Tema.VERDE
        ));

        contadores.add(Tema.tarjeta(
                "Stock bajo", lblBajas, Tema.ROJO
        ));

        formulario.add(
                contadores, BorderLayout.NORTH
        );

        cmbProducto = new JComboBox<>();

        cmbTipo = new JComboBox<>(
                new String[]{"ENTRADA", "SALIDA"}
        );

        txtCantidad = new JTextField();
        txtMotivo = new JTextField();

        JPanel campos = new JPanel(
                new GridLayout(2, 2, 10, 8)
        );

        campos.setOpaque(false);

        campos.add(Tema.campoConEtiqueta(
                "Producto:", cmbProducto));

        campos.add(Tema.campoConEtiqueta(
                "Tipo de movimiento:", cmbTipo));

        campos.add(Tema.campoConEtiqueta(
                "Cantidad:", txtCantidad));

        campos.add(Tema.campoConEtiqueta(
                "Motivo:", txtMotivo));

        JButton btnRegistrar = Tema.boton(
                "+ REGISTRAR MOVIMIENTO", Tema.AMBAR
        );

        JButton btnRecargar = Tema.boton(
                "RECARGAR", Tema.GRIS
        );

        btnRegistrar.addActionListener(
                e -> registrarMovimiento()
        );

        btnRecargar.addActionListener(
                e -> refrescar()
        );

        JPanel movimiento = new JPanel(
                new BorderLayout(0, 8)
        );

        movimiento.setOpaque(false);

        movimiento.add(
                Tema.seccion("REGISTRAR MOVIMIENTO"),
                BorderLayout.NORTH
        );

        movimiento.add(campos, BorderLayout.CENTER);

        movimiento.add(
                Tema.filaBotones(
                        btnRegistrar, btnRecargar),
                BorderLayout.SOUTH
        );

        formulario.add(
                movimiento, BorderLayout.CENTER
        );

        return formulario;
    }

    // TABLA DE EXISTENCIAS

    private JPanel crearListado() {

        JPanel listado = new JPanel(
                new BorderLayout(10, 10)
        );

        listado.setOpaque(false);

        txtBuscar = Tema.campo();

        txtBuscar.setToolTipText(
                "Buscar producto..."
        );

        listado.add(txtBuscar, BorderLayout.NORTH);

        modeloTabla = Tema.modelo(
                "Producto",
                "Stock",
                "Mínimo",
                "Máximo",
                "Estado"
        );

        tabla = new JTable(modeloTabla);

        ordenador = new TableRowSorter<>(modeloTabla);

        tabla.setRowSorter(ordenador);

        tabla.getColumnModel().getColumn(4)
                .setCellRenderer(
                        new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable t,
                    Object valor,
                    boolean seleccionada,
                    boolean foco,
                    int fila,
                    int columna) {

                Component celda =
                        super.getTableCellRendererComponent(
                                t, valor, seleccionada,
                                foco, fila, columna
                        );

                if (!seleccionada) {

                    if ("Agotado".equals(valor)) {

                        celda.setForeground(Tema.ROJO);

                    } else if ("Bajo".equals(valor)
                            || "Exceso".equals(valor)) {

                        celda.setForeground(
                                new Color(0xB07800)
                        );

                    } else {

                        celda.setForeground(Tema.VERDE);
                    }
                }

                return celda;
            }
        });

        listado.add(
                Tema.tabla(tabla), BorderLayout.CENTER
        );

        Tema.alCambiar(txtBuscar, () -> filtrar());

        return listado;
    }

    // TABLA DE HISTORIAL

    private JPanel crearHistorial() {

        JPanel historial = new JPanel(
                new BorderLayout(10, 10)
        );

        historial.setOpaque(false);

        modeloHistorial = Tema.modelo(
                "ID",
                "Fecha",
                "Producto",
                "Tipo",
                "Cantidad",
                "Motivo",
                "Empleado"
        );

        tablaHistorial = new JTable(modeloHistorial);

        historial.add(
                Tema.tabla(tablaHistorial),
                BorderLayout.CENTER
        );

        return historial;
    }

    // FILTRAR EXISTENCIAS

    private void filtrar() {

        String texto = txtBuscar.getText().trim();

        ordenador.setRowFilter(
                texto.isEmpty()
                ? null
                : RowFilter.regexFilter(
                        "(?iu)" + Pattern.quote(texto))
        );
    }

    // REGISTRAR MOVIMIENTO

    private void registrarMovimiento() {

        Producto producto =
                (Producto) cmbProducto.getSelectedItem();

        if (producto == null) {

            Tema.error(this,
                    "Selecciona un producto.");

            return;
        }

        try {

            int cantidad = Integer.parseInt(
                    txtCantidad.getText().trim()
            );

            String tipo =
                    (String) cmbTipo.getSelectedItem();

            String motivo =
                    txtMotivo.getText().trim();

            int nuevoStock =
                    controlador.registrarMovimiento(
                            producto.getIdProducto(),
                            tipo,
                            cantidad,
                            motivo
                    );

            Tema.info(this,
                    "Movimiento registrado correctamente.\n"
                    + "Producto: "
                    + producto.getNombreProducto()
                    + "\nExistencias actuales: "
                    + nuevoStock
            );

            txtCantidad.setText("");
            txtMotivo.setText("");

            refrescar();

        } catch (NumberFormatException e) {

            Tema.error(this,
                    "La cantidad debe ser un número entero.");

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());

        } catch (SQLException e) {

            e.printStackTrace();

            Tema.error(this,
                    "No se pudo registrar el movimiento "
                    + "en MySQL.");
        }
    }

    // CARGAR EXISTENCIAS

    private void cargarExistencias() {

        Producto seleccionado =
                (Producto) cmbProducto.getSelectedItem();

        int idAnterior = seleccionado == null
                ? -1 : seleccionado.getIdProducto();

        ArrayList<Producto> productos =
                controlador.listarProductos();

        cmbProducto.removeAllItems();

        modeloTabla.setRowCount(0);

        long unidades = 0;
        int alertas = 0;

        for (Producto p : productos) {

            cmbProducto.addItem(p);

            if (p.getIdProducto() == idAnterior) {

                cmbProducto.setSelectedItem(p);
            }

            unidades += p.getStock();

            if (p.getStock() <= p.getStockMinimo()) {

                alertas++;
            }

            modeloTabla.addRow(new Object[]{
                p.getNombreProducto(),
                p.getStock(),
                p.getStockMinimo(),
                p.getStockMaximo(),
                p.getEstadoStock()
            });
        }

        lblControlados.setText(
                String.valueOf(productos.size())
        );

        lblUnidades.setText(
                String.valueOf(unidades)
        );

        lblBajas.setText(
                String.valueOf(alertas)
        );

        filtrar();
    }

    // CARGAR HISTORIAL DESDE MYSQL

    private void cargarHistorial() throws SQLException {

        ArrayList<MovimientoInventario> movimientos =
                controlador.listarMovimientos();

        modeloHistorial.setRowCount(0);

        for (MovimientoInventario m : movimientos) {

            modeloHistorial.addRow(new Object[]{
                m.getIdMovimiento(),
                m.getFechaMovimiento().format(formatoFecha),
                m.getNombreProducto(),
                m.getTipoMovimiento(),
                m.getCantidad(),
                m.getMotivo(),
                m.getNombreUsuario()
            });
        }
    }

    // REFRESCAR MODULO

    @Override
    public void refrescar() {

        try {

            cargarExistencias();
            cargarHistorial();

        } catch (SQLException e) {

            modeloHistorial.setRowCount(0);

            e.printStackTrace();

            Tema.error(this,
                    "No se pudo consultar el historial.");

        } catch (IllegalArgumentException e) {

            modeloTabla.setRowCount(0);
            modeloHistorial.setRowCount(0);
            cmbProducto.removeAllItems();

            Tema.error(this, e.getMessage());
        }
    }
}
