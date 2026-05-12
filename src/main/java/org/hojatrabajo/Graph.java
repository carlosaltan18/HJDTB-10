package org.hojatrabajo;
import java.util.*;

/**
 * Graph – Grafo dirigido ponderado con Matriz de Adyacencia dinámica.
 * @Author Carlos Altán
 */
public class Graph {

    /** Valor usado para representar "sin arco" / distancia infinita */
    public static final double INF = Double.MAX_VALUE / 2;

    private double[][]          matrix;
    private final Map<String, Integer> indexMap;
    private final List<String>  cities;

    public Graph() {
        indexMap = new LinkedHashMap<>();
        cities   = new ArrayList<>();
        matrix   = new double[0][0];
    }

    /**
     * Agrega una ciudad al grafo.
     * Si ya existe, no hace nada (sin excepción).
     */
    public void addCity(String city) {
        if (city == null || city.isEmpty() || indexMap.containsKey(city)) return;

        int n = cities.size();
        indexMap.put(city, n);
        cities.add(city);

        double[][] m = new double[n + 1][n + 1];
        for (int i = 0; i <= n; i++)
            for (int j = 0; j <= n; j++)
                m[i][j] = (i < n && j < n) ? matrix[i][j]
                        : (i == j)         ? 0.0
                        :                    INF;
        matrix = m;
    }


    /**
     * Agrega un arco dirigido from → to con el peso dado.
     * Las ciudades deben existir previamente.
     * @return true si el arco se agregó; false si alguna ciudad no existe.
     */
    public boolean addEdge(String from, String to, double weight) {
        if (!indexMap.containsKey(from) || !indexMap.containsKey(to)) return false;
        matrix[indexMap.get(from)][indexMap.get(to)] = weight;
        return true;
    }

    /**
     * Crea las ciudades si no existen y luego agrega el arco.
     * Conveniente para la carga desde archivo.
     */
    public void addEdgeWithCities(String from, String to, double weight) {
        addCity(from);
        addCity(to);
        addEdge(from, to, weight);
    }

    /**
     * Elimina el arco from → to (lo pone en INF).
     *
     * @return true si se eliminó; false si alguna ciudad no existe.
     */
    public boolean removeEdge(String from, String to) {
        if (!indexMap.containsKey(from) || !indexMap.containsKey(to)) return false;
        matrix[indexMap.get(from)][indexMap.get(to)] = INF;
        return true;
    }


    public boolean          hasCity(String city)  { return indexMap.containsKey(city); }
    public int              size()                { return cities.size(); }
    public double[][]       getMatrix()           { return matrix; }
    public List<String>     getCities()           { return Collections.unmodifiableList(cities); }
    public Map<String,Integer> getIndexMap()      { return Collections.unmodifiableMap(indexMap); }

    public double getEdgeWeight(String from, String to) {
        if (!indexMap.containsKey(from) || !indexMap.containsKey(to)) return INF;
        return matrix[indexMap.get(from)][indexMap.get(to)];
    }

    // ─────────────────────────────────────────
    // Imprimir Matriz de Adyacencia
    // ─────────────────────────────────────────
    public void printMatrix() {
        int n = cities.size();
        if (n == 0) { System.out.println("(El grafo está vacío)"); return; }

        final int W = 16; // ancho de columna
        System.out.printf("%-20s", "");
        for (String c : cities) System.out.printf("%-" + W + "s", fit(c, W - 1));
        System.out.println();

        for (int i = 0; i < n; i++) {
            System.out.printf("%-20s", fit(cities.get(i), 19));
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] >= INF) System.out.printf("%-" + W + "s", "INF");
                else                     System.out.printf("%-" + W + ".1f", matrix[i][j]);
            }
            System.out.println();
        }
    }

    private String fit(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "." : s;
    }
}