package org.hojatrabajo;

import java.io.*;
import java.util.*;

/**
 * Main – Programa principal.
 * @Author Carlos Altán
 * Lee el grafo desde un archivo .txt, aplica Floyd-Warshall y
 * ofrece un menú interactivo para:
 *   1. Consultar ruta más corta entre dos ciudades.
 *   2. Ver el centro del grafo.
 *   3. Modificar el grafo (agregar / eliminar arcos).
 *   4. Mostrar la matriz de adyacencia.
 *   5. Salir.
 * Hoja de Trabajo No. 10
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Graph graph = new Graph();

        // ── Cargar archivo ────────────────────────────────────────
        System.out.print("Nombre del archivo del grafo [guategrafo.txt]: ");
        String filename = sc.nextLine().trim();
        if (filename.isEmpty()) filename = "guategrafo.txt";

        loadFromFile(graph, filename);

        System.out.println("\n--- Matriz de Adyacencia inicial ---");
        graph.printMatrix();

        // ── Primera ejecución de Floyd ────────────────────────────
        Floyd floyd = new Floyd(graph);
        floyd.compute();

        // ── Menú principal ────────────────────────────────────────
        boolean running = true;
        while (running) {
            printMenu();
            String opt = sc.nextLine().trim();

            switch (opt) {

                case "1": {
                    System.out.print("Ciudad origen  : ");
                    String from = sc.nextLine().trim();
                    System.out.print("Ciudad destino : ");
                    String to   = sc.nextLine().trim();

                    if (!graph.hasCity(from)) {
                        System.out.println("  ✖ Ciudad origen no encontrada: " + from);
                        break;
                    }
                    if (!graph.hasCity(to)) {
                        System.out.println("  ✖ Ciudad destino no encontrada: " + to);
                        break;
                    }
                    double d = floyd.getDistance(from, to);
                    if (d >= Graph.INF) {
                        System.out.println("  No existe ruta de " + from + " a " + to + ".");
                    } else {
                        List<String> path = floyd.getPath(from, to);
                        System.out.printf("  Distancia más corta : %.1f KM%n", d);
                        System.out.println("  Ruta                : " + String.join(" → ", path));
                    }
                    break;
                }

                case "2": {
                    String center = floyd.getCenter();
                    System.out.println("  Centro del grafo: " + center);
                    System.out.println("\n  Excentricidades:");
                    List<String> cities = graph.getCities();
                    double[] eccs = floyd.getEccentricities();
                    for (int i = 0; i < cities.size(); i++) {
                        String eccStr = eccs[i] >= Graph.INF
                                ? "INF (no alcanza todos los nodos)"
                                : String.format("%.1f KM", eccs[i]);
                        System.out.printf("    %-25s %s%n", cities.get(i), eccStr);
                    }
                    break;
                }

                case "3": {
                    System.out.println("  a) Eliminar arco (corte de carretera / cordón sanitario)");
                    System.out.println("  b) Agregar o actualizar arco");
                    System.out.print("  Opción: ");
                    String mod = sc.nextLine().trim().toLowerCase();

                    if (mod.equals("a")) {
                        System.out.print("  Ciudad origen  : ");
                        String c1 = sc.nextLine().trim();
                        System.out.print("  Ciudad destino : ");
                        String c2 = sc.nextLine().trim();
                        if (graph.removeEdge(c1, c2)) {
                            System.out.println("  ✔ Arco eliminado: " + c1 + " → " + c2);
                        } else {
                            System.out.println("  ✖ No se encontró el arco (verifique los nombres).");
                        }

                    } else if (mod.equals("b")) {
                        System.out.print("  Ciudad origen   : ");
                        String c1 = sc.nextLine().trim();
                        System.out.print("  Ciudad destino  : ");
                        String c2 = sc.nextLine().trim();
                        System.out.print("  Distancia (KM)  : ");
                        double km;
                        try {
                            km = Double.parseDouble(sc.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("  ✖ Valor inválido.");
                            break;
                        }
                        graph.addEdgeWithCities(c1, c2, km);
                        System.out.println("  ✔ Arco agregado/actualizado: " + c1 + " → " + c2
                                + " (" + km + " KM)");
                    } else {
                        System.out.println("  ✖ Opción inválida.");
                        break;
                    }

                    floyd = new Floyd(graph);
                    floyd.compute();
                    System.out.println("\n  Rutas recalculadas.");
                    System.out.println("  Nuevo centro del grafo: " + floyd.getCenter());
                    break;
                }

                case "4": {
                    System.out.println("\n--- Matriz de Adyacencia ---");
                    graph.printMatrix();
                    System.out.println("\n--- Matriz APSP (Floyd) ---");
                    floyd.printDistMatrix();
                    break;
                }

                case "5": {
                    running = false;
                    System.out.println("¡Hasta luego!");
                    break;
                }

                default:
                    System.out.println("  Opción inválida. Elija entre 1-5.");
            }
        }
        sc.close();
    }


    private static void loadFromFile(Graph graph, String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;   // saltar comentarios
                String[] parts = line.split("\\s+");
                if (parts.length < 3) {
                    System.out.println("  Línea ignorada (formato incorrecto): " + line);
                    continue;
                }
                String city1 = parts[0];
                String city2 = parts[1];
                double km;
                try {
                    km = Double.parseDouble(parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("  Línea ignorada (KM no es número): " + line);
                    continue;
                }
                graph.addEdgeWithCities(city1, city2, km);
                count++;
            }
            System.out.println("Grafo cargado: " + graph.size()
                    + " ciudades, " + count + " arcos desde \"" + filename + "\".");
        } catch (FileNotFoundException e) {
            System.out.println("  ✖ Archivo no encontrado: " + filename);
            System.out.println("  Continuando con grafo vacío...");
        } catch (IOException e) {
            System.out.println("  ✖ Error al leer el archivo: " + e.getMessage());
        }
    }


    private static void printMenu() {
        System.out.println("\n══════════════════════════════════════");
        System.out.println("  MENÚ – Centro de Respuesta Covid-19");
        System.out.println("══════════════════════════════════════");
        System.out.println("  1. Ruta más corta entre dos ciudades");
        System.out.println("  2. Centro del grafo");
        System.out.println("  3. Modificar el grafo");
        System.out.println("  4. Mostrar matrices");
        System.out.println("  5. Finalizar");
        System.out.print("  Opción: ");
    }
}