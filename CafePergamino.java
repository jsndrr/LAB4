import java.time.LocalDate;

/**
 * Lote de Café en Pergamino (no perecedero, almacenamiento seco).
 */
public class CafePergamino extends Lote {
    protected double humedad;

    public CafePergamino(String codigo, String productor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, double humedad) {
        super(codigo, productor, fechaIngreso, pesoRecibido, calidad);
        if (humedad < 8.0 || humedad > 14.0) {
            throw new IllegalArgumentException("La humedad del café debe estar entre 8% y 14%.");
        }
        this.humedad = humedad;
    }

    @Override
    public double calcularPago() {
        double tarifaBase = switch (calidad) {
            case A -> 12.0;
            case B -> 9.0;
            case C -> 6.0;
        };
        double pagoBase = pesoRecibido * tarifaBase;

        if (humedad > 12.0) {
            int puntosExceso = (int) Math.ceil(humedad - 12.0);
            double porcentajeDescuento = puntosExceso * 0.03;
            pagoBase -= pagoBase * porcentajeDescuento;
        }
        return pagoBase;
    }

    @Override
    public String getCategoria() {
        return "Café en pergamino";
    }

    public double getHumedad() { return humedad; }
}