/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import java.util.ArrayList;
import modelo.Cliente;

public class ClienteRepositorio {

    private static final ArrayList<Cliente> clientes = new ArrayList<>();

    // Registrar un cliente nuevo
    public boolean registrar(Cliente cliente) {
        if (cliente == null || cliente.getDocumentoCliente() == null
                || cliente.getDocumentoCliente().isBlank()
                || buscar(cliente.getDocumentoCliente()) != null) {
            return false;
        }

        clientes.add(cliente);
        return true;
    }

    // Buscar un cliente por documento
    public Cliente buscar(String documento) {
        for (Cliente cliente : clientes) {
            if (cliente.getDocumentoCliente().equals(documento)) {
                return cliente;
            }
        }
        return null;
    }

    // Actualizar el nombre de un cliente
    public boolean actualizar(String documento, String nuevoNombre) {
        Cliente cliente = buscar(documento);

        if (cliente != null) {
            cliente.setNombreCliente(nuevoNombre);
            return true;
        }
        return false;
    }

    // Eliminar un cliente
    public boolean eliminar(String documento) {
        Cliente cliente = buscar(documento);

        if (cliente != null) {
            clientes.remove(cliente);
            return true;
        }
        return false;
    }
    
    public ArrayList<Cliente> listar() {
        return new ArrayList<>(clientes);
    }
}
