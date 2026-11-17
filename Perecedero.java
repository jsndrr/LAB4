import java.time.LocalDate;

/**
 * Contrato para productos que poseen fecha de caducidad.
 */
public interface Perecedero {
    LocalDate getFechaVencimiento();
    boolean estaVencido(LocalDate fechaActual);
}