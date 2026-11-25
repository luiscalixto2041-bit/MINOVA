
package controlador;

import modelo.Cliente;
import repositorio.ClienteRepositorio;
import java.util.ArrayList;

public class ClienteControlador {

    private final ClienteRepositorio repositorio;

    public ClienteControlador() {
        repositorio = new ClienteRepositorio();
    }

    // VALIDAR DATOS
    private boolean validarDatos(String documento,
                                 String nombre,
                                 String telefono,
                                 String correo) {

        if (documento == null || documento.isBlank()
                || documento.trim().length() > 20) {
            return false;
        }

        if (nombre == null || nombre.isBlank()
                || nombre.trim().length() > 150) {
            return false;
        }

        if (telefono != null && !telefono.isBlank()) {

            if (telefono.length() > 20
                    || !telefono.matches("[0-9+() -]+")) {
                return false;
            }
        }

        if (correo != null && !correo.isBlank()) {

            if (correo.length() > 150
                    || !correo.matches(
                            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return false;
            }
        }

        return true;
    }

    private String opcional(String valor) {

        return valor == null || valor.isBlank()
                ? null : valor.trim();
    }

    // REGISTRAR CLIENTE
    // Compatibilidad con los modulos anteriores

    public boolean registrarCliente(String documento,
                                    String nombre) {

        return registrarCliente(
                documento, nombre, null, null
        );
    }

    // REGISTRAR CLIENTE COMPLETO

    public boolean registrarCliente(String documento,
                                    String nombre,
                                    String telefono,
                                    String correo) {

        if (!validarDatos(documento, nombre,
                telefono, correo)) {
            return false;
        }

        Cliente cliente = new Cliente(
                documento.trim(),
                nombre.trim(),
                opcional(telefono),
                opcional(correo)
        );

        return repositorio.registrar(cliente);
    }

    // BUSCAR POR DOCUMENTO

    public Cliente buscarCliente(String documento) {

        if (documento == null || documento.isBlank()) {
            return null;
        }

        return repositorio.buscar(documento.trim());
    }

    // BUSCAR POR ID

    public Cliente buscarPorId(int idCliente) {

        if (idCliente <= 0) {
            return null;
        }

        return repositorio.buscarPorId(idCliente);
    }

    // ACTUALIZAR NOMBRE
    // Compatibilidad con los modulos anteriores

    public boolean actualizarCliente(String documento,
                                     String nombre) {

        if (documento == null || documento.isBlank()
                || nombre == null || nombre.isBlank()) {
            return false;
        }

        return repositorio.actualizar(
                documento.trim(),
                nombre.trim()
        );
    }

    // ACTUALIZAR TODOS LOS DATOS

    public boolean actualizarCliente(int idCliente,
                                     String documento,
                                     String nombre,
                                     String telefono,
                                     String correo) {

        if (idCliente <= 0
                || !validarDatos(documento, nombre,
                        telefono, correo)) {
            return false;
        }

        Cliente cliente = new Cliente(
                idCliente,
                documento.trim(),
                nombre.trim(),
                opcional(telefono),
                opcional(correo),
                true
        );

        return repositorio.actualizar(cliente);
    }

    // ELIMINACION LOGICA

    public boolean eliminarCliente(String documento) {

        if (documento == null || documento.isBlank()) {
            return false;
        }

        return repositorio.eliminar(documento.trim());
    }

    // ACTIVAR O DESACTIVAR CLIENTE

    public boolean cambiarEstado(int idCliente,
                                 boolean estado) {

        if (idCliente <= 0) {
            return false;
        }

        return repositorio.cambiarEstado(
                idCliente, estado
        );
    }

    // LISTAR CLIENTES ACTIVOS

    public ArrayList<Cliente> listarClientes() {

        return repositorio.listar();
    }

    // LISTAR TODOS LOS CLIENTES

    public ArrayList<Cliente> listarTodos() {

        return repositorio.listarTodos();
    }
}
