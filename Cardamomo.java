import java.time.LocalDate;

/**
 * Lote de Cardamomo (perecedero a 180 días, almacenamiento seco).
 */
public class Cardamomo extends Lote implements Perecedero {
    private double porcentajeGranoVerde;

    public Cardamomo(String codigo, String productor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, double porcentajeGranoVerde) {
        super(codigo, productor, fechaIngreso, pesoRecibido, calidad);
        if (porcentajeGranoVerde < 0.0 || porcentajeGranoVerde > 100.0) {
            throw new IllegalArgumentException("El porcentaje de grano verde debe estar entre 0% y 100%.");
        }
        this.porcentajeGranoVerde = porcentajeGranoVerde;
    }

    @Override
    public double calcularPago() {
        double tarifa = switch (calidad) {
            case A -> 60.0;
            case B -> 48.0;
            case C -> 35.0;
        };
        double pago = pesoRecibido * tarifa;
        if (porcentajeGranoVerde >= 85.0) {
            pago += pago * 0.10;
        }
        return pago;
    }

    @Override
    public String getCategoria() {
        return "Cardamomo";
    }

    @Override
    public LocalDate getFechaVencimiento() {
        return fechaIngreso.plusDays(180);
    }

    @Override
    public boolean estaVencido(LocalDate fechaActual) {
        return fechaActual.isAfter(getFechaVencimiento());
    }

    public double getPorcentajeGranoVerde() { return porcentajeGranoVerde; }
}