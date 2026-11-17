import java.time.LocalDate;

/**
 * Lote de Hortaliza (perecedero de 3 a 14 días, requiere cámara fría, 5% merma en pago).
 */
public class Hortaliza extends Lote implements Perecedero, RequiereFrio {
    private int vidaUtilDias;

    public Hortaliza(String codigo, String productor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, int vidaUtilDias) {
        super(codigo, productor, fechaIngreso, pesoRecibido, calidad);
        if (vidaUtilDias < 3 || vidaUtilDias > 14) {
            throw new IllegalArgumentException("La vida útil de una hortaliza debe estar entre 3 y 14 días.");
        }
        this.vidaUtilDias = vidaUtilDias;
    }

    @Override
    public double calcularPago() {
        double tarifa = switch (calidad) {
            case A -> 4.0;
            case B -> 3.0;
            case C -> 2.0;
        };
        double pesoPagable = pesoRecibido * 0.95; // 5% merma de transporte
        return pesoPagable * tarifa;
    }

    @Override
    public String getCategoria() {
        return "Hortaliza";
    }

    @Override
    public LocalDate getFechaVencimiento() {
        return fechaIngreso.plusDays(vidaUtilDias);
    }

    @Override
    public boolean estaVencido(LocalDate fechaActual) {
        return fechaActual.isAfter(getFechaVencimiento());
    }

    public int getVidaUtilDias() { return vidaUtilDias; }
}