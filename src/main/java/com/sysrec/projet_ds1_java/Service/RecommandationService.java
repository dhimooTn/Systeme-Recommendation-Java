package com.sysrec.projet_ds1_java.Service;

import com.sysrec.projet_ds1_java.Dao.RessourceDAO;
import com.sysrec.projet_ds1_java.Model.RessourceModel;
import com.sysrec.projet_ds1_java.Utils.DbToCsv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
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

    private void updateInteractionData() {
        try {
            DbToCsv.main(new String[]{});
            System.out.println("✅ Interaction data updated successfully");
        } catch (Exception e) {
            System.out.println("❌ Failed to update interaction data: " + e.getMessage());
            throw new RuntimeException("Failed to update interaction data", e);
        }
    }

    public List<RessourceModel> getUserBasedRecommendations(long userId) throws Exception {
        updateInteractionData();
        Map<Long, Map<Long, ItemData>> data = loadData(DATA_PATH);
        if (data == null || data.isEmpty()) {
            return getPopularResources();
        }

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
            if (simSums.get(itemId) != 0) {
                double predicted = weightedSums.get(itemId) / simSums.get(itemId);
                predictions.put(itemId, predicted);
            }
        }

        return predictions.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(2)
                .map(entry -> {
                    try {
                        return ressourceDAO.getRessourceParId(entry.getKey().intValue());
                    } catch (SQLException e) {
                        System.err.println("Error fetching resource: " + entry.getKey());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<RessourceModel> getItemBasedRecommendations(long userId) throws Exception {
        updateInteractionData();
        Map<Long, Map<Long, ItemData>> data = loadData(DATA_PATH);
        if (data == null || data.isEmpty()) {
            return getPopularResources();
        }

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
                .limit(2)
                .map(entry -> {
                    try {
                        return ressourceDAO.getRessourceParId(entry.getKey().intValue());
                    } catch (SQLException e) {
                        System.err.println("Error fetching resource: " + entry.getKey());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<RessourceModel> getPopularResources() throws SQLException {
        List<RessourceModel> allResources = ressourceDAO.getRessourcesApprouvees();
        return allResources.stream()
                .sorted((r1, r2) -> Double.compare(
                        getAverageRating(r2.getResourceId()),
                        getAverageRating(r1.getResourceId())))
                .limit(2)
                .collect(Collectors.toList());
    }

    private double getAverageRating(int resourceId) {
        try {
            return ressourceDAO.getAverageRatingForTeacher(resourceId);
        } catch (SQLException e) {
            System.err.println("Error getting average rating for resource: " + resourceId);
            return 0.0;
        }
    }

    private Map<Long, Map<Long, ItemData>> loadData(String filePath) throws Exception {
        Map<Long, Map<Long, ItemData>> data = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    long userId = Long.parseLong(parts[0].trim());
                    long itemId = Long.parseLong(parts[1].trim());
                    double rating = Double.parseDouble(parts[2].trim());
                    String category = parts[3].trim();
                    String[] keywords = parts[4].trim().split("\\s+");

                    ItemData itemData = new ItemData(rating, category, keywords);
                    data.computeIfAbsent(userId, k -> new HashMap<>()).put(itemId, itemData);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line with invalid number format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading data file: " + e.getMessage());
            throw e;
        }
        return data;
    }

    private double cosineSimilarity(String[] a, String[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0) {
            return 0.0;
        }

        Set<String> allKeywords = new HashSet<>();
        Collections.addAll(allKeywords, a);
        Collections.addAll(allKeywords, b);

        int[] vecA = new int[allKeywords.size()];
        int[] vecB = new int[allKeywords.size()];
        int index = 0;

        for (String keyword : allKeywords) {
            vecA[index] = Arrays.asList(a).contains(keyword) ? 1 : 0;
            vecB[index] = Arrays.asList(b).contains(keyword) ? 1 : 0;
            index++;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += Math.pow(vecA[i], 2);
            normB += Math.pow(vecB[i], 2);
        }

        return (normA == 0 || normB == 0) ? 0.0 : dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double computePearsonSimilarity(Map<Long, ItemData> a, Map<Long, ItemData> b) {
        Set<Long> commonItems = new HashSet<>(a.keySet());
        commonItems.retainAll(b.keySet());

        int n = commonItems.size();
        if (n == 0) return 0.0;

        double sumA = 0.0, sumB = 0.0;
        double sumSqA = 0.0, sumSqB = 0.0;
        double sumProduct = 0.0;

        for (Long itemId : commonItems) {
            double ra = a.get(itemId).rating;
            double rb = b.get(itemId).rating;

            sumA += ra;
            sumB += rb;
            sumSqA += ra * ra;
            sumSqB += rb * rb;
            sumProduct += ra * rb;
        }

        double numerator = sumProduct - (sumA * sumB / n);
        double denominator = Math.sqrt((sumSqA - (sumA * sumA / n)) * (sumSqB - (sumB * sumB / n)));

        return (denominator == 0) ? 0.0 : numerator / denominator;
    }

    public void recommendUserBased(long userId) throws Exception {
        List<RessourceModel> recommendations = getUserBasedRecommendations(userId);
        System.out.println("\n✅ USER-BASED RECOMMENDATIONS for user " + userId + ":");
        if (recommendations.isEmpty()) {
            System.out.println("  ⚠️ No recommendations available (not enough similarity data)");
        } else {
            recommendations.forEach(r ->
                    System.out.println("  ➤ " + r.getTitle() + " (ID: " + r.getResourceId() + ")"));
        }
    }

    public void recommendItemBased(long userId) throws Exception {
        List<RessourceModel> recommendations = getItemBasedRecommendations(userId);
        System.out.println("\n✅ ITEM-BASED RECOMMENDATIONS for user " + userId + ":");
        if (recommendations.isEmpty()) {
            System.out.println("  ⚠️ No recommendations available (not enough similarity data)");
        } else {
            recommendations.forEach(r ->
                    System.out.println("  ➤ " + r.getTitle() + " (ID: " + r.getResourceId() + ")"));
        }
    }

    public static void main(String[] args) throws Exception {
        RecommandationService recommender = new RecommandationService();


        recommender.recommendUserBased(1);
        recommender.recommendItemBased(1);

    }
}