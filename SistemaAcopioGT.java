import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CC2008 - Laboratorio 4: AcopioGT
 * Controlador principal que gestiona bodegas, lotes, fechas de operación, despachos y reportes.
 */
public class SistemaAcopioGT {
    private LocalDate fechaOperacion;
    private ArrayList<Bodega> bodegas;
    private ArrayList<Lote> historialLotes;
    private Map<String, Double> pagosPorCategoria;
    private Map<String, Double> cobrosPorCategoria;
    private Map<String, Double> perdidasPorCategoria;

    public SistemaAcopioGT(LocalDate fechaInicial) {
        this.fechaOperacion = fechaInicial;
        this.bodegas = new ArrayList<>();
        this.historialLotes = new ArrayList<>();
        this.pagosPorCategoria = new LinkedHashMap<>();
        this.cobrosPorCategoria = new LinkedHashMap<>();
        this.perdidasPorCategoria = new LinkedHashMap<>();

        String[] categorias = {"Café en pergamino", "Café orgánico", "Cardamomo", "Hortaliza", "Queso fresco"};
        for (String cat : categorias) {
            pagosPorCategoria.put(cat, 0.0);
            cobrosPorCategoria.put(cat, 0.0);
            perdidasPorCategoria.put(cat, 0.0);
        }
    }

    private void registrarEnMapa(Map<String, Double> mapa, String categoria, double monto) {
        mapa.put(categoria, mapa.getOrDefault(categoria, 0.0) + monto);
    }

    public Lote buscarLote(String codigo) {
        if (codigo == null) return null;
        for (Lote l : historialLotes) {
            if (l.getCodigo().equalsIgnoreCase(codigo.trim())) return l;
        }
        return null;
    }

    public Bodega buscarBodega(String codigo) {
        if (codigo == null) return null;
        for (Bodega b : bodegas) {
            if (b.getCodigo().equalsIgnoreCase(codigo.trim())) return b;
        }
        return null;
    }

    public boolean registrarBodega(Bodega bodega) {
        if (bodega == null || buscarBodega(bodega.getCodigo()) != null) {
            return false;
        }
        return bodegas.add(bodega);
    }

    /**
     * Carga lotes iniciales sin registrar pagos ni cobros financieros.
     */
    public boolean precargarLoteSinPago(Lote lote, String codigoBodega) {
        if (lote == null || buscarLote(lote.getCodigo()) != null) return false;
        if (lote.getFechaIngreso().isAfter(fechaOperacion)) return false;

        Bodega b = buscarBodega(codigoBodega);
        if (b == null || !b.almacenarLote(lote)) return false;

        historialLotes.add(lote);
        return true;
    }

    /**
     * Calcula el pago al productor y la fecha de vencimiento (si aplica) sin alterar el sistema.
     */
    public String preliquidarLote(Lote lote) {
        String vencimiento = (lote instanceof Perecedero)
                ? ((Perecedero) lote).getFechaVencimiento().toString()
                : "No aplica (Producto no perecedero)";
        return String.format("PRE-LIQUIDACIÓN\nCategoría: %s | Productor: %s | Peso: %.2f kg\nPago al Productor: Q%.2f (Costo/kg: Q%.2f)\nFecha de Vencimiento: %s",
                lote.getCategoria(), lote.getProductor(), lote.getPesoRecibido(),
                lote.calcularPago(), lote.getCostoPorKg(), vencimiento);
    }

    /**
     * Recibe un lote en bodega, lo deja disponible y registra el pago completo al productor.
     */
    public boolean recibirLote(Lote lote, String codigoBodega) {
        if (lote == null || buscarLote(lote.getCodigo()) != null) return false;
        Bodega b = buscarBodega(codigoBodega);
        if (b == null || !b.almacenarLote(lote)) return false;

        historialLotes.add(lote);
        registrarEnMapa(pagosPorCategoria, lote.getCategoria(), lote.calcularPago());
        return true;
    }

    /**
     * Despacha un pedido tomando kilogramos de todas las bodegas en orden de salida.
     * Si no alcanza el inventario total de la categoría, rechaza sin modificar nada.
     * Retorna el monto total cobrado al comprador, o -1.0 si se rechaza.
     */
    public double despacharPedido(String categoria, double kgPedidos) {
        if (kgPedidos <= 0) return -1.0;

        // Recolectar todos los lotes disponibles de la categoría en todas las bodegas
        ArrayList<Lote> candidatos = new ArrayList<>();
        double totalDisponible = 0.0;

        for (Bodega b : bodegas) {
            for (Lote l : b) { // Recorrido polimórfico vía Iterable<Lote>
                if (l.getCategoria().equalsIgnoreCase(categoria.trim()) && l.getEstado() == EstadoLote.DISPONIBLE) {
                    candidatos.add(l);
                    totalDisponible += l.getPesoDisponible();
                }
            }
        }

        // Validación atómica: si no cubre el pedido completo, rechazar sin tocar existencias
        if (totalDisponible + 1e-6 < kgPedidos) {
            return -1.0;
        }

        // Ordenar candidatos globalmente en orden de salida
        Collections.sort(candidatos);

        double restante = kgPedidos;
        double montoCobrado = 0.0;

        for (Lote l : candidatos) {
            if (restante <= 1e-6) break;

            double aTomar = Math.min(restante, l.getPesoDisponible());
            l.descontarPeso(aTomar);
            restante -= aTomar;

            // Precio de venta: costo por kg + 35%
            montoCobrado += aTomar * l.getCostoPorKg() * 1.35;

            // Si el lote llegó a cero, queda AGOTADO y sale de su bodega liberando capacidad
            if (l.getEstado() == EstadoLote.AGOTADO) {
                for (Bodega b : bodegas) {
                    b.removerLote(l);
                }
            }
        }

        registrarEnMapa(cobrosPorCategoria, candidatos.get(0).getCategoria(), montoCobrado);
        return montoCobrado;
    }

    /**
     * Avanza la fecha de operación y descarta automáticamente los lotes perecederos vencidos,
     * registrando la pérdida financiera según los kilogramos restantes.
     */
    public int avanzarFecha(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Solo se puede avanzar la fecha hacia adelante (1 o más días).");
        }
        this.fechaOperacion = this.fechaOperacion.plusDays(dias);
        int descartados = 0;

        for (Bodega b : bodegas) {
            Iterator<Lote> it = b.iterator(); // Recorrido seguro con eliminación
            while (it.hasNext()) {
                Lote l = it.next();
                if (l instanceof Perecedero && ((Perecedero) l).estaVencido(fechaOperacion)) {
                    double perdida = l.getPesoDisponible() * l.getCostoPorKg();
                    registrarEnMapa(perdidasPorCategoria, l.getCategoria(), perdida);
                    l.marcarDescartado();
                    it.remove(); // Sale de la bodega y libera capacidad
                    descartados++;
                }
            }
        }
        return descartados;
    }

    // --- REPORTES GERENCIALES ---

    public void generarReporteExistenciasYBodegas() {
        System.out.println("\n=================================================================================");
        System.out.println("                  EXISTENCIAS POR CATEGORÍA Y CONTENIDO DE BODEGAS               ");
        System.out.println("=================================================================================");
        String[] categorias = {"Café en pergamino", "Café orgánico", "Cardamomo", "Hortaliza", "Queso fresco"};
        for (String cat : categorias) {
            double totalKg = 0.0;
            ArrayList<Lote> lotesCat = new ArrayList<>();
            for (Bodega b : bodegas) {
                for (Lote l : b) {
                    if (l.getCategoria().equals(cat)) {
                        lotesCat.add(l);
                        totalKg += l.getPesoDisponible();
                    }
                }
            }
            Collections.sort(lotesCat);
            System.out.printf(">> Categoría: %-18s | Total Disponible: %8.2f kg (%d lote(s))\n", cat, totalKg, lotesCat.size());
            for (Lote l : lotesCat) {
                System.out.println("   - " + l);
            }
        }

        System.out.println("\n--- CONTENIDO POR BODEGA (EN ORDEN DE SALIDA) ---");
        for (Bodega b : bodegas) {
            System.out.println(b);
            for (Lote l : b) {
                System.out.println("   * " + l);
            }
        }
        System.out.println("=================================================================================\n");
    }

    public void generarReporteProximosAVencer() {
        LocalDate limite = fechaOperacion.plusDays(3);
        ArrayList<Lote> proximos = new ArrayList<>();

        for (Bodega b : bodegas) {
            for (Lote l : b) {
                if (l instanceof Perecedero p) {
                    LocalDate venc = p.getFechaVencimiento();
                    if (!venc.isBefore(fechaOperacion) && !venc.isAfter(limite)) {
                        proximos.add(l);
                    }
                }
            }
        }

        Collections.sort(proximos);
        System.out.println("\n=================================================================================");
        System.out.printf("   LOTES PERECEDEROS QUE VENCEN EN LOS PRÓXIMOS 3 DÍAS (%s a %s)\n", fechaOperacion, limite);
        System.out.println("=================================================================================");
        if (proximos.isEmpty()) {
            System.out.println("No hay lotes próximos a vencer en este rango.");
        } else {
            for (Lote l : proximos) {
                System.out.println(" - " + l);
            }
        }
        System.out.println("=================================================================================\n");
    }

    public void generarReporteFinanciero() {
        System.out.println("\n=================================================================================");
        System.out.println("                     REPORTE FINANCIERO POR CATEGORÍA                            ");
        System.out.println("=================================================================================");
        System.out.printf("%-20s | %15s | %15s | %15s\n", "Categoría", "Pagado Prod.", "Cobrado Desp.", "Pérdidas Venc.");
        System.out.println("---------------------------------------------------------------------------------");

        double sumPagos = 0.0, sumCobros = 0.0, sumPerdidas = 0.0;
        for (String cat : pagosPorCategoria.keySet()) {
            double p = pagosPorCategoria.get(cat);
            double c = cobrosPorCategoria.get(cat);
            double per = perdidasPorCategoria.get(cat);
            sumPagos += p;
            sumCobros += c;
            sumPerdidas += per;
            System.out.printf("%-20s | Q%14.2f | Q%14.2f | Q%14.2f\n", cat, p, c, per);
        }
        System.out.println("---------------------------------------------------------------------------------");
        System.out.printf("%-20s | Q%14.2f | Q%14.2f | Q%14.2f\n", "TOTAL GENERAL", sumPagos, sumCobros, sumPerdidas);
        System.out.println("=================================================================================\n");
    }

    public LocalDate getFechaOperacion() { return fechaOperacion; }
    public ArrayList<Bodega> getBodegas() { return bodegas; }
}