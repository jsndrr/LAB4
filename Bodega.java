import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * CC2008 - Laboratorio 4: AcopioGT
 * Representa una bodega física (seca o cámara fría).
 * Implementa Iterable<Lote> para entregar sus lotes siempre en orden de salida.
 */
public class Bodega implements Iterable<Lote> {
    private String codigo;
    private double capacidadMaxima;
    private boolean esCamaraFria;
    private ArrayList<Lote> lotes;

    public Bodega(String codigo, double capacidadMaxima, boolean esCamaraFria) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la bodega no puede estar vacío.");
        }
        if (capacidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a cero.");
        }
        this.codigo = codigo.trim();
        this.capacidadMaxima = capacidadMaxima;
        this.esCamaraFria = esCamaraFria;
        this.lotes = new ArrayList<>();
    }

    public double calcularPesoAlmacenado() {
        double total = 0.0;
        for (Lote l : lotes) {
            total += l.getPesoDisponible();
        }
        return total;
    }

    public double calcularCapacidadDisponible() {
        return capacidadMaxima - calcularPesoAlmacenado();
    }

    /**
     * Valida compatibilidad térmica y capacidad disponible antes de recibir un lote.
     */
    public boolean puedeRecibir(Lote lote) {
        if (lote == null) return false;
        // Compatibilidad estricta: frío solo en cámara fría, seco solo en bodega seca
        if (lote.requiereCamaraFria() != this.esCamaraFria) {
            return false;
        }
        return (calcularPesoAlmacenado() + lote.getPesoRecibido()) <= (capacidadMaxima + 1e-6);
    }

    public boolean almacenarLote(Lote lote) {
        if (!puedeRecibir(lote)) {
            return false;
        }
        lotes.add(lote);
        Collections.sort(lotes); // Mantiene la bodega ordenada según el criterio de salida
        return true;
    }

    public boolean removerLote(Lote lote) {
        return lotes.remove(lote);
    }

    /**
     * Retorna un iterador sobre los lotes ordenados por prioridad de salida.
     * Soporta la operación remove() para descartar lotes vencidos o agotados en recorrido.
     */
    @Override
    public Iterator<Lote> iterator() {
        Collections.sort(lotes);
        return lotes.iterator();
    }

    public String getCodigo() { return codigo; }
    public double getCapacidadMaxima() { return capacidadMaxima; }
    public boolean isEsCamaraFria() { return esCamaraFria; }

    @Override
    public String toString() {
        return String.format("Bodega: %-8s | Tipo: %-12s | Ocupado: %7.2f / %7.2f kg (Disp: %7.2f kg)",
                codigo, (esCamaraFria ? "Cámara Fría" : "Bodega Seca"),
                calcularPesoAlmacenado(), capacidadMaxima, calcularCapacidadDisponible());
    }
}