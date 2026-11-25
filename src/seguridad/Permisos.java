/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package seguridad;

import java.util.Locale;
import modelo.Usuario;

public class Permisos {

    public static boolean puedeAcceder(
            Usuario usuario, String modulo) {

        if (usuario == null || usuario.getRol() == null
                || modulo == null) {
            return false;
        }

        String rol = usuario.getRol()
                .trim().toUpperCase(Locale.ROOT);

        switch (rol) {

            case "ADMINISTRADOR":
                return true;

            case "CAJERO":
                return switch (modulo) {
                    case "INICIO", "CLIENTES", "PEDIDOS",
                         "PRODUCTOS", "MESAS", "PAGOS" -> true;
                    default -> false;
                };

            case "MESERO":
                return switch (modulo) {
                    case "INICIO", "CLIENTES", "PEDIDOS",
                         "PRODUCTOS", "RESERVAS", "MESAS" -> true;
                    default -> false;
                };

            case "INVENTARIO":
                return switch (modulo) {
                    case "INICIO", "PRODUCTOS",
                         "INVENTARIO" -> true;
                    default -> false;
                };

            default:
                return false;
        }
    }

    public static boolean puedeEditar(
            Usuario usuario, String modulo) {

        if (!puedeAcceder(usuario, modulo)) {
            return false;
        }

        String rol = usuario.getRol()
                .trim().toUpperCase(Locale.ROOT);

        if (rol.equals("ADMINISTRADOR")) {
            return true;
        }

        if (modulo.equals("PRODUCTOS")) {
            return rol.equals("INVENTARIO");
        }

        if (modulo.equals("MESAS")) {
            return rol.equals("MESERO");
        }

        return !modulo.equals("INICIO");
    }
}
