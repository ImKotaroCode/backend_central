package backend_central.backend_central.exception;

// Error de negocio o de integracion con Culqi durante el checkout publico; se traduce a 4xx con mensaje para el usuario.
public class CheckoutException extends RuntimeException {
    public CheckoutException(String message) {
        super(message);
    }

    public CheckoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
