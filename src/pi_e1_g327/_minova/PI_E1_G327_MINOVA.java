


package pi_e1_g327._minova;

import vista.VetanaPrincipal;

public class PI_E1_G327_MINOVA {

    public static void main(String[] args) {

        // Iniciar la interfaz grafica de MINOVA
        java.awt.EventQueue.invokeLater(() -> {

            // Crear la ventana principal
            VetanaPrincipal ventana = new VetanaPrincipal();

            // Centrar la ventana en la pantalla
            ventana.setLocationRelativeTo(null);

            // Mostrar la ventana
            ventana.setVisible(true);

        });
    }
}


