package org.hojatrabajo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Floyd.
 */
class FloydTest {

    private Graph graph;
    private Floyd floyd;

    @BeforeEach
    void setUp() {
        graph = new Graph();
    }

    @Test
    @DisplayName("Grafo vacío: compute no debe fallar y centro debe ser null")
    void testEmptyGraph() {
        floyd = new Floyd(graph);
        floyd.compute();

        assertNull(floyd.getCenter(), "El centro de un grafo vacío debe ser null");
        assertEquals(Graph.INF, floyd.getDistance("A", "B"), "Distancia en grafo vacío debe ser INF");
    }

    @Test
    @DisplayName("Distancia directa e indirecta (ruta más corta)")
    void testShortestPathDistance() {
        // A -> B (10), B -> C (10), A -> C (50)
        graph.addEdgeWithCities("A", "B", 10);
        graph.addEdgeWithCities("B", "C", 10);
        graph.addEdgeWithCities("A", "C", 50);

        floyd = new Floyd(graph);
        floyd.compute();

        // La ruta A -> B -> C (20) es mejor que A -> C (50)
        assertEquals(20.0, floyd.getDistance("A", "C"), 0.001, "Debe elegir la ruta más corta A -> B -> C");
        assertEquals(10.0, floyd.getDistance("A", "B"), 0.001, "Distancia directa A -> B debe ser 10");

        // Grafo dirigido, no debe haber regreso de C a A
        assertEquals(Graph.INF, floyd.getDistance("C", "A"), 0.001, "No hay ruta de C a A");
    }

    @Test
    @DisplayName("Reconstrucción del camino (getPath)")
    void testGetPath() {
        graph.addEdgeWithCities("Guatemala", "Mixco", 15);
        graph.addEdgeWithCities("Mixco", "Antigua", 30);
        graph.addEdgeWithCities("Antigua", "Escuintla", 25);

        floyd = new Floyd(graph);
        floyd.compute();

        List<String> path = floyd.getPath("Guatemala", "Escuintla");

        assertEquals(4, path.size(), "El camino debe contener 4 ciudades");
        assertEquals("Guatemala", path.get(0));
        assertEquals("Mixco", path.get(1));
        assertEquals("Antigua", path.get(2));
        assertEquals("Escuintla", path.get(3));
    }

    @Test
    @DisplayName("Ruta hacia sí mismo debe devolver solo esa ciudad y distancia 0")
    void testSelfPathAndDistance() {
        graph.addEdgeWithCities("A", "B", 5);

        floyd = new Floyd(graph);
        floyd.compute();

        assertEquals(0.0, floyd.getDistance("A", "A"), 0.001);

        List<String> path = floyd.getPath("A", "A");
        assertEquals(1, path.size());
        assertEquals("A", path.get(0));
    }

    @Test
    @DisplayName("Ciudades desconectadas o desconocidas")
    void testDisconnectedAndUnknownCities() {
        graph.addEdgeWithCities("A", "B", 5);
        graph.addEdgeWithCities("C", "D", 10);

        floyd = new Floyd(graph);
        floyd.compute();

        // Desconectadas
        assertEquals(Graph.INF, floyd.getDistance("A", "C"), 0.001);
        assertTrue(floyd.getPath("A", "C").isEmpty(), "El camino entre ciudades desconectadas debe estar vacío");

        // Desconocidas
        assertEquals(Graph.INF, floyd.getDistance("A", "X"), 0.001);
        assertTrue(floyd.getPath("A", "X").isEmpty());
    }

    @Test
    @DisplayName("Cálculo correcto del centro del grafo")
    void testGraphCenter() {
        // Grafo en forma de línea bidireccional A <-> B <-> C
        graph.addEdgeWithCities("A", "B", 10);
        graph.addEdgeWithCities("B", "A", 10);
        graph.addEdgeWithCities("B", "C", 10);
        graph.addEdgeWithCities("C", "B", 10);

        floyd = new Floyd(graph);
        floyd.compute();

        // Excentricidades esperadas:
        // A -> llega a B(10), C(20). Max = 20
        // C -> llega a B(10), A(20). Max = 20
        // B -> llega a A(10), C(10). Max = 10  <-- Menor excentricidad (Centro)

        double[] eccs = floyd.getEccentricities();
        assertEquals(3, eccs.length);

        String center = floyd.getCenter();
        assertEquals("B", center, "B debería ser el centro porque tiene la menor excentricidad");
    }

    @Test
    @DisplayName("Actualización de distancias tras recalcular (compute)")
    void testRecomputeAfterGraphChange() {
        graph.addEdgeWithCities("A", "B", 10);
        floyd = new Floyd(graph);
        floyd.compute();
        assertEquals(10.0, floyd.getDistance("A", "B"), 0.001);

        // Simulamos un cambio en el grafo
        graph.addEdge("A", "B", 5);

        floyd.compute();
        assertEquals(5.0, floyd.getDistance("A", "B"), 0.001, "La distancia debe actualizarse tras re-ejecutar compute");
    }
}