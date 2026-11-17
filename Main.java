import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * CC2008 - Laboratorio 4: AcopioGT
 * Driver program con menú interactivo de consola y precarga inicial.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static SistemaAcopioGT sistema;

    public static void main(String[] args) {
        LocalDate fechaInicial = LocalDate.of(2026, 3, 10);
        sistema = new SistemaAcopioGT(fechaInicial);
        precargarDatosIniciales(fechaInicial);

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1 -> preliquidarYRecibirLoteMenu();
                case 2 -> registrarBodegaMenu();
                case 3 -> despacharPedidoMenu();
                case 4 -> avanzarFechaMenu();
                case 5 -> consultarBodegasMenu();
                case 6 -> menuReportes();
                case 7 -> {
                    salir = true;
                    System.out.println("Cerrando sistema AcopioGT... ¡Hasta pronto!");
                }
                default -> System.out.println("Opción inválida. Seleccione entre 1 y 7.");
            }
        }
        scanner.close();
    }

    /**
     * Precarga 3 bodegas y 10 lotes (2 por categoría) sin registrar pagos ni cobros.
     */
    private static void precargarDatosIniciales(LocalDate hoy) {
        // 3 Bodegas (2 secas, 1 cámara fría)
        sistema.registrarBodega(new Bodega("BOD-S1", 2000.0, false));
        sistema.registrarBodega(new Bodega("BOD-S2", 1500.0, false));
        sistema.registrarBodega(new Bodega("BOD-F1", 1000.0, true));

        // 1. Café en pergamino (incluye humedad > 12%: 13.4% -> 2 puntos de exceso -> -6%)
        sistema.precargarLoteSinPago(new CafePergamino("CAF-01", "Finca El Sol", hoy.minusDays(5), 500.0, Calidad.A, 13.4), "BOD-S1");
        sistema.precargarLoteSinPago(new CafePergamino("CAF-02", "Cooperativa Sur", hoy.minusDays(2), 300.0, Calidad.B, 11.0), "BOD-S2");

        // 2. Café orgánico (sello válido)
        sistema.precargarLoteSinPago(new CafeOrganico("ORG-01", "Asociación Verde", hoy.minusDays(4), 400.0, Calidad.A, 12.5, "Mayacert", hoy.plusMonths(6)), "BOD-S1");
        sistema.precargarLoteSinPago(new CafeOrganico("ORG-02", "EcoCafé GT", hoy.minusDays(1), 250.0, Calidad.B, 10.5, "BioLatina", hoy.plusMonths(4)), "BOD-S2");

        // 3. Cardamomo (incluye bono grano verde >= 85%)
        sistema.precargarLoteSinPago(new Cardamomo("CAR-01", "Cobán Export", hoy.minusDays(10), 80.0, Calidad.B, 90.0), "BOD-S1");
        sistema.precargarLoteSinPago(new Cardamomo("CAR-02", "Alta Verapaz Agro", hoy.minusDays(3), 120.0, Calidad.A, 80.0), "BOD-S2");

        // 4. Hortaliza (Cámara fría. Empate exacto de vencimiento: hoy + 2 días, resuelto por Calidad A antes que B)
        sistema.precargarLoteSinPago(new Hortaliza("HOR-01", "Granja Chimal", hoy.minusDays(3), 150.0, Calidad.B, 5), "BOD-F1"); // Vence hoy + 2
        sistema.precargarLoteSinPago(new Hortaliza("HOR-02", "Huertos Almolonga", hoy.minusDays(2), 200.0, Calidad.A, 4), "BOD-F1"); // Vence hoy + 2 (Sale antes por Calidad A)

        // 5. Queso fresco (Cámara fría)
        sistema.precargarLoteSinPago(new QuesoFresco("QUE-01", "Lácteos Oriente", hoy.minusDays(5), 40.0, Calidad.A, 25.0), "BOD-F1");
        sistema.precargarLoteSinPago(new QuesoFresco("QUE-02", "Finca La Pradera", hoy.minusDays(19), 60.0, Calidad.B, 18.0), "BOD-F1"); // Vence en 2 días (próximo a vencer)

        System.out.println("=== SISTEMA DE ACOPIO Y DESPACHO - ACOPIOGT ===");
        System.out.println("Fecha de operación inicial: " + hoy);
        System.out.println(">> Precarga completada: 3 bodegas y 10 lotes disponibles (sin pagos ni cobros registrados).\n");
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ PRINCIPAL (Fecha Operación: " + sistema.getFechaOperacion() + ") ---");
        System.out.println("1. Pre-liquidar y recibir lote");
        System.out.println("2. Registrar nueva bodega");
        System.out.println("3. Registrar pedido y despachar");
        System.out.println("4. Avanzar fecha de operación");
        System.out.println("5. Consultar bodegas y lotes en orden de salida");
        System.out.println("6. Reportes gerenciales");
        System.out.println("7. Salir");
    }

    private static void preliquidarYRecibirLoteMenu() {
        System.out.println("\n--- PRE-LIQUIDACIÓN Y RECEPCIÓN DE LOTE ---");
        System.out.println("Categorías: 1. Café pergamino | 2. Café orgánico | 3. Cardamomo | 4. Hortaliza | 5. Queso fresco");
        int cat = leerEntero("Seleccione categoría (1-5): ");
        if (cat < 1 || cat > 5) {
            System.out.println("Categoría inválida.");
            return;
        }

        String codigo = leerTexto("Código único del lote: ");
        if (sistema.buscarLote(codigo) != null) {
            System.out.println("Error: Ya existe un lote con el código '" + codigo + "'.");
            return;
        }

        String productor = leerTexto("Nombre del productor: ");
        double peso = leerDouble("Peso recibido en kg (> 0): ");
        Calidad calidad = leerCalidad();

        try {
            LocalDate fechaIng = sistema.getFechaOperacion();
            Lote nuevoLote = switch (cat) {
                case 1 -> {
                    double hum = leerDouble("Humedad del grano (8 - 14%): ");
                    yield new CafePergamino(codigo, productor, fechaIng, peso, calidad, hum);
                }
                case 2 -> {
                    double hum = leerDouble("Humedad del grano (8 - 14%): ");
                    String ent = leerTexto("Entidad certificadora: ");
                    LocalDate val = leerFecha("Fecha validez del sello (YYYY-MM-DD): ");
                    yield new CafeOrganico(codigo, productor, fechaIng, peso, calidad, hum, ent, val);
                }
                case 3 -> {
                    double gv = leerDouble("Porcentaje de grano verde (0 - 100%): ");
                    yield new Cardamomo(codigo, productor, fechaIng, peso, calidad, gv);
                }
                case 4 -> {
                    int vida = leerEntero("Vida útil en días (3 - 14): ");
                    yield new Hortaliza(codigo, productor, fechaIng, peso, calidad, vida);
                }
                case 5 -> {
                    double grasa = leerDouble("Porcentaje de grasa (10 - 40%): ");
                    yield new QuesoFresco(codigo, productor, fechaIng, peso, calidad, grasa);
                }
                default -> null;
            };

            System.out.println("\n" + sistema.preliquidarLote(nuevoLote));
            String resp = leerTexto("¿El productor acepta la pre-liquidación y desea almacenar el lote? (s/n): ");
            if (resp.equalsIgnoreCase("s")) {
                consultarBodegasMenu();
                String codBod = leerTexto("Ingrese el código de la bodega destino: ");
                if (sistema.recibirLote(nuevoLote, codBod)) {
                    System.out.printf("¡Lote recibido en %s! Pago registrado por Q%.2f\n", codBod, nuevoLote.calcularPago());
                } else {
                    System.out.println("Error: Bodega inexistente, incompatible térmicamente o sin capacidad suficiente. No se registró el lote.");
                }
            } else {
                System.out.println("Pre-liquidación finalizada sin registro.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Operación rechazada: " + e.getMessage());
        }
    }

    private static void registrarBodegaMenu() {
        System.out.println("\n--- REGISTRO DE BODEGA ---");
        String cod = leerTexto("Código único de bodega: ");
        double cap = leerDouble("Capacidad máxima en kg (> 0): ");
        String fria = leerTexto("¿Es cámara fría? (s/n): ");

        try {
            Bodega b = new Bodega(cod, cap, fria.equalsIgnoreCase("s"));
            if (sistema.registrarBodega(b)) {
                System.out.println("¡Bodega registrada exitosamente!");
            } else {
                System.out.println("Error: Ya existe una bodega con ese código.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void despacharPedidoMenu() {
        System.out.println("\n--- DESPACHO DE PEDIDO ---");
        System.out.println("Categorías: 1. Café en pergamino | 2. Café orgánico | 3. Cardamomo | 4. Hortaliza | 5. Queso fresco");
        int op = leerEntero("Seleccione categoría (1-5): ");
        String categoria = switch (op) {
            case 1 -> "Café en pergamino";
            case 2 -> "Café orgánico";
            case 3 -> "Cardamomo";
            case 4 -> "Hortaliza";
            case 5 -> "Queso fresco";
            default -> null;
        };

        if (categoria == null) {
            System.out.println("Categoría inválida.");
            return;
        }

        double kg = leerDouble("Kilogramos solicitados (> 0): ");
        if (kg <= 0) {
            System.out.println("Error: La cantidad debe ser mayor a cero.");
            return;
        }

        double cobro = sistema.despacharPedido(categoria, kg);
        if (cobro >= 0) {
            System.out.printf("¡Despacho completado exitosamente! Total cobrado al comprador: Q%.2f\n", cobro);
        } else {
            System.out.println("Pedido rechazado: Existencias insuficientes para cubrir el pedido completo. No se descontó producto.");
        }
    }

    private static void avanzarFechaMenu() {
        int dias = leerEntero("¿Cuántos días desea avanzar la fecha de operación? (>= 1): ");
        try {
            int descartados = sistema.avanzarFecha(dias);
            System.out.println("Nueva fecha de operación: " + sistema.getFechaOperacion());
            System.out.println("Lotes perecederos vencidos y descartados: " + descartados);
        } catch (IllegalArgumentException e) {
            System.out.println("Operación rechazada: " + e.getMessage());
        }
    }

    private static void consultarBodegasMenu() {
        System.out.println("\n--- BODEGAS Y LOTES EN ORDEN DE SALIDA ---");
        for (Bodega b : sistema.getBodegas()) {
            System.out.println(b);
            for (Lote l : b) {
                System.out.println("   * " + l);
            }
        }
    }

    private static void menuReportes() {
        System.out.println("\n--- REPORTES GERENCIALES ---");
        System.out.println("1. Existencias por categoría y contenido de bodegas");
        System.out.println("2. Lotes perecederos próximos a vencer (próximos 3 días)");
        System.out.println("3. Reporte financiero (Pagos, Cobros y Pérdidas por categoría)");
        int op = leerEntero("Seleccione reporte: ");

        switch (op) {
            case 1 -> sistema.generarReporteExistenciasYBodegas();
            case 2 -> sistema.generarReporteProximosAVencer();
            case 3 -> sistema.generarReporteFinanciero();
            default -> System.out.println("Opción inválida.");
        }
    }

    // --- UTILIDADES DE LECTURA ---

    private static Calidad leerCalidad() {
        while (true) {
            String c = leerTexto("Calidad (A, B o C): ").toUpperCase();
            try {
                return Calidad.valueOf(c);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Ingrese A, B o C.");
            }
        }
    }

    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            String f = leerTexto(mensaje);
            try {
                return LocalDate.parse(f);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato inválido. Use YYYY-MM-DD (ej. 2026-09-15).");
            }
        }
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                int val = scanner.nextInt();
                scanner.nextLine();
                return val;
            } catch (InputMismatchException e) {
                System.out.println("Error: Ingrese un número entero válido.");
                scanner.nextLine();
            }
        }
    }

    private static double leerDouble(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                double val = scanner.nextDouble();
                scanner.nextLine();
                return val;
            } catch (InputMismatchException e) {
                System.out.println("Error: Ingrese un número decimal válido.");
                scanner.nextLine();
            }
        }
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }
}