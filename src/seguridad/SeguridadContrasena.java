/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package seguridad;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SeguridadContrasena {

    private static final int ITERACIONES = 600000;
    private static final int LONGITUD = 256;

    public static String generarHash(String contrasena) {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        byte[] hash = calcularHash(contrasena, salt);

        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(
            String contrasena, String hashGuardado) {

        if (hashGuardado == null) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split(":");

            if (partes.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder()
                    .decode(partes[0]);

            byte[] hashOriginal = Base64.getDecoder()
                    .decode(partes[1]);

            byte[] hashNuevo = calcularHash(contrasena, salt);

            return MessageDigest.isEqual(
                    hashOriginal, hashNuevo);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] calcularHash(
            String contrasena, byte[] salt) {

        PBEKeySpec spec = new PBEKeySpec(
                contrasena.toCharArray(),
                salt,
                ITERACIONES,
                LONGITUD
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error al proteger la contrasena", e);
        } finally {
            spec.clearPassword();
        }
    }
}

