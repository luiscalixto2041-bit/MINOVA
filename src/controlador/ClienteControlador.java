/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import modelo.Cliente;
import repositorio.ClienteRepositorio;
import java.util.ArrayList;

public class ClienteControlador {

    private final ClienteRepositorio repositorio;

    public ClienteControlador() {
        repositorio = new ClienteRepositorio();
    }

    // Registrar cliente
    public boolean registrarCliente(String documento, String nombre) {
        if (documento == null || documento.isBlank()
                || nombre == null || nombre.isBlank()) {
            return false;
        }

        Cliente cliente = new Cliente(documento.trim(), nombre.trim());
        return repositorio.registrar(cliente);
    }

    // Buscar cliente
    public Cliente buscarCliente(String documento) {
        if (documento == null || documento.isBlank()) {
            return null;
        }

        return repositorio.buscar(documento.trim());
    }

    // Actualizar cliente
    public boolean actualizarCliente(String documento, String nombre) {
        if (documento == null || documento.isBlank()
                || nombre == null || nombre.isBlank()) {
            return false;
        }

        return repositorio.actualizar(documento.trim(), nombre.trim());
    }

    // Eliminar cliente
    public boolean eliminarCliente(String documento) {
        if (documento == null || documento.isBlank()) {
            return false;
        }

        return repositorio.eliminar(documento.trim());
    }
    
    // Listar clientes
    public ArrayList<Cliente> listarClientes() {
        return repositorio.listar();
    }
}

