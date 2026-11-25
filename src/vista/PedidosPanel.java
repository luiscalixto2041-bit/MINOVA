
package vista;

import controlador.PedidoControlador;
import controlador.ProductoControlador;
import controlador.ClienteControlador;

import modelo.Pedido;
import modelo.DetallePedido;
import modelo.Producto;
import modelo.Cliente;
import modelo.Usuario;

import repositorio.ConexionMySQL;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PedidosPanel extends JPanel
        implements Refrescable {

    private final PedidoControlador controlador;

    private final ProductoControlador productoControlador =
            new ProductoControlador();

    private final ClienteControlador clienteControlador =
            new ClienteControlador();

    private final ArrayList<DetallePedido> carrito =
            new ArrayList<>();

    private final ArrayList<Pedido> pedidos =
            new ArrayList<>();

    private JComboBox<Cliente> cmbCliente;
    private JComboBox<MesaOpcion> cmbMesa;
    private JComboBox<String> cmbTipo;
    private JComboBox<Producto> cmbProducto;
    private JComboBox<String> cmbEstado;

    private JTextField txtCantidad;

    private JLabel lblPrecio;
    private JLabel lblStock;
    private JLabel lblTotal;

    private JTable tablaCarrito;
    private JTable tablaPedidos;

    private DefaultTableModel modeloCarrito;
    private DefaultTableModel modeloPedidos;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // CONSTRUCTOR

    public PedidosPanel(Usuario usuarioActual) {

        controlador = new PedidoControlador(usuarioActual);

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(
                new BorderLayout(10, 10)
        );

        contenido.setOpaque(false);

        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab(
                "NUEVO PEDIDO",
                crearFormulario()
        );

        pestanas.addTab(
                "PEDIDOS REGISTRADOS",
                crearListado()
        );

        contenido.add(pestanas, BorderLayout.CENTER);

        add(
                Tema.marcoModulo(
                        "GESTION DE PEDIDOS",
                        contenido
                ),
                BorderLayout.CENTER
        );

        refrescar();
    }

    // FORMULARIO DE NUEVO PEDIDO

    private JPanel crearFormulario() {

        JPanel panel = new JPanel(
                new BorderLayout(12, 12)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        JPanel superior = new JPanel(
                new GridLayout(0, 1, 8, 8)
        );

        JPanel datos = new JPanel(
                new GridLayout(1, 3, 10, 10)
        );

        cmbCliente = new JComboBox<>();

        cmbMesa = new JComboBox<>();

        cmbTipo = new JComboBox<>(new String[]{
            "LOCAL",
            "PARA_LLEVAR",
            "DOMICILIO"
        });

        datos.add(
                Tema.campoConEtiqueta(
                        "Cliente", cmbCliente
                )
        );

        datos.add(
                Tema.campoConEtiqueta(
                        "Tipo de pedido", cmbTipo
                )
        );

        datos.add(
                Tema.campoConEtiqueta(
                        "Mesa", cmbMesa
                )
        );

        superior.add(datos);

        JPanel productos = new JPanel(
                new GridLayout(1, 4, 10, 10)
        );

        cmbProducto = new JComboBox<>();

        txtCantidad = Tema.campo();
        txtCantidad.setText("1");

        lblPrecio = new JLabel("$ 0");
        lblStock = new JLabel("0 unidades");

        productos.add(
                Tema.campoConEtiqueta(
                        "Producto", cmbProducto
                )
        );

        productos.add(
                Tema.campoConEtiqueta(
                        "Cantidad", txtCantidad
                )
        );

        productos.add(
                Tema.campoConEtiqueta(
                        "Precio unitario", lblPrecio
                )
        );

        productos.add(
                Tema.campoConEtiqueta(
                        "Stock disponible", lblStock
                )
        );

        superior.add(productos);

        JButton btnAgregar = Tema.boton(
                "+ AGREGAR PRODUCTO",
                Tema.VERDE
        );

        JButton btnQuitar = Tema.boton(
                "QUITAR PRODUCTO",
                Tema.ROJO
        );

        JButton btnLimpiar = Tema.boton(
                "VACIAR PEDIDO",
                Tema.GRIS
        );

        btnAgregar.addActionListener(
                e -> agregarProducto()
        );

        btnQuitar.addActionListener(
                e -> quitarProducto()
        );

        btnLimpiar.addActionListener(
                e -> vaciarCarrito()
        );

        superior.add(
                Tema.filaBotones(
                        btnAgregar,
                        btnQuitar,
                        btnLimpiar
                )
        );

        panel.add(superior, BorderLayout.NORTH);

        modeloCarrito = Tema.modelo(
                "ID",
                "Producto",
                "Cantidad",
                "Precio unitario",
                "Subtotal"
        );

        tablaCarrito = new JTable(modeloCarrito);

        tablaCarrito.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        panel.add(
                Tema.tabla(tablaCarrito),
                BorderLayout.CENTER
        );

        JPanel inferior = new JPanel(
                new BorderLayout(10, 10)
        );

        lblTotal = new JLabel("TOTAL: $ 0");

        lblTotal.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        lblTotal.setForeground(Tema.ROJO);

        JButton btnRegistrar = Tema.boton(
                "+ REGISTRAR PEDIDO",
                Tema.ROJO
        );

        btnRegistrar.addActionListener(
                e -> registrarPedido()
        );

        inferior.add(lblTotal, BorderLayout.WEST);

        inferior.add(
                btnRegistrar,
                BorderLayout.EAST
        );

        panel.add(inferior, BorderLayout.SOUTH);

        cmbProducto.addActionListener(
                e -> actualizarProductoSeleccionado()
        );

        cmbTipo.addActionListener(
                e -> actualizarTipoPedido()
        );

        return panel;
    }

    // LISTADO DE PEDIDOS

    private JPanel crearListado() {

        JPanel panel = new JPanel(
                new BorderLayout(10, 10)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        cmbEstado = new JComboBox<>(new String[]{
            "TODOS",
            "PENDIENTE",
            "EN_PREPARACION",
            "LISTO",
            "ENTREGADO",
            "CANCELADO"
        });

        JButton btnRecargar = Tema.boton(
                "RECARGAR", Tema.GRIS
        );

        JPanel filtros = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        filtros.add(new JLabel("Filtrar por estado:"));
        filtros.add(cmbEstado);
        filtros.add(btnRecargar);

        panel.add(filtros, BorderLayout.NORTH);

        modeloPedidos = Tema.modelo(
                "ID",
                "Fecha",
                "Cliente",
                "Empleado",
                "Tipo",
                "Mesa (ID)",
                "Estado",
                "Total"
        );

        tablaPedidos = new JTable(modeloPedidos);

        tablaPedidos.setAutoCreateRowSorter(true);

        tablaPedidos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        panel.add(
                Tema.tabla(tablaPedidos),
                BorderLayout.CENTER
        );

        JButton btnDetalle = Tema.boton(
                "VER DETALLE", Tema.AMBAR
        );

        JButton btnAvanzar = Tema.boton(
                "AVANZAR ESTADO", Tema.VERDE
        );

        JButton btnCancelar = Tema.boton(
                "CANCELAR PEDIDO", Tema.ROJO
        );

        btnDetalle.addActionListener(
                e -> verDetalle()
        );

        btnAvanzar.addActionListener(
                e -> avanzarEstado()
        );

        btnCancelar.addActionListener(
                e -> cancelarPedido()
        );

        btnRecargar.addActionListener(
                e -> refrescar()
        );

        cmbEstado.addActionListener(
                e -> cargarPedidos()
        );

        panel.add(
                Tema.filaBotones(
                        btnDetalle,
                        btnAvanzar,
                        btnCancelar
                ),
                BorderLayout.SOUTH
        );

        return panel;
    }

    // OPCION PARA MOSTRAR MESAS

    private static class MesaOpcion {

        private final Integer id;
        private final String nombre;

        public MesaOpcion(
                Integer id, String nombre) {

            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    // CARGAR CLIENTES

    private void cargarClientes() {

        cmbCliente.removeAllItems();

        Cliente consumidor = new Cliente();

        consumidor.setIdCliente(0);
        consumidor.setNombreCliente(
                "Consumidor final"
        );
        consumidor.setDocumentoCliente("");

        cmbCliente.addItem(consumidor);

        for (Cliente cliente
                : clienteControlador.listarClientes()) {

            cmbCliente.addItem(cliente);
        }

        cmbCliente.setSelectedIndex(0);
    }

    // CARGAR MESAS DESDE MYSQL

    private void cargarMesas() throws SQLException {

        cmbMesa.removeAllItems();

        cmbMesa.addItem(
                new MesaOpcion(
                        null, "Seleccionar mesa"
                )
        );

        String sql = """
            SELECT id_mesa,
                   numero_mesa,
                   capacidad,
                   estado
            FROM mesa
            WHERE estado IN (
                'DISPONIBLE',
                'OCUPADA'
            )
            ORDER BY numero_mesa
            """;

        try (Connection con =
                ConexionMySQL.conectar();

                PreparedStatement ps =
                        con.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String nombre =
                        "Mesa " + rs.getInt("numero_mesa")
                        + " - "
                        + rs.getInt("capacidad")
                        + " personas - "
                        + rs.getString("estado");

                cmbMesa.addItem(
                        new MesaOpcion(
                                rs.getInt("id_mesa"),
                                nombre
                        )
                );
            }
        }
    }

    // CARGAR PRODUCTOS

    private void cargarProductos() {

        cmbProducto.removeAllItems();

        for (Producto producto
                : productoControlador.listarProductos()) {

            if (producto.getStock() > 0) {

                cmbProducto.addItem(producto);
            }
        }

        actualizarProductoSeleccionado();
    }

    // ACTUALIZAR TIPO DE PEDIDO

    private void actualizarTipoPedido() {

        boolean local = "LOCAL".equals(
                cmbTipo.getSelectedItem()
        );

        cmbMesa.setEnabled(local);

        if (!local && cmbMesa.getItemCount() > 0) {

            cmbMesa.setSelectedIndex(0);
        }
    }

    // MOSTRAR PRECIO Y STOCK

    private void actualizarProductoSeleccionado() {

        Producto producto = (Producto)
                cmbProducto.getSelectedItem();

        if (producto == null) {

            lblPrecio.setText("$ 0");
            lblStock.setText("0 unidades");

            return;
        }

        int reservado = 0;

        for (DetallePedido detalle : carrito) {

            if (detalle.getIdProducto()
                    == producto.getIdProducto()) {

                reservado += detalle.getCantidad();
            }
        }

        lblPrecio.setText(
                Tema.dinero(
                        producto.getPrecioProducto()
                )
        );

        lblStock.setText(
                Math.max(
                        0,
                        producto.getStock() - reservado
                ) + " unidades"
        );
    }

    // AGREGAR PRODUCTO AL CARRITO

    private void agregarProducto() {

        try {

            Producto seleccionado = (Producto)
                    cmbProducto.getSelectedItem();

            if (seleccionado == null) {

                throw new IllegalArgumentException(
                        "Selecciona un producto."
                );
            }

            int cantidad = Integer.parseInt(
                    txtCantidad.getText().trim()
            );

            if (cantidad <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            Producto producto =
                    productoControlador.buscarPorId(
                            seleccionado.getIdProducto()
                    );

            if (producto == null
                    || !producto.isEstado()) {

                throw new IllegalArgumentException(
                        "El producto no esta disponible."
                );
            }

            DetallePedido existente = null;

            for (DetallePedido detalle : carrito) {

                if (detalle.getIdProducto()
                        == producto.getIdProducto()) {

                    existente = detalle;
                    break;
                }
            }

            long cantidadTotal = cantidad;

            if (existente != null) {

                cantidadTotal += existente.getCantidad();
            }

            if (cantidadTotal > producto.getStock()) {

                throw new IllegalArgumentException(
                        "Stock insuficiente. Disponible: "
                        + producto.getStock()
                );
            }

            BigDecimal precio = BigDecimal.valueOf(
                    producto.getPrecioProducto()
            );

            if (existente != null) {

                existente.setCantidad(
                        (int) cantidadTotal
                );

                existente.setPrecioUnitario(precio);

            } else {

                carrito.add(new DetallePedido(
                        producto.getIdProducto(),
                        producto.getNombreProducto(),
                        cantidad,
                        precio
                ));
            }

            txtCantidad.setText("1");

            actualizarCarrito();

        } catch (NumberFormatException e) {

            Tema.error(
                    this,
                    "Ingresa una cantidad numerica valida."
            );

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }

    // QUITAR PRODUCTO

    private void quitarProducto() {

        int fila = tablaCarrito.getSelectedRow();

        if (fila < 0) {

            Tema.error(
                    this,
                    "Selecciona un producto del pedido."
            );

            return;
        }

        int indice =
                tablaCarrito.convertRowIndexToModel(fila);

        carrito.remove(indice);

        actualizarCarrito();
    }

    // VACIAR CARRITO

    private void vaciarCarrito() {

        if (!carrito.isEmpty()) {

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseas eliminar todos los productos?",
                    "Vaciar pedido",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        carrito.clear();

        actualizarCarrito();
    }

    // ACTUALIZAR TABLA DEL CARRITO

    private void actualizarCarrito() {

        modeloCarrito.setRowCount(0);

        for (DetallePedido detalle : carrito) {

            modeloCarrito.addRow(new Object[]{
                detalle.getIdProducto(),
                detalle.getNombreProducto(),
                detalle.getCantidad(),
                Tema.dinero(
                        detalle.getPrecioUnitario()
                                .doubleValue()
                ),
                Tema.dinero(
                        detalle.getSubtotal()
                                .doubleValue()
                )
            });
        }

        BigDecimal total =
                controlador.calcularTotal(carrito);

        lblTotal.setText(
                "TOTAL: "
                + Tema.dinero(total.doubleValue())
        );

        actualizarProductoSeleccionado();
    }

    // REGISTRAR PEDIDO

    private void registrarPedido() {

        try {

            if (carrito.isEmpty()) {

                throw new IllegalArgumentException(
                        "Agrega al menos un producto."
                );
            }

            Cliente cliente = (Cliente)
                    cmbCliente.getSelectedItem();

            MesaOpcion mesa = (MesaOpcion)
                    cmbMesa.getSelectedItem();

            String tipo = (String)
                    cmbTipo.getSelectedItem();

            Integer idCliente = null;
            Integer idMesa = null;

            if (cliente != null
                    && cliente.getIdCliente() > 0) {

                idCliente = cliente.getIdCliente();
            }

            if ("LOCAL".equals(tipo)
                    && mesa != null) {

                idMesa = mesa.id;
            }

            if ("LOCAL".equals(tipo)
                    && idMesa == null) {

                throw new IllegalArgumentException(
                        "Selecciona una mesa."
                );
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Confirmar el registro del pedido?\n"
                    + lblTotal.getText()
                    + "\nEl precio final se verifica en MySQL.",
                    "Confirmar pedido",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            int idPedido = controlador.registrarPedido(
                    idCliente,
                    idMesa,
                    tipo,
                    new ArrayList<>(carrito)
            );

            carrito.clear();
            actualizarCarrito();

            Tema.info(
                    this,
                    "Pedido #" + idPedido
                    + " registrado correctamente."
            );

            refrescar();

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());

        } catch (SQLException e) {

            mostrarErrorSQL(e);
        }
    }

    // CARGAR PEDIDOS REGISTRADOS

    private void cargarPedidos() {

        modeloPedidos.setRowCount(0);
        pedidos.clear();

        try {

            String estado = (String)
                    cmbEstado.getSelectedItem();

            pedidos.addAll(
                    controlador.listarPorEstado(estado)
            );

            for (Pedido pedido : pedidos) {

                modeloPedidos.addRow(new Object[]{
                    pedido.getIdPedido(),
                    pedido.getFechaPedido()
                            .format(FORMATO_FECHA),
                    pedido.getNombreCliente(),
                    pedido.getNombreUsuario(),
                    pedido.getTipoPedido(),
                    pedido.getIdMesa() == null
                            ? "-"
                            : pedido.getIdMesa(),
                    pedido.getEstado(),
                    Tema.dinero(
                            pedido.getTotal().doubleValue()
                    )
                });
            }

        } catch (SQLException e) {

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }

    // OBTENER PEDIDO SELECCIONADO

    private Pedido pedidoSeleccionado() {

        int fila = tablaPedidos.getSelectedRow();

        if (fila < 0) {

            Tema.error(
                    this,
                    "Selecciona un pedido de la tabla."
            );

            return null;
        }

        int indice =
                tablaPedidos.convertRowIndexToModel(fila);

        return pedidos.get(indice);
    }

    // MOSTRAR DETALLES

    private void verDetalle() {

        Pedido seleccionado = pedidoSeleccionado();

        if (seleccionado == null) {
            return;
        }

        try {

            Pedido pedido = controlador.consultarPedido(
                    seleccionado.getIdPedido()
            );

            if (pedido == null) {

                Tema.error(
                        this,
                        "El pedido no existe."
                );

                return;
            }

            DefaultTableModel modelo = Tema.modelo(
                    "Producto",
                    "Cantidad",
                    "Precio",
                    "Subtotal"
            );

            for (DetallePedido detalle
                    : pedido.getDetalles()) {

                modelo.addRow(new Object[]{
                    detalle.getNombreProducto(),
                    detalle.getCantidad(),
                    Tema.dinero(
                            detalle.getPrecioUnitario()
                                    .doubleValue()
                    ),
                    Tema.dinero(
                            detalle.getSubtotal()
                                    .doubleValue()
                    )
                });
            }

            JTable tabla = new JTable(modelo);

            JPanel panel = new JPanel(
                    new BorderLayout(10, 10)
            );

            panel.add(
                    new JLabel(
                            "Pedido #" + pedido.getIdPedido()
                            + " | " + pedido.getEstado()
                    ),
                    BorderLayout.NORTH
            );

            panel.add(
                    new JScrollPane(tabla),
                    BorderLayout.CENTER
            );

            JLabel total = new JLabel(
                    "TOTAL: "
                    + Tema.dinero(
                            pedido.getTotal().doubleValue()
                    )
            );

            total.setFont(
                    new Font(
                            "SansSerif",
                            Font.BOLD,
                            20
                    )
            );

            panel.add(total, BorderLayout.SOUTH);

            panel.setPreferredSize(
                    new Dimension(650, 300)
            );

            JOptionPane.showMessageDialog(
                    this,
                    panel,
                    "Detalle del pedido",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }

    // AVANZAR ESTADO

    private void avanzarEstado() {

        Pedido pedido = pedidoSeleccionado();

        if (pedido == null) {
            return;
        }

        String siguiente = controlador.siguienteEstado(
                pedido.getEstado()
        );

        if (siguiente == null) {

            Tema.error(
                    this,
                    "Este pedido no puede avanzar de estado."
            );

            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "Cambiar pedido #"
                + pedido.getIdPedido()
                + " a " + siguiente + "?",
                "Actualizar estado",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            controlador.cambiarEstado(
                    pedido.getIdPedido(),
                    siguiente
            );

            Tema.info(
                    this,
                    "Estado actualizado correctamente."
            );

            cargarPedidos();

        } catch (SQLException e) {

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }

    // CANCELAR PEDIDO

    private void cancelarPedido() {

        Pedido pedido = pedidoSeleccionado();

        if (pedido == null) {
            return;
        }

        if (!"PENDIENTE".equals(pedido.getEstado())) {

            Tema.error(
                    this,
                    "Solo se pueden cancelar pedidos pendientes."
            );

            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "Cancelar pedido #"
                + pedido.getIdPedido()
                + "?\nLas existencias seran devueltas.",
                "Cancelar pedido",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            controlador.cancelarPedido(
                    pedido.getIdPedido()
            );

            Tema.info(
                    this,
                    "Pedido cancelado correctamente."
            );

            refrescar();

        } catch (SQLException e) {

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }

    // MOSTRAR ERRORES SQL

    private void mostrarErrorSQL(SQLException e) {

        System.err.println(
                "Error de pedidos: " + e.getMessage()
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

            Cliente anterior = (Cliente)
                    cmbCliente.getSelectedItem();

            MesaOpcion mesaAnterior = (MesaOpcion)
                    cmbMesa.getSelectedItem();

            cargarClientes();
            cargarMesas();
            cargarProductos();

            if (anterior != null) {

                for (int i = 0;
                        i < cmbCliente.getItemCount(); i++) {

                    if (cmbCliente.getItemAt(i)
                            .getIdCliente()
                            == anterior.getIdCliente()) {

                        cmbCliente.setSelectedIndex(i);
                        break;
                    }
                }
            }

            if (mesaAnterior != null
                    && mesaAnterior.id != null) {

                for (int i = 0;
                        i < cmbMesa.getItemCount(); i++) {

                    if (mesaAnterior.id.equals(
                            cmbMesa.getItemAt(i).id)) {

                        cmbMesa.setSelectedIndex(i);
                        break;
                    }
                }
            }

            actualizarTipoPedido();
            actualizarCarrito();
            cargarPedidos();

        } catch (SQLException e) {

            mostrarErrorSQL(e);

        } catch (IllegalArgumentException e) {

            Tema.error(this, e.getMessage());
        }
    }
}
