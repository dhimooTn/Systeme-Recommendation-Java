package com.sysrec.projet_ds1_java.Service;

import com.sysrec.projet_ds1_java.Dao.RessourceDAO;
import com.sysrec.projet_ds1_java.Model.RessourceModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class RecommandationService {

    private static final String DATA_PATH = "src/main/resources/database/interactions.csv";
    private static final double SIMILARITY_THRESHOLD = 0.2;
    private final RessourceDAO ressourceDAO = new RessourceDAO();

    public static class ItemData {
        double rating;
        String category;
        String[] keywords;

        public ItemData(double rating, String category, String[] keywords) {
            this.rating = rating;
            this.category = category;
            this.keywords = keywords;
        }
    }

    public List<RessourceModel> getUserBasedRecommendations(long userId) throws Exception {
        Map<Long, Map<Long, ItemData>> data = loadData(DATA_PATH);
        Map<Long, ItemData> targetRatings = data.get(userId);

        if (targetRatings == null || targetRatings.isEmpty()) {
            return getPopularResources();
        }

        Map<Long, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Map<Long, ItemData>> entry : data.entrySet()) {
            if (entry.getKey() == userId) continue;
            double sim = computePearsonSimilarity(targetRatings, entry.getValue());
            if (sim > SIMILARITY_THRESHOLD) {
                similarities.put(entry.getKey(), sim);
            }
        }

        if (similarities.isEmpty()) {
            return getPopularResources();
        }

        Map<Long, Double> weightedSums = new HashMap<>();
        Map<Long, Double> simSums = new HashMap<>();

        for (Map.Entry<Long, Double> simEntry : similarities.entrySet()) {
            Map<Long, ItemData> neighborRatings = data.get(simEntry.getKey());
            for (Map.Entry<Long, ItemData> itemEntry : neighborRatings.entrySet()) {
                Long itemId = itemEntry.getKey();
                if (targetRatings.containsKey(itemId)) continue;

                double rating = itemEntry.getValue().rating;
                weightedSums.merge(itemId, simEntry.getValue() * rating, Double::sum);
                simSums.merge(itemId, simEntry.getValue(), Double::sum);
            }
        }

        Map<Long, Double> predictions = new HashMap<>();
        for (Long itemId : weightedSums.keySet()) {
            double predicted = weightedSums.get(itemId) / simSums.get(itemId);
            predictions.put(itemId, predicted);
        }

        return predictions.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(2)
                .map(entry -> ressourceDAO.getRessourceParId(entry.getKey().intValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<RessourceModel> getItemBasedRecommendations(long userId) throws Exception {
        Map<Long, Map<Long, ItemData>> data = loadData(DATA_PATH);
        Map<Long, ItemData> targetRatings = data.get(userId);

        if (targetRatings == null || targetRatings.isEmpty()) {
            return getPopularResources();
        }

        Map<Long, ItemData> allItems = new HashMap<>();
        for (Map<Long, ItemData> userItems : data.values()) {
            allItems.putAll(userItems);
        }

        Map<Long, Double> predictions = new HashMap<>();

        for (Map.Entry<Long, ItemData> candidate : allItems.entrySet()) {
            Long itemId = candidate.getKey();
            if (targetRatings.containsKey(itemId)) continue;

            double numerator = 0;
            double denominator = 0;

            for (Map.Entry<Long, ItemData> rated : targetRatings.entrySet()) {
                double sim = cosineSimilarity(candidate.getValue().keywords, rated.getValue().keywords);
                if (sim > SIMILARITY_THRESHOLD) {
                    numerator += sim * rated.getValue().rating;
                    denominator += sim;
                }
            }

            if (denominator > 0) {
                predictions.put(itemId, numerator / denominator);
            }
        }

        return predictions.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(10)
                .map(entry -> ressourceDAO.getRessourceParId(entry.getKey().intValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<RessourceModel> getPopularResources() {
        List<RessourceModel> allResources = ressourceDAO.getToutesLesRessources();
        return allResources.stream()
                .sorted((r1, r2) -> Double.compare(
                        getAverageRating(r2.getResourceId()),
                        getAverageRating(r1.getResourceId())))
                .limit(10)
                .collect(Collectors.toList());
    }

    private double getAverageRating(int resourceId) {
        // Implement this method based on your database structure
        // This is a placeholder implementation
        return 0.0;
    }

    private Map<Long, Map<Long, ItemData>> loadData(String filePath) throws Exception {
        Map<Long, Map<Long, ItemData>> data = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                try {
                    long userId = Long.parseLong(parts[0].trim());
                    long itemId = Long.parseLong(parts[1].trim());
                    double rating = Double.parseDouble(parts[2].trim());
                    String category = parts[3].trim();
                    String[] keywords = parts[4].trim().split("\\s+");

                    ItemData itemData = new ItemData(rating, category, keywords);
                    data.putIfAbsent(userId, new HashMap<>());
                    data.get(userId).put(itemId, itemData);
                } catch (NumberFormatException e) {
                    System.out.println("❌ Ligne ignorée (format incorrect) : " + line);
                }
            }
        }
        return data;
    }

    private double cosineSimilarity(String[] a, String[] b) {
        Set<String> all = new HashSet<>();
        Collections.addAll(all, a);
        Collections.addAll(all, b);

        int[] vecA = new int[all.size()];
        int[] vecB = new int[all.size()];
        int index = 0;

        for (String keyword : all) {
            vecA[index] = Arrays.asList(a).contains(keyword) ? 1 : 0;
            vecB[index] = Arrays.asList(b).contains(keyword) ? 1 : 0;
            index++;
        }

        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        return (normA == 0 || normB == 0) ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double computePearsonSimilarity(Map<Long, ItemData> a, Map<Long, ItemData> b) {
        Set<Long> commonItems = new HashSet<>(a.keySet());
        commonItems.retainAll(b.keySet());

        int n = commonItems.size();
        if (n == 0) return 0;

        double sumA = 0, sumB = 0, sumSqA = 0, sumSqB = 0, sumProd = 0;

        for (Long itemId : commonItems) {
            double ra = a.get(itemId).rating;
            double rb = b.get(itemId).rating;

            sumA += ra;
            sumB += rb;
            sumSqA += ra * ra;
            sumSqB += rb * rb;
            sumProd += ra * rb;
        }

        double numerator = sumProd - (sumA * sumB / n);
        double denominator = Math.sqrt(sumSqA - (sumA * sumA / n)) * Math.sqrt(sumSqB - (sumB * sumB / n));

        return (denominator == 0) ? 0 : numerator / denominator;
    }

    public void recommendUserBased(long userId) throws Exception {
        List<RessourceModel> recommendations = getUserBasedRecommendations(userId);
        System.out.println("\n✅ Recommandations USER-BASED pour l'utilisateur " + userId + " :");
        if (recommendations.isEmpty()) {
            System.out.println("  ⚠️ Pas assez de similarité pour générer une recommandation.");
        } else {
            recommendations.forEach(r ->
                    System.out.println("  ➤ " + r.getTitle() + " (ID: " + r.getResourceId() + ")"));
        }
    }

    public void recommendItemBased(long userId) throws Exception {
        List<RessourceModel> recommendations = getItemBasedRecommendations(userId);
        System.out.println("\n✅ Recommandations ITEM-BASED pour l'utilisateur " + userId + " :");
        if (recommendations.isEmpty()) {
            System.out.println("  ⚠️ Aucune recommandation possible (pas assez de similarité).");
        } else {
            recommendations.forEach(r ->
                    System.out.println("  ➤ " + r.getTitle() + " (ID: " + r.getResourceId() + ")"));
        }
    }

    public static void main(String[] args) throws Exception {
        RecommandationService recommender = new RecommandationService();
        recommender.recommendUserBased(2);
        recommender.recommendItemBased(2);
    }
}