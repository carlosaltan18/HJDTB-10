"""
solution.py – Implementación en Python usando NetworkX
=======================================================
Equivalente completo del programa Java, usando el módulo NetworkX
y su implementación nativa de Floyd-Warshall.
Requisitos:
    pip install networkx
Ejecutar:
    python solution.py
Carlos Altán 25772
"""

import networkx as nx
from typing import Optional




def load_graph(filename: str) -> nx.DiGraph:
    """
    Lee el grafo dirigido desde un archivo .txt con el formato:
        Ciudad1 Ciudad2 KM
    Ignora líneas en blanco y comentarios (#).
    """
    G = nx.DiGraph()
    try:
        with open(filename, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                parts = line.split()
                if len(parts) < 3:
                    print(f"  [!] Línea ignorada (formato incorrecto): {line}")
                    continue
                city1, city2 = parts[0], parts[1]
                try:
                    km = float(parts[2])
                except ValueError:
                    print(f"  [!] Línea ignorada (KM no es número): {line}")
                    continue
                G.add_edge(city1, city2, weight=km)
        print(f"Grafo cargado: {G.number_of_nodes()} ciudades, "
              f"{G.number_of_edges()} arcos desde '{filename}'.")
    except FileNotFoundError:
        print(f"  [!] Archivo '{filename}' no encontrado. Grafo vacío.")
    return G



def run_floyd(G: nx.DiGraph):
    """
    Ejecuta Floyd-Warshall usando NetworkX.

    Devuelve:
        pred : diccionario de predecesores (para reconstruir caminos)
        dist : diccionario de distancias  dist[u][v]
    """
    if G.number_of_nodes() == 0:
        return {}, {}
    pred, dist = nx.floyd_warshall_predecessor_and_distance(G, weight="weight")
    return pred, dist


def get_path(pred: dict, from_city: str, to_city: str) -> list:
    """Reconstruye el camino más corto entre dos ciudades."""
    try:
        return nx.reconstruct_path(from_city, to_city, pred)
    except (KeyError, nx.NetworkXError):
        return []



INF = float("inf")


def eccentricity(node: str, G: nx.DiGraph, dist: dict) -> float:
    """
    Excentricidad de un nodo = mayor distancia desde ese nodo
    hacia cualquier otro vértice alcanzable.
    """
    nodes = list(G.nodes())
    max_d = 0.0
    for v in nodes:
        if v == node:
            continue
        d = dist.get(node, {}).get(v, INF)
        if d > max_d:
            max_d = d
    return max_d


def get_center(G: nx.DiGraph, dist: dict) -> tuple[Optional[str], float]:
    """
    Calcula el centro del grafo según el algoritmo de CentroDeGrafo.pdf:
      1. Ya tenemos la matriz APSP (dist) de Floyd.
      2. Para cada columna i, el máximo es la excentricidad de i.
      3. El vértice con mínima excentricidad es el centro.

    Devuelve (ciudad_centro, excentricidad_mínima).
    """
    nodes = list(G.nodes())
    if not nodes:
        return None, INF

    min_ecc = INF
    center = None
    for u in nodes:
        ecc = eccentricity(u, G, dist)
        if ecc < min_ecc:
            min_ecc = ecc
            center = u
    return center, min_ecc


def print_matrix(G: nx.DiGraph, dist: dict, title: str = "Matriz de Distancias") -> None:
    """Imprime la matriz APSP en forma tabular."""
    nodes = sorted(G.nodes())
    if not nodes:
        print("(El grafo está vacío)")
        return

    W = 16
    print(f"\n{'─' * (20 + W * len(nodes))}")
    print(title)
    print(f"{'─' * (20 + W * len(nodes))}")
    print(f"{'':20}", end="")
    for n in nodes:
        print(f"{n[:W-1]:<{W}}", end="")
    print()

    for u in nodes:
        print(f"{u[:19]:<20}", end="")
        for v in nodes:
            val = dist.get(u, {}).get(v, INF)
            cell = "INF" if val == INF else f"{val:.1f}"
            print(f"{cell:<{W}}", end="")
        print()


def print_adjacency(G: nx.DiGraph) -> None:
    """Imprime la matriz de adyacencia original."""
    nodes = sorted(G.nodes())
    if not nodes:
        print("(El grafo está vacío)")
        return

    adj: dict = {u: {v: INF for v in nodes} for u in nodes}
    for u in nodes:
        adj[u][u] = 0.0
    for u, v, data in G.edges(data=True):
        adj[u][v] = data.get("weight", 1.0)

    W = 16
    print(f"\n{'─' * (20 + W * len(nodes))}")
    print("Matriz de Adyacencia")
    print(f"{'─' * (20 + W * len(nodes))}")
    print(f"{'':20}", end="")
    for n in nodes:
        print(f"{n[:W-1]:<{W}}", end="")
    print()
    for u in nodes:
        print(f"{u[:19]:<20}", end="")
        for v in nodes:
            cell = "INF" if adj[u][v] == INF else f"{adj[u][v]:.1f}"
            print(f"{cell:<{W}}", end="")
        print()


def menu(G: nx.DiGraph) -> None:
    pred, dist = run_floyd(G)

    while True:
        print("\n══════════════════════════════════════")
        print("  MENÚ – Centro de Respuesta Covid-19")
        print("  (Python / NetworkX)")
        print("══════════════════════════════════════")
        print("  1. Ruta más corta entre dos ciudades")
        print("  2. Centro del grafo")
        print("  3. Modificar el grafo")
        print("  4. Mostrar matrices")
        print("  5. Finalizar")
        opt = input("  Opción: ").strip()

        # ── 1. Ruta más corta
        if opt == "1":
            from_city = input("  Ciudad origen  : ").strip()
            to_city   = input("  Ciudad destino : ").strip()

            if from_city not in G:
                print(f"  [!] Ciudad no encontrada: {from_city}")
                continue
            if to_city not in G:
                print(f"  [!] Ciudad no encontrada: {to_city}")
                continue

            d = dist.get(from_city, {}).get(to_city, INF)
            if d == INF:
                print(f"  No existe ruta de {from_city} a {to_city}.")
            else:
                path = get_path(pred, from_city, to_city)
                print(f"  Distancia más corta : {d:.1f} KM")
                print(f"  Ruta                : {' → '.join(path)}")

        # ── 2. Centro del grafo
        elif opt == "2":
            center, min_ecc = get_center(G, dist)
            ecc_str = f"{min_ecc:.1f} KM" if min_ecc < INF else "INF"
            print(f"\n  Centro del grafo : {center}  (excentricidad: {ecc_str})")
            print("\n  Excentricidades por ciudad:")
            for u in sorted(G.nodes()):
                ecc = eccentricity(u, G, dist)
                es = f"{ecc:.1f} KM" if ecc < INF else "INF"
                marker = "  ← centro" if u == center else ""
                print(f"    {u:<25} {es}{marker}")

        # ── 3. Modificar el grafo
        elif opt == "3":
            print("  a) Eliminar arco  (corte / cordón sanitario)")
            print("  b) Agregar o actualizar arco")
            mod = input("  Opción: ").strip().lower()

            if mod == "a":
                c1 = input("  Ciudad origen  : ").strip()
                c2 = input("  Ciudad destino : ").strip()
                if G.has_edge(c1, c2):
                    G.remove_edge(c1, c2)
                    print(f"  ✔ Arco eliminado: {c1} → {c2}")
                else:
                    print(f"  [!] No existe el arco {c1} → {c2}.")
            elif mod == "b":
                c1 = input("  Ciudad origen   : ").strip()
                c2 = input("  Ciudad destino  : ").strip()
                try:
                    km = float(input("  Distancia (KM)  : ").strip())
                except ValueError:
                    print("  [!] Valor inválido.")
                    continue
                G.add_edge(c1, c2, weight=km)
                print(f"  ✔ Arco agregado/actualizado: {c1} → {c2} ({km} KM)")
            else:
                print("  [!] Opción inválida.")
                continue

            # Recalcular Floyd
            pred, dist = run_floyd(G)
            new_center, _ = get_center(G, dist)
            print(f"\n  Rutas recalculadas. Nuevo centro: {new_center}")

        # ── 4. Mostrar matrices
        elif opt == "4":
            print_adjacency(G)
            print_matrix(G, dist, title="Matriz APSP (Floyd-Warshall)")

        # ── 5. Salir
        elif opt == "5":
            print("  ¡Hasta luego!")
            break

        else:
            print("  [!] Opción inválida. Elija entre 1-5.")


if __name__ == "__main__":
    print("═" * 42)
    print("  Hoja de Trabajo 10 – CC2003 UVG")
    print("  Floyd + Centro del Grafo (NetworkX)")
    print("═" * 42)

    filename = input("Nombre del archivo [guategrafo.txt]: ").strip()
    if not filename:
        filename = "guategrafo.txt"

    G = load_graph(filename)

    # Mostrar matrices iniciales
    print_adjacency(G)

    # Ejecutar Floyd inicial
    pred, dist = run_floyd(G)
    print_matrix(G, dist, title="Matriz APSP inicial (Floyd-Warshall)")

    center, ecc = get_center(G, dist)
    ecc_str = f"{ecc:.1f} KM" if ecc < float('inf') else "INF"
    print(f"\nCentro inicial del grafo: {center}  (excentricidad: {ecc_str})")

    menu(G)