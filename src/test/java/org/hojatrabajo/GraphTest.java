package org.hojatrabajo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GraphTest – Pruebas unitarias con JUnit 5.
 *
 * Cubre: addCity, addEdge, removeEdge, Floyd-Warshall y centro del grafo.
 *
 * Para compilar y ejecutar (con JUnit 5 en el classpath):
 *   javac -cp junit-platform-console-standalone.jar:. *.java
 *   java  -cp junit-platform-console-standalone.jar:. \
 *         org.junit.platform.console.ConsoleLauncher --scan-classpath
 *
 * CC2003 – Algoritmos y Estructura de Datos
 * Hoja de Trabajo No. 10
 */
class GraphTest {

    private Graph graph;
    private Floyd floyd;

    @BeforeEach
    void setUp() {
        graph = new Graph();
    }

    // ══════════════════════════════════════════
    // Pruebas de Graph
    // ══════════════════════════════════════════

    @Test
    @DisplayName("Agregar una ciudad nueva la registra correctamente")
    void testAddCity() {
        graph.addCity("Guatemala");
        assertTrue(graph.hasCity("Guatemala"),
                "La ciudad debería existir después de agregarla");
        assertEquals(1, graph.size(), "El grafo debería tener 1 ciudad");
    }

    @Test
    @DisplayName("Agregar la misma ciudad dos veces no duplica nodos")
    void testAddDuplicateCityNoDuplicate() {
        graph.addCity("Mixco");
        graph.addCity("Mixco");
        assertEquals(1, graph.size(), "No debe haber duplicados");
    }

    @Test
    @DisplayName("addEdgeWithCities crea ciudades y arco en un solo paso")
    void testAddEdgeWithCities() {
        graph.addEdgeWithCities("Mixco", "Antigua", 30);
        assertEquals(2, graph.size());
        assertTrue(graph.hasCity("Mixco"));
        assertTrue(graph.hasCity("Antigua"));
        assertEquals(30, graph.getEdgeWeight("Mixco", "Antigua"), 0.001);
    }

    @Test
    @DisplayName("addEdge falla con ciudad inexistente y devuelve false")
    void testAddEdgeUnknownCity() {
        graph.addCity("Mixco");
        assertFalse(graph.addEdge("Mixco", "CiudadFantasma", 50));
    }

    @Test
    @DisplayName("Arco en dirección contraria no existe por defecto")
    void testDirectedEdgeNotSymmetric() {
        graph.addEdgeWithCities("Guatemala", "Escuintla", 60);
        assertEquals(Graph.INF, graph.getEdgeWeight("Escuintla", "Guatemala"), 0.001,
                "El grafo es dirigido; no debe haber arco inverso automático");
    }

    @Test
    @DisplayName("removeEdge elimina el arco (pone INF)")
    void testRemoveEdge() {
        graph.addEdgeWithCities("Antigua", "Escuintla", 55);
        boolean removed = graph.removeEdge("Antigua", "Escuintla");
        assertTrue(removed);
        assertEquals(Graph.INF, graph.getEdgeWeight("Antigua", "Escuintla"), 0.001);
    }

    @Test
    @DisplayName("removeEdge devuelve false si alguna ciudad no existe")
    void testRemoveEdgeUnknownCity() {
        graph.addCity("Antigua");
        assertFalse(graph.removeEdge("Antigua", "NoExiste"));
    }

    @Test
    @DisplayName("La diagonal de la matriz siempre es 0")
    void testMatrixDiagonalZero() {
        graph.addEdgeWithCities("A", "B", 10);
        graph.addEdgeWithCities("B", "C", 20);
        double[][] m = graph.getMatrix();
        for (int i = 0; i < graph.size(); i++) {
            assertEquals(0.0, m[i][i], 0.001,
                    "La distancia de un nodo a sí mismo debe ser 0");
        }
    }

    // ══════════════════════════════════════════
    // Pruebas de Floyd-Warshall
    // ══════════════════════════════════════════

    @Test
    @DisplayName("Floyd: distancia directa entre dos ciudades conectadas")
    void testFloydDirectDistance() {
        graph.addEdgeWithCities("Mixco", "Antigua", 30);
        floyd = new Floyd(graph);
        floyd.compute();
        assertEquals(30, floyd.getDistance("Mixco", "Antigua"), 0.001);
    }

    @Test
    @DisplayName("Floyd: distancia transitiva a través de nodo intermedio")
    void testFloydTransitiveDistance() {
        graph.addEdgeWithCities("Mixco", "Antigua", 30);
        graph.addEdgeWithCities("Antigua", "Escuintla", 25);
        floyd = new Floyd(graph);
        floyd.compute();
        assertEquals(55, floyd.getDistance("Mixco", "Escuintla"), 0.001);
    }

    @Test
    @DisplayName("Floyd: elige ruta más corta cuando hay dos caminos")
    void testFloydChoosesShorterPath() {
        graph.addEdgeWithCities("Guatemala", "Escuintla", 60); // directo
        graph.addEdgeWithCities("Guatemala", "Antigua", 45);
        graph.addEdgeWithCities("Antigua", "Escuintla", 55);   // 45+55=100, más largo
        floyd = new Floyd(graph);
        floyd.compute();
        assertEquals(60, floyd.getDistance("Guatemala", "Escuintla"), 0.001,
                "Debería elegir la ruta directa de 60 KM");
    }

    @Test
    @DisplayName("Floyd: retorna INF cuando no hay camino")
    void testFloydNoPath() {
        graph.addEdgeWithCities("Mixco", "Antigua", 30);
        floyd = new Floyd(graph);
        floyd.compute();
        assertEquals(Graph.INF, floyd.getDistance("Antigua", "Mixco"), 0.001,
                "Grafo dirigido: no debe existir camino inverso");
    }

    @Test
    @DisplayName("Floyd: ciudad desconocida retorna INF")
    void testFloydUnknownCity() {
        graph.addEdgeWithCities("A", "B", 10);
        floyd = new Floyd(graph);
        floyd.compute();
        assertEquals(Graph.INF, floyd.getDistance("X", "B"), 0.001);
    }

    @Test
    @DisplayName("Floyd: reconstrucción del camino correcto (3 nodos)")
    void testFloydPathReconstruction() {
        graph.addEdgeWithCities("Mixco", "Antigua", 30);
        graph.addEdgeWithCities("Antigua", "Escuintla", 25);
        floyd = new Floyd(graph);
        floyd.compute();

        List<String> path = floyd.getPath("Mixco", "Escuintla");
        assertEquals(3, path.size(), "El camino debe pasar por 3 ciudades");
        assertEquals("Mixco",     path.get(0));
        assertEquals("Antigua",   path.get(1));
        assertEquals("Escuintla", path.get(2));
    }

    @Test
    @DisplayName("Floyd: camino vacío cuando no existe ruta")
    void testFloydEmptyPathWhenNoRoute() {
        graph.addEdgeWithCities("A", "B", 5);
        floyd = new Floyd(graph);
        floyd.compute();
        List<String> path = floyd.getPath("B", "A");
        assertTrue(path.isEmpty(), "No debe existir camino de B a A en grafo dirigido");
    }

    @ParameterizedTest
    @DisplayName("Floyd: self-distance siempre es 0")
    @CsvSource({"Guatemala", "Mixco", "Antigua"})
    void testFloydSelfDistance(String city) {
        graph.addEdgeWithCities("Guatemala", "Mixco", 15);
        graph.addEdgeWithCities("Mixco", "Antigua", 30);
        floyd = new Floyd(graph);
        floyd.compute();
        assertEquals(0.0, floyd.getDistance(city, city), 0.001);
    }


    @Test
    @DisplayName("Centro: grafo vacío devuelve null")
    void testCenterEmptyGraph() {
        floyd = new Floyd(graph);
        floyd.compute();
        assertNull(floyd.getCenter());
    }

    @Test
    @DisplayName("Centro: grafo de un solo nodo")
    void testCenterSingleNode() {
        graph.addCity("Guatemala");
        floyd = new Floyd(graph);
        floyd.compute();
        assertEquals("Guatemala", floyd.getCenter());
    }

    @Test
    @DisplayName("Excentricidades: tamaño igual al número de ciudades")
    void testEccentricitiesSize() {
        graph.addEdgeWithCities("X", "Y", 10);
        graph.addEdgeWithCities("Y", "Z", 20);
        floyd = new Floyd(graph);
        floyd.compute();
        double[] eccs = floyd.getEccentricities();
        assertEquals(graph.size(), eccs.length);
    }

    // ══════════════════════════════════════════
    // Prueba de integración – escenario completo
    // ══════════════════════════════════════════

    @Test
    @DisplayName("Integración: cargar grafo, Floyd, modificar arco y recalcular")
    void testIntegrationModifyAndRecalculate() {
        graph.addEdgeWithCities("Guatemala", "Mixco",    15);
        graph.addEdgeWithCities("Guatemala", "Antigua",  45);
        graph.addEdgeWithCities("Mixco",     "Antigua",  30);
        graph.addEdgeWithCities("Antigua",   "Escuintla", 55);

        floyd = new Floyd(graph);
        floyd.compute();

        // Guatemala → Escuintla: Guatemala→Antigua(45)→Escuintla(55) = 100
        //                     o  Guatemala→Mixco(15)→Antigua(30)→Escuintla(55) = 100
        assertEquals(100, floyd.getDistance("Guatemala", "Escuintla"), 0.001);

        // Eliminar Antigua→Escuintla (cordón sanitario)
        graph.removeEdge("Antigua", "Escuintla");
        floyd = new Floyd(graph);
        floyd.compute();

        // Ya no hay camino Guatemala → Escuintla
        assertEquals(Graph.INF, floyd.getDistance("Guatemala", "Escuintla"), 0.001);

        // Agregar nueva ruta directa
        graph.addEdgeWithCities("Guatemala", "Escuintla", 80);
        floyd = new Floyd(graph);
        floyd.compute();

        assertEquals(80, floyd.getDistance("Guatemala", "Escuintla"), 0.001);
    }
}