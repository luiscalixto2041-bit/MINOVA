/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionMySQL {

    private static final String URL =
            "jdbc:mysql://localhost:3306/minova_v2"
            + "?serverTimezone=America/Bogota";

    private static final String USUARIO = "minova_app";

    public static Connection conectar() throws SQLException {

        String contrasena = System.getenv("MINOVA_DB_PASSWORD");

        if (contrasena == null || contrasena.isBlank()) {
            throw new SQLException(
                "Falta configurar MINOVA_DB_PASSWORD"
            );
        }

        return DriverManager.getConnection(
                URL, USUARIO, contrasena
        );
    }
}
