import java.time.LocalDate;

/**
 * Lote de Café Orgánico: especialización de Café en Pergamino con certificación vigente.
 */
public class CafeOrganico extends CafePergamino {
    private String entidadCertificadora;
    private LocalDate fechaValidezSello;

    public CafeOrganico(String codigo, String productor, LocalDate fechaIngreso, double pesoRecibido,
                        Calidad calidad, double humedad, String entidadCertificadora, LocalDate fechaValidezSello) {
        super(codigo, productor, fechaIngreso, pesoRecibido, calidad, humedad);
        if (entidadCertificadora == null || entidadCertificadora.trim().isEmpty()) {
            throw new IllegalArgumentException("La entidad certificadora es obligatoria.");
        }
        if (fechaValidezSello == null || fechaValidezSello.isBefore(fechaIngreso)) {
            throw new IllegalArgumentException("El sello orgánico está vencido en la fecha de ingreso.");
        }
        this.entidadCertificadora = entidadCertificadora.trim();
        this.fechaValidezSello = fechaValidezSello;
    }

    @Override
    public double calcularPago() {
        return super.calcularPago() + (pesoRecibido * 0.75);
    }

    @Override
    public String getCategoria() {
        return "Café orgánico";
    }

    public String getEntidadCertificadora() { return entidadCertificadora; }
    public LocalDate getFechaValidezSello() { return fechaValidezSello; }
}