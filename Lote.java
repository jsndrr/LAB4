import java.time.LocalDate;

/**
 * CC2008 - Laboratorio 4: AcopioGT
 * Clase base abstracta para todos los lotes de productos.
 * Implementa Comparable<Lote> para establecer el orden estricto de salida.
 */
public abstract class Lote implements Comparable<Lote> {
    protected String codigo;
    protected String productor;
    protected LocalDate fechaIngreso;
    protected double pesoRecibido;
    protected double pesoDisponible;
    protected Calidad calidad;
    protected EstadoLote estado;

    public Lote(String codigo, String productor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del lote no puede estar vacío.");
        }
        if (productor == null || productor.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del productor no puede estar vacío.");
        }
        if (fechaIngreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria.");
        }
        if (pesoRecibido <= 0) {
            throw new IllegalArgumentException("El peso recibido debe ser mayor a cero.");
        }
        if (calidad == null) {
            throw new IllegalArgumentException("La calidad del lote es obligatoria.");
        }

        this.codigo = codigo.trim();
        this.productor = productor.trim();
        this.fechaIngreso = fechaIngreso;
        this.pesoRecibido = pesoRecibido;
        this.pesoDisponible = pesoRecibido;
        this.calidad = calidad;
        this.estado = EstadoLote.DISPONIBLE;
    }

    public abstract double calcularPago();
    public abstract String getCategoria();

    public double getCostoPorKg() {
        return calcularPago() / pesoRecibido;
    }

    /**
     * Determina polimórficamente si el lote exige cámara fría verificando el contrato RequiereFrio.
     */
    public boolean requiereCamaraFria() {
        return this instanceof RequiereFrio;
    }

    public void descontarPeso(double kg) {
        if (kg <= 0 || kg > pesoDisponible + 1e-6) {
            throw new IllegalArgumentException("Cantidad inválida a descontar del lote " + codigo);
        }
        this.pesoDisponible -= kg;
        if (this.pesoDisponible <= 1e-6) {
            this.pesoDisponible = 0.0;
            this.estado = EstadoLote.AGOTADO;
        }
    }

    public void marcarDescartado() {
        this.estado = EstadoLote.DESCARTADO;
    }

    /**
     * Orden de salida:
     * 1. Perecederos antes que no perecederos.
     * 2. Entre perecederos: vence antes. Entre no perecederos: ingresó antes.
     * 3. Mejor calidad (A < B < C según orden natural del enum).
     * 4. Código menor en orden alfabético.
     */
    @Override
    public int compareTo(Lote otro) {
        boolean estePerecedero = this instanceof Perecedero;
        boolean otroPerecedero = otro instanceof Perecedero;

        if (estePerecedero && !otroPerecedero) return -1;
        if (!estePerecedero && otroPerecedero) return 1;

        int compFecha;
        if (estePerecedero) {
            LocalDate vencEste = ((Perecedero) this).getFechaVencimiento();
            LocalDate vencOtro = ((Perecedero) otro).getFechaVencimiento();
            compFecha = vencEste.compareTo(vencOtro);
        } else {
            compFecha = this.fechaIngreso.compareTo(otro.fechaIngreso);
        }
        if (compFecha != 0) return compFecha;

        int compCalidad = this.calidad.compareTo(otro.calidad);
        if (compCalidad != 0) return compCalidad;

        return this.codigo.compareToIgnoreCase(otro.codigo);
    }

    public String getCodigo() { return codigo; }
    public String getProductor() { return productor; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public double getPesoRecibido() { return pesoRecibido; }
    public double getPesoDisponible() { return pesoDisponible; }
    public Calidad getCalidad() { return calidad; }
    public EstadoLote getEstado() { return estado; }

    @Override
    public String toString() {
        String infoVenc = (this instanceof Perecedero)
                ? " | Vence: " + ((Perecedero) this).getFechaVencimiento()
                : " | No perecedero";
        return String.format("[%s] Cód: %-8s | Prod: %-14s | Ing: %s%s | Cal: %s | Disp: %6.2f/%6.2f kg | Costo/kg: Q%6.2f | Estado: %s",
                getCategoria(), codigo, productor, fechaIngreso, infoVenc, calidad, pesoDisponible, pesoRecibido, getCostoPorKg(), estado);
    }
}