
package pi_e1_g327._minova;
import java.time.LocalDate;
public class PI_E1_G327_MINOVA {

    public static void main(String[] args) {
     System.out.println("=========================================");
        System.out.println("   SISTEMA DE INFORMACION MINOVA (MINOVA)  ");
        System.out.println("   Bits Angus Burger - Proyecto Integrador ");
        System.out.println("=========================================\n");
       
        DatosEntrada registroPrueba = new DatosEntrada(
            "admin_minova",          // nombreUsuario
            "clave1234",             // contraseña
            "Administrador",         // rol
            "1107894562",            // documentoCliente
            "Carlos Perez",          // nombreCliente
            5,                       // numeroMesa
            LocalDate.of(2026, 10, 25), // fechaReserva
            LocalDate.of(2026, 10, 25), // horaReserva (o fecha/hora asignada)
            101,                     // idPedido
            "Hamburguesa Doble Bits",// nombreProducto
            2,                       // cantidad
            25000.0,                 // precioProducto
            15,                      // stock
            "Efectivo",              // metodoPago
            50000.0                  // valorPago
        );

       
        System.out.println("--- DATOS DEL USUARIO Y SESION ---");
        System.out.println("Usuario: " + registroPrueba.getNombreUsuario());
        System.out.println("Rol: " + registroPrueba.getRol());
        
        System.out.println("\n--- DATOS DE CLIENTE Y RESERVA ---");
        System.out.println("Cliente: " + registroPrueba.getNombreCliente() + " (Doc: " + registroPrueba.getDocumentoCliente() + ")");
        System.out.println("Mesa asignada: " + registroPrueba.getNumeroMesa());
        System.out.println("Fecha de reserva: " + registroPrueba.getFechaReserva());

        System.out.println("\n--- DATOS DEL PEDIDO E INVENTARIO ---");
        System.out.println("Producto solicitado: " + registroPrueba.getNombreProducto());
        System.out.println("Cantidad: " + registroPrueba.getCantidad() + " | Precio Unitario: $" + registroPrueba.getPrecioProducto());
        System.out.println("Stock disponible: " + registroPrueba.getStock());
        System.out.println("Metodo de pago: " + registroPrueba.getMetodoPago());
        System.out.println("Valor pagado: $" + registroPrueba.getValorPago());

        System.out.println("\n--- PROBANDO METODO DE LA CLASE ---");
        registroPrueba.mostrarDetallesPedido();
        
        System.out.println("\n¡Ejecucion finalizada con exito!");
    }
    
}
