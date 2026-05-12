package org.hojatrabajo;

import java.util.*;

/**
 * Floyd – Implementación del algoritmo de Floyd-Warshall.
 *
 * Calcula:
 *   • Matriz de distancias mínimas entre cualquier par de vértices.
 *   • Reconstrucción del camino más corto (con ciudades intermedias).
 *   • Centro del grafo (vértice de mínima excentricidad).
 *
 * CC2003 – Algoritmos y Estructura de Datos
 * Hoja de Trabajo No. 10
 */
public class Floyd {

    private double[][]  dist;   // dist[i][j] = distancia mínima i → j
    private int[][]     next;   // next[i][j] = siguiente nodo en el camino i → j
    private final Graph graph;

    // ─────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────
    public Floyd(Graph graph) {
        this.graph = graph;
    }

    // ─────────────────────────────────────────
    // Algoritmo de Floyd-Warshall  O(n³)
    // ─────────────────────────────────────────
    /**
     * Ejecuta Floyd-Warshall sobre el grafo actual.
     * Debe llamarse (o re-llamarse) cada vez que el grafo cambia.
     */
    public void compute() {
        int n = graph.size();
        if (n == 0) { dist = new double[0][0]; next = new int[0][0]; return; }

        double[][] adj = graph.getMatrix();
        dist = new double[n][n];
        next = new int[n][n];

        // Inicialización: copiar adyacencias y configurar 'next'
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = adj[i][j];
                // Si existe arco i→j (y no es el mismo nodo), next apunta a j
                next[i][j] = (i != j && adj[i][j] < Graph.INF) ? j : -1;
            }
        }

        // Triple ciclo  k = nodo intermedio
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] < Graph.INF && dist[k][j] < Graph.INF) {
                        double through = dist[i][k] + dist[k][j];
                        if (through < dist[i][j]) {
                            dist[i][j] = through;
                            next[i][j] = next[i][k];   // redirigir camino
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────
    // Consultas de distancia
    // ─────────────────────────────────────────
    /**
     * @return distancia mínima de 'from' a 'to'.
     *         Graph.INF si no existe camino o alguna ciudad es desconocida.
     */
    public double getDistance(String from, String to) {
        Map<String, Integer> idx = graph.getIndexMap();
        if (!idx.containsKey(from) || !idx.containsKey(to)) return Graph.INF;
        return dist[idx.get(from)][idx.get(to)];
    }

    // ─────────────────────────────────────────
    // Reconstrucción del camino
    // ─────────────────────────────────────────
    /**
     * Reconstruye el camino más corto de 'from' a 'to'.
     *
     * @return Lista de ciudades desde origen hasta destino (incluidos).
     *         Lista vacía si no existe camino.
     */
    public List<String> getPath(String from, String to) {
        List<String> path = new ArrayList<>();
        Map<String, Integer> idx = graph.getIndexMap();
        List<String> cities = graph.getCities();

        if (!idx.containsKey(from) || !idx.containsKey(to)) return path;

        int u = idx.get(from);
        int v = idx.get(to);

        if (u == v) { path.add(cities.get(u)); return path; }
        if (next[u][v] == -1) return path;   // sin camino

        path.add(cities.get(u));
        while (u != v) {
            u = next[u][v];
            if (u == -1) return new ArrayList<>();   // camino roto (no debería ocurrir)
            path.add(cities.get(u));
        }
        return path;
    }

    // ─────────────────────────────────────────
    // Excentricidad de un vértice
    // ─────────────────────────────────────────
    /**
     * La excentricidad del vértice i es la mayor distancia desde i
     * hacia cualquier otro vértice alcanzable.
     *
     * @return arreglo de excentricidades, uno por ciudad (mismo orden que graph.getCities()).
     */
    public double[] getEccentricities() {
        int n = graph.size();
        double[] ecc = new double[n];
        for (int i = 0; i < n; i++) {
            double max = 0;
            for (int j = 0; j < n; j++) {
                if (i != j && dist[i][j] > max) max = dist[i][j];
            }
            ecc[i] = max;   // si max == 0 y n>1, city i no llega a nadie → 0 (caso borde)
        }
        return ecc;
    }

    // ─────────────────────────────────────────
    // Centro del grafo
    // ─────────────────────────────────────────
    /**
     * El centro del grafo es el vértice con menor excentricidad.
     * (Algoritmo descrito en CentroDeGrafo.pdf)
     *
     * Pasos:
     *   1. Aplicar Floyd → dist (ya hecho en compute()).
     *   2. Para cada columna i, tomar el máximo → excentricidad(i).
     *   3. El vértice con mínima excentricidad es el centro.
     *
     * @return nombre de la ciudad centro, o null si el grafo está vacío.
     */
    public String getCenter() {
        int n = graph.size();
        if (n == 0) return null;

        List<String> cities = graph.getCities();
        double[] ecc = getEccentricities();

        double minEcc = Graph.INF;
        String center = null;

        for (int i = 0; i < n; i++) {
            if (ecc[i] < minEcc) {
                minEcc = ecc[i];
                center  = cities.get(i);
            }
        }
        return center;
    }

    // ─────────────────────────────────────────
    // Acceso a matrices internas (para pruebas)
    // ─────────────────────────────────────────
    public double[][] getDist() { return dist; }
    public int[][]    getNext() { return next; }

    // ─────────────────────────────────────────
    // Imprimir la APSP (All-Pairs Shortest Path)
    // ─────────────────────────────────────────
    public void printDistMatrix() {
        List<String> cities = graph.getCities();
        int n = cities.size();
        if (n == 0) { System.out.println("(El grafo está vacío)"); return; }

        final int W = 16;
        System.out.printf("%-20s", "APSP");
        for (String c : cities) System.out.printf("%-" + W + "s", fit(c, W - 1));
        System.out.println();

        for (int i = 0; i < n; i++) {
            System.out.printf("%-20s", fit(cities.get(i), 19));
            for (int j = 0; j < n; j++) {
                if (dist[i][j] >= Graph.INF) System.out.printf("%-" + W + "s", "INF");
                else                          System.out.printf("%-" + W + ".1f", dist[i][j]);
            }
            System.out.println();
        }
    }

    private String fit(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "." : s;
    }
}