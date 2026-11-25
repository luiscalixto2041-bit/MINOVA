/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package vista;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import modelo.Usuario;
import repositorio.UsuarioRepositorio;
import seguridad.Permisos;

public class UsuariosPanel extends JPanel implements Refrescable {

    private final Usuario usuarioActual;
    private final UsuarioRepositorio repositorio;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private TableRowSorter<DefaultTableModel> ordenador;

    private List<Usuario> usuarios = new ArrayList<>();

    private final String[] roles = {
        "ADMINISTRADOR",
        "CAJERO",
        "MESERO",
        "INVENTARIO"
    };

    public UsuariosPanel(Usuario usuarioActual) {

        this.usuarioActual = usuarioActual;
        this.repositorio = new UsuarioRepositorio();

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        if (!Permisos.puedeAcceder(usuarioActual, "USUARIOS")) {

            add(new JLabel("Acceso no autorizado."));
            return;
        }

        // CABECERA

        JPanel cabecera = new JPanel();
        cabecera.setLayout(
                new BoxLayout(cabecera, BoxLayout.Y_AXIS)
        );
        cabecera.setOpaque(false);

        JLabel titulo = new JLabel("Gestión de empleados");

        titulo.setFont(
                new Font("SansSerif", Font.BOLD, 26)
        );

        titulo.setForeground(Tema.OSCURO);

        JLabel descripcion = new JLabel(
                "Administra los usuarios y permisos de MINOVA."
        );

        descripcion.setForeground(Tema.GRIS);

        cabecera.add(titulo);
        cabecera.add(Box.createVerticalStrut(8));
        cabecera.add(descripcion);
        cabecera.add(Box.createVerticalStrut(20));

        // BUSCADOR

        JPanel buscador = new JPanel(new BorderLayout(10, 0));
        buscador.setOpaque(false);

        txtBuscar = new JTextField();

        txtBuscar.setPreferredSize(
                new Dimension(200, 32)
        );

        buscador.add(new JLabel("Buscar:"), BorderLayout.WEST);
        buscador.add(txtBuscar, BorderLayout.CENTER);

        JButton btnActualizar = Tema.boton(
                "ACTUALIZAR", Tema.GRIS
        );

        btnActualizar.addActionListener(e -> refrescar());

        buscador.add(btnActualizar, BorderLayout.EAST);

        cabecera.add(buscador);

        add(cabecera, BorderLayout.NORTH);

        // TABLA DE USUARIOS

        modeloTabla = new DefaultTableModel(
                new String[]{
                    "ID",
                    "Nombre completo",
                    "Usuario",
                    "Rol",
                    "Estado"
                }, 0
        ) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 0 ? Integer.class : String.class;
            }
        };

        tabla = new JTable(modeloTabla);

        tabla.setRowHeight(32);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.getTableHeader().setBackground(Tema.OSCURO);
        tabla.getTableHeader().setForeground(Color.WHITE);

        tabla.getColumnModel().getColumn(0)
                .setPreferredWidth(35);

        tabla.getColumnModel().getColumn(1)
                .setPreferredWidth(180);

        ordenador = new TableRowSorter<>(modeloTabla);

        tabla.setRowSorter(ordenador);

        JScrollPane scroll = new JScrollPane(tabla);

        add(scroll, BorderLayout.CENTER);

        // FILTRAR USUARIOS

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

        // BOTONES

        JPanel acciones = new JPanel(
                new GridLayout(2, 3, 10, 10)
        );

        acciones.setOpaque(false);

        JButton btnNuevo = Tema.boton(
                "+ NUEVO EMPLEADO", Tema.MOSTAZA
        );

        JButton btnEditar = Tema.boton(
                "EDITAR", Tema.GRIS
        );

        JButton btnActivar = Tema.boton(
                "ACTIVAR", new Color(65, 145, 75)
        );

        JButton btnDesactivar = Tema.boton(
                "DESACTIVAR", Tema.ROJO
        );

        JButton btnContrasena = Tema.boton(
                "CONTRASEÑA", Tema.OSCURO
        );

        JButton btnLimpiar = Tema.boton(
                "LIMPIAR BÚSQUEDA", Tema.GRIS
        );

        btnNuevo.addActionListener(e -> registrar());

        btnEditar.addActionListener(e -> editar());

        btnActivar.addActionListener(
                e -> cambiarEstado(true)
        );

        btnDesactivar.addActionListener(
                e -> cambiarEstado(false)
        );

        btnContrasena.addActionListener(
                e -> restablecerContrasena()
        );

        btnLimpiar.addActionListener(e -> {

            txtBuscar.setText("");
            tabla.clearSelection();
        });

        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnContrasena);
        acciones.add(btnActivar);
        acciones.add(btnDesactivar);
        acciones.add(btnLimpiar);

        add(acciones, BorderLayout.SOUTH);
    }

    // 1. CARGAR USUARIOS DESDE MYSQL

    @Override
    public void refrescar() {

        if (!Permisos.puedeAcceder(
                usuarioActual, "USUARIOS")) {
            return;
        }

        try {

            usuarios = repositorio.listarUsuarios(
                    usuarioActual
            );

            modeloTabla.setRowCount(0);

            for (Usuario u : usuarios) {

                modeloTabla.addRow(new Object[]{
                    u.getIdUsuario(),
                    u.getNombreCompleto(),
                    u.getNombreUsuario(),
                    u.getRol(),
                    u.isEstado() ? "ACTIVO" : "INACTIVO"
                });
            }

        } catch (SQLException e) {

            usuarios.clear();
            modeloTabla.setRowCount(0);

            mostrarError(e);
        }
    }

    // 2. BUSCAR USUARIOS

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
    }

    // 3. OBTENER USUARIO SELECCIONADO

    private Usuario seleccionado() {

        int fila = tabla.getSelectedRow();

        if (fila == -1) {

            Tema.error(
                    this,
                    "Selecciona un empleado de la tabla."
            );

            return null;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);

        int id = (Integer) modeloTabla.getValueAt(
                filaModelo, 0
        );

        for (Usuario u : usuarios) {

            if (u.getIdUsuario() == id) {
                return u;
            }
        }

        return null;
    }

    // 4. REGISTRAR EMPLEADO

    private void registrar() {

        JTextField nombre = new JTextField();
        JTextField usuario = new JTextField();

        JPasswordField clave = new JPasswordField();
        JPasswordField confirmar = new JPasswordField();

        JComboBox<String> rol = new JComboBox<>(roles);

        // CAJERO COMO ROL PREDETERMINADO
        rol.setSelectedIndex(1);

        JPanel formulario = Tema.formulario(
                new String[]{
                    "Nombre completo:",
                    "Usuario:",
                    "Contraseña:",
                    "Confirmar contraseña:",
                    "Rol:"
                },
                new JComponent[]{
                    nombre,
                    usuario,
                    clave,
                    confirmar,
                    rol
                }
        );

        formulario.setPreferredSize(
                new Dimension(400, 220)
        );

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Registrar nuevo empleado",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        String nombreCompleto = nombre.getText().trim();
        String nombreUsuario = usuario.getText().trim();

        String contrasena = new String(clave.getPassword());
        String confirmacion = new String(confirmar.getPassword());

        clave.setText("");
        confirmar.setText("");

        int idRol = rol.getSelectedIndex() + 1;

        if (!contrasena.equals(confirmacion)) {

            Tema.error(
                    this,
                    "Las contraseñas no coinciden."
            );

            return;
        }

        try {

            if (repositorio.existeUsuario(nombreUsuario)) {

                Tema.error(
                        this,
                        "El nombre de usuario ya existe."
                );

                return;
            }

            boolean registrado = repositorio.registrarUsuario(
                    usuarioActual,
                    nombreCompleto,
                    nombreUsuario,
                    contrasena,
                    idRol
            );

            if (registrado) {

                Tema.info(
                        this,
                        "Empleado registrado correctamente."
                );

                refrescar();

            } else {

                Tema.error(
                        this,
                        "No se pudo registrar el empleado."
                );
            }

        } catch (SQLException | IllegalArgumentException e) {

            mostrarError(e);
        }
    }

    // 5. EDITAR EMPLEADO

    private void editar() {

        Usuario u = seleccionado();

        if (u == null) {
            return;
        }

        JTextField nombre = new JTextField(
                u.getNombreCompleto()
        );

        JTextField usuario = new JTextField(
                u.getNombreUsuario()
        );

        JComboBox<String> rol = new JComboBox<>(roles);

        rol.setSelectedItem(u.getRol());

        // IMPEDIR CAMBIAR EL PROPIO ROL

        if (u.getIdUsuario() == usuarioActual.getIdUsuario()) {

            rol.setEnabled(false);
        }

        JPanel formulario = Tema.formulario(
                new String[]{
                    "Nombre completo:",
                    "Usuario:",
                    "Rol:"
                },
                new JComponent[]{
                    nombre,
                    usuario,
                    rol
                }
        );

        formulario.setPreferredSize(
                new Dimension(400, 150)
        );

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Editar empleado",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            boolean actualizado = repositorio.actualizarUsuario(
                    usuarioActual,
                    u.getIdUsuario(),
                    nombre.getText().trim(),
                    usuario.getText().trim(),
                    rol.getSelectedIndex() + 1
            );

            if (actualizado) {

                Tema.info(
                        this,
                        "Empleado actualizado correctamente."
                );

                refrescar();

            } else {

                Tema.error(
                        this,
                        "No se pudo actualizar el empleado."
                );
            }

        } catch (SQLException | IllegalArgumentException e) {

            mostrarError(e);
        }
    }

    // 6. ACTIVAR O DESACTIVAR CUENTA

    private void cambiarEstado(boolean estado) {

        Usuario u = seleccionado();

        if (u == null) {
            return;
        }

        if (u.isEstado() == estado) {

            Tema.info(
                    this,
                    "El empleado ya tiene ese estado."
            );

            return;
        }

        if (u.getIdUsuario() == usuarioActual.getIdUsuario()
                && !estado) {

            Tema.error(
                    this,
                    "No puedes desactivar tu propia cuenta."
            );

            return;
        }

        String accion = estado ? "activar" : "desactivar";

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas " + accion + " la cuenta de "
                        + u.getNombreUsuario() + "?",
                "Confirmar operación",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean actualizado = repositorio.cambiarEstado(
                    usuarioActual,
                    u.getIdUsuario(),
                    estado
            );

            if (actualizado) {

                Tema.info(
                        this,
                        estado
                        ? "Cuenta activada correctamente."
                        : "Cuenta desactivada correctamente."
                );

                refrescar();

            } else {

                Tema.error(
                        this,
                        "No se pudo cambiar el estado."
                );
            }

        } catch (SQLException e) {

            mostrarError(e);
        }
    }

    // 7. RESTABLECER CONTRASENA

    private void restablecerContrasena() {

        Usuario u = seleccionado();

        if (u == null) {
            return;
        }

        JPasswordField nueva = new JPasswordField();
        JPasswordField confirmar = new JPasswordField();

        JPanel formulario = Tema.formulario(
                new String[]{
                    "Nueva contraseña:",
                    "Confirmar contraseña:"
                },
                new JComponent[]{
                    nueva,
                    confirmar
                }
        );

        formulario.setPreferredSize(
                new Dimension(400, 100)
        );

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Cambiar contraseña de " + u.getNombreUsuario(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        String contrasena = new String(nueva.getPassword());
        String confirmacion = new String(confirmar.getPassword());

        nueva.setText("");
        confirmar.setText("");

        if (!contrasena.equals(confirmacion)) {

            Tema.error(
                    this,
                    "Las contraseñas no coinciden."
            );

            return;
        }

        try {

            boolean actualizado =
                    repositorio.restablecerContrasena(
                            usuarioActual,
                            u.getIdUsuario(),
                            contrasena
                    );

            if (actualizado) {

                Tema.info(
                        this,
                        "Contraseña actualizada correctamente."
                );

            } else {

                Tema.error(
                        this,
                        "No se pudo actualizar la contraseña."
                );
            }

        } catch (SQLException | IllegalArgumentException e) {

            mostrarError(e);
        }
    }

    // 8. MANEJO DE ERRORES

    private void mostrarError(Exception e) {

        if (e instanceof IllegalArgumentException) {

            Tema.error(this, e.getMessage());

        } else if (e instanceof SQLException
                && ((SQLException) e).getErrorCode() == 1062) {

            Tema.error(
                    this,
                    "El nombre de usuario ya está registrado."
            );

        } else {

            Tema.error(
                    this,
                    "No se pudo completar la operación."
            );

            System.err.println(e.getMessage());
        }
    }
}

