import java.time.LocalDate;

/**
 * Lote de Queso Fresco (perecedero a 21 días, requiere cámara fría, no acepta calidad C).
 */
public class QuesoFresco extends Lote implements Perecedero, RequiereFrio {
    private double porcentajeGrasa;

    public QuesoFresco(String codigo, String productor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, double porcentajeGrasa) {
        super(codigo, productor, fechaIngreso, pesoRecibido, calidad);
        if (calidad == Calidad.C) {
            throw new IllegalArgumentException("No se acepta queso fresco de calidad C.");
        }
        if (porcentajeGrasa < 10.0 || porcentajeGrasa > 40.0) {
            throw new IllegalArgumentException("El porcentaje de grasa debe estar entre 10% y 40%.");
        }
        this.porcentajeGrasa = porcentajeGrasa;
    }

    @Override
    public double calcularPago() {
        double tarifa = (calidad == Calidad.A) ? 38.0 : 30.0;
        return (pesoRecibido * tarifa) - (pesoRecibido * 1.50);
    }

    @Override
    public String getCategoria() {
        return "Queso fresco";
    }

    @Override
    public LocalDate getFechaVencimiento() {
        return fechaIngreso.plusDays(21);
    }

    @Override
    public boolean estaVencido(LocalDate fechaActual) {
        return fechaActual.isAfter(getFechaVencimiento());
    }

    public double getPorcentajeGrasa() { return porcentajeGrasa; }
}