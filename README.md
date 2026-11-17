 Requisitos del Sistema
Java Development Kit (JDK): Versión 17 o superior.
Sistema Operativo: Windows, Linux o macOS con terminal/consola configurada.

 Estructura del Proyecto

 Calidad.java                    # Enumeración de calidades (A, B, C)
EstadoLote.java                 # Enumeración de estados (DISPONIBLE, AGOTADO, DESCARTADO)
 Perecedero.java                 # Interfaz para productos con fecha de vencimiento
 RequiereFrio.java               # Interfaz de capacidad para almacenamiento refrigerado
Lote.java                       # Clase base abstracta (implementa Comparable<Lote>)
CafePergamino.java              # Subclase de Lote (descuento por humedad > 12%)
CafeOrganico.java               # Subclase de CafePergamino (bono por certificación)
Cardamomo.java                  # Subclase perecedera (bono por grano verde >= 85%)
 Hortaliza.java                  # Subclase perecedera y refrigerada (5% merma)
QuesoFresco.java                # Subclase perecedera y refrigerada (costo empaque)
Bodega.java                     # Gestión de almacenamiento (implementa Iterable<Lote>)
SistemaAcopioGT.java            # Controlador de dominio, despachos, fechas y reportes
Main.java                       # Driver program con menú interactivo y precarga inicial
README.md                       # Documentación de compilación y ejecución
 Instrucciones de Compilación y Ejecución

Clonar el repositorio:
git clone https://github.com/jsndrr/LAB4


Compilar todos los archivos fuente Java:
javac *.java

Ejecutar la aplicación:
java Main
