
package vista;

import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import controlador.ClienteControlador;
import modelo.Cliente;

public class ClientesPanel extends JPanel
        implements Refrescable {

    private final Color OSCURO = new Color(34, 34, 34);
    private final Color ROJO = new Color(209, 51, 46);
    private final Color AMBAR = new Color(229, 169, 60);
    private final Color FONDO = new Color(250, 250, 250);
    private final Color GRIS = new Color(102, 102, 102);
    private final Color VERDE = new Color(65, 145, 75);

    private final ClienteControlador controlador =
            new ClienteControlador();

    private JTextField txtDocumento;
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtBuscar;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> ordenador;

    private JLabel lblTotal;

    private int idSeleccionado = -1;

    private ArrayList<Cliente> clientes = new ArrayList<>();

    public ClientesPanel() {

        setLayout(new BorderLayout(0, 15));
        setBackground(OSCURO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // TITULO PRINCIPAL

        JLabel titulo = new JLabel("GESTIÓN DE CLIENTES");

        titulo.setFont(
                new Font("SansSerif", Font.BOLD, 26)
        );

        titulo.setForeground(AMBAR);

        add(titulo, BorderLayout.NORTH);

        // PANEL PRINCIPAL

        JPanel contenido = new JPanel(
                new BorderLayout(15, 20)
        );

        contenido.setBackground(FONDO);

        contenido.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        contenido.add(
                crearFormulario(), BorderLayout.NORTH
        );

        contenido.add(
                crearListado(), BorderLayout.CENTER
        );

        add(contenido, BorderLayout.CENTER);

        actualizarTabla();
    }

    // CREAR FORMULARIO

    private JPanel crearFormulario() {

        JPanel formulario = new JPanel(
                new BorderLayout(10, 15)
        );

        formulario.setBackground(FONDO);

        JLabel subtitulo = new JLabel("DATOS DEL CLIENTE");

        subtitulo.setFont(
                new Font("SansSerif", Font.BOLD, 17)
        );

        subtitulo.setForeground(OSCURO);

        formulario.add(subtitulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(
                new GridLayout(2, 2, 15, 12)
        );

        campos.setBackground(FONDO);

        txtDocumento = new JTextField();
        txtNombre = new JTextField();
        txtTelefono = new JTextField();
        txtCorreo = new JTextField();

        campos.add(crearCampo(
                "Número de documento:", txtDocumento));

        campos.add(crearCampo(
                "Nombre completo:", txtNombre));

        campos.add(crearCampo(
                "Teléfono (opcional):", txtTelefono));

        campos.add(crearCampo(
                "Correo electrónico (opcional):", txtCorreo));

        formulario.add(campos, BorderLayout.CENTER);

        // BOTONES DEL FORMULARIO

        JPanel botones = new JPanel(
                new GridLayout(2, 3, 8, 8)
        );

        botones.setBackground(FONDO);

        JButton btnRegistrar = crearBoton(
                "+ REGISTRAR", AMBAR
        );

        JButton btnActualizar = crearBoton(
                "ACTUALIZAR", OSCURO
        );

        JButton btnLimpiar = crearBoton(
                "LIMPIAR", GRIS
        );

        JButton btnActivar = crearBoton(
                "ACTIVAR", VERDE
        );

        JButton btnDesactivar = crearBoton(
                "DESACTIVAR", ROJO
        );

        JButton btnRecargar = crearBoton(
                "RECARGAR", GRIS
        );

        btnRegistrar.addActionListener(e -> registrar());

        btnActualizar.addActionListener(e -> actualizar());

        btnLimpiar.addActionListener(e -> limpiarCampos());

        btnActivar.addActionListener(
                e -> cambiarEstado(true)
        );

        btnDesactivar.addActionListener(
                e -> cambiarEstado(false)
        );

        btnRecargar.addActionListener(e -> actualizarTabla());

        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnLimpiar);
        botones.add(btnActivar);
        botones.add(btnDesactivar);
        botones.add(btnRecargar);

        formulario.add(botones, BorderLayout.SOUTH);

        return formulario;
    }

    // CREAR CAMPOS

    private JPanel crearCampo(String texto,
                              JTextField campo) {

        JPanel panel = new JPanel(
                new BorderLayout(0, 7)
        );

        panel.setBackground(FONDO);

        JLabel etiqueta = new JLabel(texto);

        etiqueta.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        etiqueta.setForeground(OSCURO);

        campo.setPreferredSize(
                new Dimension(100, 30)
        );

        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);

        return panel;
    }

    // CREAR BOTONES

    private JButton crearBoton(String texto, Color color) {

        JButton boton = new JButton(texto);

        boton.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);

        boton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        boton.setPreferredSize(
                new Dimension(120, 32)
        );

        return boton;
    }

    // CREAR LISTADO

    private JPanel crearListado() {

        JPanel listado = new JPanel(
                new BorderLayout(10, 12)
        );

        listado.setBackground(FONDO);

        listado.setBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 0, 0, GRIS
                )
        );

        JLabel subtitulo = new JLabel(
                "CLIENTES REGISTRADOS"
        );

        subtitulo.setFont(
                new Font("SansSerif", Font.BOLD, 17)
        );

        subtitulo.setForeground(OSCURO);

        txtBuscar = new JTextField();

        txtBuscar.setToolTipText(
                "Buscar por documento, nombre, telefono o correo"
        );

        txtBuscar.setPreferredSize(
                new Dimension(100, 30)
        );

        JPanel panelBusqueda = new JPanel(
                new BorderLayout(0, 8)
        );

        panelBusqueda.setBackground(FONDO);

        panelBusqueda.add(
                subtitulo, BorderLayout.NORTH
        );

        panelBusqueda.add(
                txtBuscar, BorderLayout.CENTER
        );

        listado.add(panelBusqueda, BorderLayout.NORTH);

        // TABLA

        modeloTabla = new DefaultTableModel(
                new Object[]{
                    "ID",
                    "Documento",
                    "Nombre",
                    "Teléfono",
                    "Correo",
                    "Estado"
                }, 0
        ) {

            @Override
            public boolean isCellEditable(
                    int fila, int columna) {

                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {

                return columna == 0
                        ? Integer.class : String.class;
            }
        };

        tabla = new JTable(modeloTabla);

        tabla.setRowHeight(30);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        tabla.getTableHeader().setBackground(OSCURO);
        tabla.getTableHeader().setForeground(Color.WHITE);

        tabla.getColumnModel().getColumn(0)
                .setPreferredWidth(30);

        tabla.getColumnModel().getColumn(2)
                .setPreferredWidth(150);

        tabla.getColumnModel().getColumn(4)
                .setPreferredWidth(160);

        ordenador = new TableRowSorter<>(modeloTabla);

        tabla.setRowSorter(ordenador);

        JScrollPane scroll = new JScrollPane(tabla);

        listado.add(scroll, BorderLayout.CENTER);

        // CONTADOR DE CLIENTES

        lblTotal = new JLabel("Total de clientes: 0");

        lblTotal.setForeground(GRIS);

        listado.add(lblTotal, BorderLayout.SOUTH);

        // BUSCADOR AUTOMATICO

        txtBuscar.getDocument().addDocumentListener(
                new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });

        // SELECCIONAR CLIENTE

        tabla.getSelectionModel().addListSelectionListener(
                e -> {

            if (!e.getValueIsAdjusting()) {

                cargarSeleccion();
            }
        });

        return listado;
    }

    // FILTRAR CLIENTES

    private void filtrar() {

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

        lblTotal.setText(
                "Clientes visibles: " + tabla.getRowCount()
                + " de " + clientes.size()
        );
    }

    // CARGAR DATOS DEL CLIENTE SELECCIONADO

    private void cargarSeleccion() {

        int fila = tabla.getSelectedRow();

        if (fila == -1) {

            idSeleccionado = -1;
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);

        idSeleccionado = (Integer) modeloTabla.getValueAt(
                filaModelo, 0
        );

        txtDocumento.setText(
                modeloTabla.getValueAt(filaModelo, 1).toString()
        );

        txtNombre.setText(
                modeloTabla.getValueAt(filaModelo, 2).toString()
        );

        txtTelefono.setText(
                modeloTabla.getValueAt(filaModelo, 3).toString()
        );

        txtCorreo.setText(
                modeloTabla.getValueAt(filaModelo, 4).toString()
        );
    }

    // ACTUALIZAR TABLA DESDE MYSQL

    private void actualizarTabla() {

        limpiarCampos();

        clientes = controlador.listarTodos();

        modeloTabla.setRowCount(0);

        for (Cliente cliente : clientes) {

            modeloTabla.addRow(new Object[]{
                cliente.getIdCliente(),
                cliente.getDocumentoCliente(),
                cliente.getNombreCliente(),
                cliente.getTelefono() == null
                        ? "" : cliente.getTelefono(),
                cliente.getCorreo() == null
                        ? "" : cliente.getCorreo(),
                cliente.isEstado()
                        ? "ACTIVO" : "INACTIVO"
            });
        }

        filtrar();
    }

    // REGISTRAR CLIENTE

    private void registrar() {

        String documento = txtDocumento.getText().trim();
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();

        if (documento.isEmpty() || nombre.isEmpty()) {

            Tema.error(
                    this,
                    "El documento y el nombre son obligatorios."
            );

            return;
        }

        boolean registrado = controlador.registrarCliente(
                documento,
                nombre,
                telefono,
                correo
        );

        if (registrado) {

            Tema.info(
                    this,
                    "Cliente registrado correctamente."
            );

            actualizarTabla();

        } else {

            Tema.error(
                    this,
                    "No se pudo registrar el cliente.\n"
                    + "Verifica los datos, la conexión o si "
                    + "el documento ya está registrado."
            );
        }
    }

    // ACTUALIZAR CLIENTE

    private void actualizar() {

        if (idSeleccionado <= 0) {

            Tema.error(
                    this,
                    "Selecciona un cliente de la tabla."
            );

            return;
        }

        boolean actualizado = controlador.actualizarCliente(
                idSeleccionado,
                txtDocumento.getText().trim(),
                txtNombre.getText().trim(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim()
        );

        if (actualizado) {

            Tema.info(
                    this,
                    "Cliente actualizado correctamente."
            );

            actualizarTabla();

        } else {

            Tema.error(
                    this,
                    "No se pudo actualizar el cliente.\n"
                    + "Verifica los datos y que esté activo."
            );
        }
    }

    // ACTIVAR O DESACTIVAR CLIENTE

    private void cambiarEstado(boolean estado) {

        if (idSeleccionado <= 0) {

            Tema.error(
                    this,
                    "Selecciona un cliente de la tabla."
            );

            return;
        }

        Cliente cliente = controlador.buscarPorId(
                idSeleccionado
        );

        if (cliente == null) {

            Tema.error(
                    this,
                    "No se pudo consultar el cliente."
            );

            return;
        }

        if (cliente.isEstado() == estado) {

            Tema.info(
                    this,
                    "El cliente ya tiene ese estado."
            );

            return;
        }

        String accion = estado ? "activar" : "desactivar";

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas " + accion + " al cliente "
                        + cliente.getNombreCliente() + "?",
                "Confirmar operación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean actualizado = controlador.cambiarEstado(
                idSeleccionado, estado
        );

        if (actualizado) {

            Tema.info(
                    this,
                    estado
                    ? "Cliente activado correctamente."
                    : "Cliente desactivado correctamente."
            );

            actualizarTabla();

        } else {

            Tema.error(
                    this,
                    "No se pudo cambiar el estado del cliente."
            );
        }
    }

    // LIMPIAR CAMPOS

    private void limpiarCampos() {

        idSeleccionado = -1;

        txtDocumento.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");

        if (tabla != null) {
            tabla.clearSelection();
        }
    }

    // REFRESCAR AL ABRIR EL MODULO

    @Override
    public void refrescar() {

        actualizarTabla();
    }
}
