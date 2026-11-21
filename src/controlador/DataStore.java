package controlador;

import java.util.ArrayList;
import java.util.List;
import modelo.Usuario;

public class DataStore {
    private static DataStore instancia;
    private List<Usuario> usuarios;
    private DataStore() {

        usuarios = new ArrayList<>();
        usuarios.add(new Usuario("admin", "1234", "Administrador"));
        usuarios.add(new Usuario("mesero", "1", "Mesero"));
    }

    public static DataStore get() {
        if (instancia == null) {
            instancia = new DataStore();
        }
        return instancia;
    }

    // validar inicio de sesion
    public Usuario login(String nombreUsuario, String contraseña) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equalsIgnoreCase(nombreUsuario)
                    && u.getContraseña().equals(contraseña)) {

                return u;
            }
        }
        return null;
    }

    // registrar un usuario nuevo
    public void registrarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    // comprobar si el usuario ya existe
    public boolean existeUsuario(String nombreUsuario) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equalsIgnoreCase(nombreUsuario)) {
                return true;
            }
        }
        return false;
    }
}

