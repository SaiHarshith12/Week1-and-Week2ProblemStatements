import java.util.concurrent.ConcurrentHashMap;

    class TokenBucket {
        double tokens;
        long lastRefillTime;
        final int maxTokens;
        final double refillRate; // tokens per second

        public TokenBucket(int maxTokens, double refillRate) {
            this.tokens = maxTokens;
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.lastRefillTime = System.nanoTime();
        }
    }

    class RateLimiter {

        private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
        private final int MAX_TOKENS = 1000;
        private final double REFILL_RATE = 1000.0 / 3600.0; // per second

        public String checkRateLimit(String clientId) {
            TokenBucket bucket = buckets.computeIfAbsent(clientId,
                    k -> new TokenBucket(MAX_TOKENS, REFILL_RATE));

            synchronized (bucket) {
                refill(bucket);

                if (bucket.tokens >= 1) {
                    bucket.tokens -= 1;
                    return "Allowed (" + (int) bucket.tokens + " requests remaining)";
                } else {
                    double waitTime = (1 - bucket.tokens) / bucket.refillRate;
                    return "Denied (0 remaining, retry after " + (int) waitTime + " seconds)";
                }
            }
        }

        private void refill(TokenBucket bucket) {
            long now = System.nanoTime();
            double secondsPassed = (now - bucket.lastRefillTime) / 1e9;

            double tokensToAdd = secondsPassed * bucket.refillRate;
            bucket.tokens = Math.min(bucket.maxTokens, bucket.tokens + tokensToAdd);

            bucket.lastRefillTime = now;
        }

        public String getRateLimitStatus(String clientId) {
            TokenBucket bucket = buckets.get(clientId);
            if (bucket == null) return "No usage yet";

            synchronized (bucket) {
                refill(bucket);

                int remaining = (int) bucket.tokens;
                int used = MAX_TOKENS - remaining;

                return "{used: " + used +
                        ", remaining: " + remaining +
                        ", limit: " + MAX_TOKENS + "}";
            }
        }
    }

    public class TokenBucketRateLimiter {
        public static void main(String[] args) throws InterruptedException {

            RateLimiter rl = new RateLimiter();
            String clientId = "abc123";

            // Simulate requests
            for (int i = 0; i < 1005; i++) {
                String result = rl.checkRateLimit(clientId);
                System.out.println(result);

                // Small delay to see refill effect (optional)
                if (i % 200 == 0) Thread.sleep(100);
            }

            // Check status
            System.out.println("\nFinal Status:");
            System.out.println(rl.getRateLimitStatus(clientId));
        }
    }

