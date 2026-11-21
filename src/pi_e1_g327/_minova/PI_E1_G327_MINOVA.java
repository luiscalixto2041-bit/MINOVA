package pi_e1_g327._minova;

import javax.swing.SwingUtilities;
import vista.LoginFrame;

public class PI_E1_G327_MINOVA {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            LoginFrame ventana = new LoginFrame();

            ventana.setVisible(true);

        });

    }

}

