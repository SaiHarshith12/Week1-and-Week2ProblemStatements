import java.util.*;

class PlagiarismDetector {

    private int n;
    private Map<String, Set<String>> ngramIndex = new HashMap<>();
    private Map<String, List<String>> docNgrams = new HashMap<>();

    public PlagiarismDetector(int n) {
        this.n = n;
    }

    public void addDocument(String docName, String text) {

        String[] words = text.toLowerCase().split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.length - n; i++) {

            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < n; j++) {
                sb.append(words[i + j]).append(" ");
            }

            String ngram = sb.toString().trim();
            ngrams.add(ngram);

            ngramIndex.putIfAbsent(ngram, new HashSet<>());
            ngramIndex.get(ngram).add(docName);
        }

        docNgrams.put(docName, ngrams);
    }

    public double calculateSimilarity(String doc1, String doc2) {

        Set<String> set1 = new HashSet<>(docNgrams.get(doc1));
        Set<String> set2 = new HashSet<>(docNgrams.get(doc2));

        set1.retainAll(set2);

        int common = set1.size();
        int total = docNgrams.get(doc1).size();

        return (double) common / total * 100;
    }

    public String mostSimilar(String docName) {

        double maxSimilarity = 0;
        String bestDoc = null;

        for (String otherDoc : docNgrams.keySet()) {

            if (!otherDoc.equals(docName)) {

                double sim = calculateSimilarity(docName, otherDoc);

                if (sim > maxSimilarity) {
                    maxSimilarity = sim;
                    bestDoc = otherDoc;
                }
            }
        }

        System.out.println("Similarity: " + maxSimilarity + "%");
        return bestDoc;
    }
}
public class PlagiarismDetectionSystem {
    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector(3); // 3-word n-grams

        String doc1 = "data structures and algorithms are important";
        String doc2 = "data structures and algorithms are useful";
        String doc3 = "machine learning is different from algorithms";

        detector.addDocument("Doc1", doc1);
        detector.addDocument("Doc2", doc2);
        detector.addDocument("Doc3", doc3);

        System.out.println("Most similar to Doc1: " + detector.mostSimilar("Doc1"));
    }
}

