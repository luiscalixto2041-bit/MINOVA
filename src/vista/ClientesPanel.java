package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controlador.ClienteControlador;
import modelo.Cliente;

public class ClientesPanel extends JPanel implements Refrescable {

    private final Color OSCURO = new Color(34, 34, 34);
    private final Color ROJO = new Color(209, 51, 46);
    private final Color AMBAR = new Color(229, 169, 60);
    private final Color FONDO = new Color(250, 250, 250);
    private final Color GRIS = new Color(102, 102, 102);

    private final ClienteControlador controlador = new ClienteControlador();

    private JTextField txtDocumento;
    private JTextField txtNombre;
    private JTextField txtBuscar;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public ClientesPanel() {

        setLayout(new BorderLayout(0, 15));
        setBackground(OSCURO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // TITULO PRINCIPAL
        JLabel titulo = new JLabel("GESTIÓN DE CLIENTES");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(AMBAR);

        add(titulo, BorderLayout.NORTH);

        // PANEL BLANCO
        JPanel contenido = new JPanel(new BorderLayout(15, 20));
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(20, 20, 20, 20));

        contenido.add(crearFormulario(), BorderLayout.NORTH);
        contenido.add(crearListado(), BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);

        actualizarTabla();
    }

    // FORMULARIO
    private JPanel crearFormulario() {

        JPanel formulario = new JPanel(new BorderLayout(10, 15));
        formulario.setBackground(FONDO);

        JLabel subtitulo = new JLabel("DATOS DEL CLIENTE");
        subtitulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        subtitulo.setForeground(OSCURO);

        formulario.add(subtitulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(1, 2, 15, 0));
        campos.setBackground(FONDO);

        txtDocumento = new JTextField();
        txtNombre = new JTextField();

        campos.add(crearCampo("Número de documento:", txtDocumento));
        campos.add(crearCampo("Nombre:", txtNombre));

        formulario.add(campos, BorderLayout.CENTER);

        // BOTONES
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        botones.setBackground(FONDO);

        JButton btnRegistrar = crearBoton(" + REGISTRAR CLIENTE", AMBAR);
        JButton btnActualizar = crearBoton("ACTUALIZAR DATOS", OSCURO);
        JButton btnEliminar = crearBoton("ELIMINAR", ROJO);

        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());

        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);

        formulario.add(botones, BorderLayout.SOUTH);

        return formulario;
    }

    // CREAR CAMPO CON ETIQUETA
    private JPanel crearCampo(String texto, JTextField campo) {

        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setBackground(FONDO);

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 12));
        etiqueta.setForeground(OSCURO);

        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);

        return panel;
    }

    // BOTONES
    private JButton crearBoton(String texto, Color color) {

        JButton boton = new JButton(texto);

        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

    // LISTADO DE CLIENTES
    private JPanel crearListado() {

        JPanel listado = new JPanel(new BorderLayout(10, 12));
        listado.setBackground(FONDO);
        listado.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, GRIS));

        JLabel subtitulo = new JLabel("CLIENTES REGISTRADOS");
        subtitulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        subtitulo.setForeground(OSCURO);

        // BUSCADOR
        txtBuscar = new JTextField();
        txtBuscar.setToolTipText("Buscar por documento o nombre...");

        JPanel panelBusqueda = new JPanel(new BorderLayout(0, 8));
        panelBusqueda.setBackground(FONDO);
        panelBusqueda.add(subtitulo, BorderLayout.NORTH);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

        listado.add(panelBusqueda, BorderLayout.NORTH);

        // TABLA
        modeloTabla = new DefaultTableModel(
                new Object[]{"Documento", "Nombre"}, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(27);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(OSCURO);
        tabla.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(tabla);

        listado.add(scroll, BorderLayout.CENTER);

        // BUSQUEDA AUTOMATICA
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarTabla();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarTabla();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarTabla();
            }
        });

        // SELECCIONAR CLIENTE
        tabla.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int fila = tabla.getSelectedRow();

                if (fila != -1) {
                    txtDocumento.setText(
                            modeloTabla.getValueAt(fila, 0).toString());

                    txtNombre.setText(
                            modeloTabla.getValueAt(fila, 1).toString());
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

        for (Cliente cliente : controlador.listarClientes()) {

            String documento = cliente.getDocumentoCliente();
            String nombre = cliente.getNombreCliente();

            if (documento.toLowerCase().contains(busqueda)
                    || nombre.toLowerCase().contains(busqueda)) {

                modeloTabla.addRow(new Object[]{
                    documento,
                    nombre
                });
            }
        }
    }

    // REGISTRAR CLIENTE
    private void registrar() {

        String documento = txtDocumento.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (controlador.registrarCliente(documento, nombre)) {

            JOptionPane.showMessageDialog(
                    this, "Cliente registrado correctamente.");

            limpiarCampos();
            actualizarTabla();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo registrar. Verifica los datos o si el documento ya existe.");
        }
    }

    // ACTUALIZAR CLIENTE
    private void actualizar() {

        String documento = txtDocumento.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (controlador.actualizarCliente(documento, nombre)) {

            JOptionPane.showMessageDialog(
                    this, "Cliente actualizado correctamente.");

            limpiarCampos();
            actualizarTabla();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo actualizar. Verifica los datos o si el cliente existe.");
        }
    }

    // ELIMINAR CLIENTE
    private void eliminar() {

        String documento = txtDocumento.getText().trim();

        if (documento.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Selecciona un cliente o ingresa su documento.");

            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar este cliente?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {

            if (controlador.eliminarCliente(documento)) {

                JOptionPane.showMessageDialog(
                        this, "Cliente eliminado correctamente.");

                limpiarCampos();
                actualizarTabla();

            } else {

                JOptionPane.showMessageDialog(
                        this, "No se encontró un cliente con ese documento.");
            }
        }
    }

    // LIMPIAR CAMPOS
    private void limpiarCampos() {

        txtDocumento.setText("");
        txtNombre.setText("");
        tabla.clearSelection();
    }

    @Override
    public void refrescar() {
        actualizarTabla();
    }
}
