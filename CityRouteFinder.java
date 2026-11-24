
import java.util.*;

public class CityRouteFinder {

    // Store the road network using an Adjacency List
    static Map<String, List<Edge>> graph = new LinkedHashMap<>();

    // Store blocked roads
    static Set<String> blockedRoads = new HashSet<>();

    static final long INF = Long.MAX_VALUE;

    // Edge class represents a road and its distance
    static class Edge {
        String destination;
        int distance;

        Edge(String destination, int distance) {
            this.destination = destination;
            this.distance = distance;
        }
    }

    // Class used by PriorityQueue
    static class NodeDistance {
        String city;
        long distance;

        NodeDistance(String city, long distance) {
            this.city = city;
            this.distance = distance;
        }
    }

    // Add a city to the graph
    static void addCity(String city) {
        graph.putIfAbsent(city, new ArrayList<>());
    }

    // Find the actual city name (case-insensitive)
    static String getCityName(String city) {
        for (String name : graph.keySet()) {
            if (name.equalsIgnoreCase(city.trim())) {
                return name;
            }
        }
        return null;
    }

    // Generate a unique key for a two-way road
    static String roadKey(String city1, String city2) {
        String a = city1.toLowerCase();
        String b = city2.toLowerCase();

        if (a.compareTo(b) < 0) {
            return a + "|" + b;
        }

        return b + "|" + a;
    }

    // Add a two-way road
    static void addRoad(String city1, String city2, int distance) {

        addCity(city1);
        addCity(city2);

        graph.get(city1).add(new Edge(city2, distance));
        graph.get(city2).add(new Edge(city1, distance));
    }

    // Check whether a road exists
    static boolean roadExists(String city1, String city2) {

        for (Edge edge : graph.get(city1)) {
            if (edge.destination.equals(city2)) {
                return true;
            }
        }

        return false;
    }

    // Block a road
    static void blockRoad(String city1, String city2) {

        String a = getCityName(city1);
        String b = getCityName(city2);

        if (a == null || b == null) {
            System.out.println("Invalid city name.");
            return;
        }

        if (!roadExists(a, b)) {
            System.out.println("Road does not exist.");
            return;
        }

        String key = roadKey(a, b);

        if (blockedRoads.contains(key)) {
            System.out.println("Road is already blocked.");
        } else {
            blockedRoads.add(key);
            System.out.println("Road " + a + "-" + b + " blocked.");
        }
    }

    // Unblock a road
    static void unblockRoad(String city1, String city2) {

        String a = getCityName(city1);
        String b = getCityName(city2);

        if (a == null || b == null) {
            System.out.println("Invalid city name.");
            return;
        }

        if (!roadExists(a, b)) {
            System.out.println("Road does not exist.");
            return;
        }

        String key = roadKey(a, b);

        if (blockedRoads.remove(key)) {
            System.out.println("Road " + a + "-" + b + " unblocked.");
        } else {
            System.out.println("This road is not blocked.");
        }
    }

    // Find the shortest route using Dijkstra's Algorithm
    static void findShortestRoute(String source, String destination) {

        String start = getCityName(source);
        String end = getCityName(destination);

        // Validate source city
        if (start == null) {
            System.out.println("City " + source + " not found in the network.");
            return;
        }

        // Validate destination city
        if (end == null) {
            System.out.println("City " + destination + " not found in the network.");
            return;
        }

        // Distance map
        Map<String, Long> dist = new HashMap<>();

        // Parent map to reconstruct the route
        Map<String, String> parent = new HashMap<>();

        // Initialize all distances to infinity
        for (String city : graph.keySet()) {
            dist.put(city, INF);
        }

        dist.put(start, 0L);

        // PriorityQueue selects the city with minimum distance
        PriorityQueue<NodeDistance> pq =
                new PriorityQueue<>(
                        Comparator.comparingLong(node -> node.distance)
                );

        pq.add(new NodeDistance(start, 0));

        while (!pq.isEmpty()) {

            NodeDistance current = pq.poll();

            String currentCity = current.city;
            long currentDistance = current.distance;

            // Ignore outdated PriorityQueue entries
            if (currentDistance != dist.get(currentCity)) {
                continue;
            }

            // Destination reached
            if (currentCity.equals(end)) {
                break;
            }

            // Visit neighbouring cities
            for (Edge edge : graph.get(currentCity)) {

                String neighbour = edge.destination;

                // Skip blocked roads
                if (blockedRoads.contains(
                        roadKey(currentCity, neighbour))) {
                    continue;
                }

                long newDistance =
                        currentDistance + edge.distance;

                // Update if a shorter path is found
                if (newDistance < dist.get(neighbour)) {

                    dist.put(neighbour, newDistance);

                    parent.put(neighbour, currentCity);

                    pq.add(new NodeDistance(
                            neighbour, newDistance));
                }
            }
        }

        // Check whether destination is reachable
        if (dist.get(end) == INF) {
            System.out.println("No route available between "
                    + start + " and " + end + ".");
            return;
        }

        // Reconstruct the shortest route
        List<String> path = new ArrayList<>();

        String current = end;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        Collections.reverse(path);

        // Display result
        System.out.println("\n----- SHORTEST ROUTE -----");

        System.out.println("Route: "
                + String.join(" -> ", path));

        System.out.println("Distance: "
                + dist.get(end) + " km");

        System.out.println("--------------------------");
    }

    // Display the complete road network
    static void displayNetwork() {

        System.out.println("\n----- ROAD NETWORK -----");

        for (String city : graph.keySet()) {

            System.out.print(city + " -> ");

            for (Edge edge : graph.get(city)) {

                String status = blockedRoads.contains(
                        roadKey(city, edge.destination))
                        ? " [BLOCKED]"
                        : "";

                System.out.print(
                        edge.destination + " ("
                        + edge.distance + " km)"
                        + status + "   ");
            }

            System.out.println();
        }

        System.out.println("------------------------");
    }

    // Main method
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Initialize cities and roads
        addRoad("Durg", "Bhilai", 10);
        addRoad("Durg", "Raipur", 35);
        addRoad("Bhilai", "Raipur", 30);
        addRoad("Raipur", "Bilaspur", 115);

        int choice;

        do {

            System.out.println("\n================================");
            System.out.println("        CITY ROUTE FINDER");
            System.out.println("================================");

            System.out.println("1. Find Shortest Route");
            System.out.println("2. Block a Road");
            System.out.println("3. Unblock a Road");
            System.out.println("4. Display Road Network");
            System.out.println("5. Exit");

            System.out.println("================================");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {

                case 1:

                    System.out.print("Enter starting city: ");
                    String source = sc.nextLine();

                    System.out.print("Enter destination city: ");
                    String destination = sc.nextLine();

                    findShortestRoute(source, destination);

                    break;

                case 2:

                    System.out.print("Enter first city: ");
                    String city1 = sc.nextLine();

                    System.out.print("Enter second city: ");
                    String city2 = sc.nextLine();

                    blockRoad(city1, city2);

                    break;

                case 3:

                    System.out.print("Enter first city: ");
                    String city3 = sc.nextLine();

                    System.out.print("Enter second city: ");
                    String city4 = sc.nextLine();

                    unblockRoad(city3, city4);

                    break;

                case 4:

                    displayNetwork();

                    break;

                case 5:

                    System.out.println("Thank you for using City Route Finder!");

                    break;

                default:

                    System.out.println("Invalid choice. Try again.");
            }

        } while (true);

    }
}
