package vista;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import controlador.ClienteControlador;
import controlador.ReservaControlador;
import modelo.Cliente;
import modelo.Reserva;

public class ReservasPanel extends JPanel implements Refrescable {

    private final ReservaControlador controlador = new ReservaControlador();
    private final ClienteControlador clienteControlador = new ClienteControlador();

    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JComboBox<String> cmbCliente;
    private JComboBox<Integer> cmbMesa;
    private JTextField txtFecha;
    private JTextField txtHora;

    private JLabel lblRegistradas;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public ReservasPanel() {

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(new BorderLayout(15, 20));
        contenido.add(crearFormulario(), BorderLayout.NORTH);
        contenido.add(crearListado(), BorderLayout.CENTER);

        add(Tema.marcoModulo("GESTIÓN DE RESERVAS", contenido), BorderLayout.CENTER);

        refrescar();
    }

    private JPanel crearFormulario() {

        JPanel formulario = new JPanel(new BorderLayout(10, 12));
        formulario.setOpaque(false);

        formulario.add(Tema.seccion("REGISTRAR NUEVA RESERVA"), BorderLayout.NORTH);

        cmbCliente = new JComboBox<>();
        cmbMesa = new JComboBox<>();
        txtFecha = new JTextField();
        txtHora = new JTextField();

        for (int mesa = 1; mesa <= ReservaControlador.TOTAL_MESAS; mesa++) {
            cmbMesa.addItem(mesa);
        }

        txtFecha.setToolTipText("DD/MM/AAAA");
        txtHora.setToolTipText("HH:MM en formato de 24 horas");

        JPanel campos = new JPanel(new GridLayout(2, 2, 15, 10));
        campos.setOpaque(false);
        campos.add(Tema.campoConEtiqueta("Cliente:", cmbCliente));
        campos.add(Tema.campoConEtiqueta("Mesa:", cmbMesa));
        campos.add(Tema.campoConEtiqueta("Fecha (DD/MM/AAAA):", txtFecha));
        campos.add(Tema.campoConEtiqueta("Hora (HH:MM):", txtHora));

        formulario.add(campos, BorderLayout.CENTER);

        JButton btnRegistrar = Tema.boton("+ REGISTRAR RESERVA", Tema.AMBAR);
        btnRegistrar.addActionListener(e -> registrar());

        formulario.add(Tema.filaBotones(btnRegistrar), BorderLayout.SOUTH);

        return formulario;
    }

    // LISTADO DE RESERVAS
    private JPanel crearListado() {

        JPanel listado = new JPanel(new BorderLayout(10, 12));
        listado.setOpaque(false);
        listado.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GRIS));

        lblRegistradas = Tema.subtitulo("");

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Tema.seccion("RESERVAS REGISTRADAS"), BorderLayout.WEST);
        encabezado.add(lblRegistradas, BorderLayout.EAST);

        listado.add(encabezado, BorderLayout.NORTH);

        modeloTabla = Tema.modelo("Mesa", "Fecha", "Hora", "Documento", "Cliente");
        tabla = new JTable(modeloTabla);

        listado.add(Tema.tabla(tabla), BorderLayout.CENTER);

        return listado;
    }

    // USADO DESDE MESASPANEL PARA DEJAR LA MESA ELEGIDA
    public void seleccionarMesa(int numeroMesa) {

        cmbMesa.setSelectedItem(numeroMesa);

        if (txtFecha.getText().isEmpty()) {
            txtFecha.setText(LocalDate.now().format(formatoFecha));
        }
    }

    // REGISTRAR RESERVA
    private void registrar() {

        if (cmbCliente.getSelectedItem() == null) {
            Tema.error(this, "Primero registra un cliente en la sección CLIENTES.");
            return;
        }

        String documento = ((String) cmbCliente.getSelectedItem()).split(" - ")[0];
        int mesa = (Integer) cmbMesa.getSelectedItem();

        try {
            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim(), formatoFecha);
            LocalTime hora = LocalTime.parse(txtHora.getText().trim());

            boolean registrada = controlador.registrarReserva(
                    mesa, fecha.toString(), hora.toString(), documento);

            if (registrada) {

                Tema.info(this, "Reserva registrada correctamente.");

                txtFecha.setText("");
                txtHora.setText("");
                refrescar();

            } else {

                Tema.error(this,
                        "No se pudo registrar la reserva. Verifica si la mesa ya está reservada a esa hora.");
            }

        } catch (DateTimeParseException e) {

            Tema.error(this, "Fecha u hora inválida. Usa DD/MM/AAAA y HH:MM (por ejemplo 18:00).");
        }
    }

    @Override
    public void refrescar() {

        // CLIENTES DISPONIBLES
        Object seleccionado = cmbCliente.getSelectedItem();
        cmbCliente.removeAllItems();

        for (Cliente cliente : clienteControlador.listarClientes()) {
            cmbCliente.addItem(cliente.getDocumentoCliente() + " - " + cliente.getNombreCliente());
        }

        if (seleccionado != null) {
            cmbCliente.setSelectedItem(seleccionado);
        }

        // TABLA
        modeloTabla.setRowCount(0);

        for (Reserva reserva : controlador.listarReservas()) {

            Cliente cliente = clienteControlador.buscarCliente(reserva.getDocumentoCliente());
            String nombre = "(cliente no encontrado)";

            if (cliente != null) {
                nombre = cliente.getNombreCliente();
            }

            modeloTabla.addRow(new Object[]{
                reserva.getNumeroMesa(),
                reserva.getFechaReserva().format(formatoFecha),
                reserva.getHoraReserva(),
                reserva.getDocumentoCliente(),
                nombre
            });
        }

        lblRegistradas.setText(modeloTabla.getRowCount() + " registradas");
    }
}
