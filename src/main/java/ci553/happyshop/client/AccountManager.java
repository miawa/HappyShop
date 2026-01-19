package ci553.happyshop.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;



public class AccountManager {

    private final Map<String, UserRecord> accounts = new ConcurrentHashMap<>(); // userId -> record

    private static final AccountManager INSTANCE = new AccountManager();

    private static final Path USERS_FILE = Paths.get("happyShopDB", "users.json");
    private static final SecureRandom RNG = new SecureRandom();
    private static final int SALT_BYTES = 16;
    private static final String PBKDF2_ALGO = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERS = 100_000;
    private static final int DERIVED_KEY_BITS = 256;

    private AccountManager() {
        try {
            loadFromFile();
        } catch (Exception e) {
            
            System.err.println("AccountManager: failed to load users.json: " + e.getMessage());
        }
    }

    public static AccountManager getInstance() {
        return INSTANCE;
    }

    private static class UserRecord {
        final String name;
        final String saltBase64;
        final String pinHashBase64;

        UserRecord(String name, String saltBase64, String pinHashBase64) {
            this.name = name;
            this.saltBase64 = saltBase64;
            this.pinHashBase64 = pinHashBase64;
        }
    }

   
    public synchronized String createAccount(String name, String pin) {
        if (!isValidPin(pin)) {
            throw new IllegalArgumentException("PIN must be exactly 4 numeric digits");
        }

        String id;
        do {
            id = generateUserId();
        } while (accounts.containsKey(id));

        String saltB64 = generateSaltBase64();
        String hashB64 = hashPinBase64(pin, saltB64);
        accounts.put(id, new UserRecord(name, saltB64, hashB64));
        try {
            saveToFile();
        } catch (IOException e) {
            System.err.println("AccountManager: failed to save users.json: " + e.getMessage());
        }
        return id;
    }

   
    public synchronized String createAccount(String id, String name, String pin) {
        if (!isValidPin(pin)) {
            throw new IllegalArgumentException("PIN must be exactly 4 numeric digits");
        }
        if (id == null || !id.matches("\\d{4}")) {
            throw new IllegalArgumentException("User ID must be a 4-digit string");
        }
        if (accounts.containsKey(id)) {
            throw new IllegalStateException("User ID already taken");
        }
        String saltB64 = generateSaltBase64();
        String hashB64 = hashPinBase64(pin, saltB64);
        accounts.put(id, new UserRecord(name, saltB64, hashB64));
        try {
            saveToFile();
        } catch (IOException e) {
            System.err.println("AccountManager: failed to save users.json: " + e.getMessage());
        }
        return id;
    }

   
    public boolean authenticate(String userId, String pin) {
        if (userId == null || pin == null) return false;
        UserRecord stored = accounts.get(userId);
        if (stored == null) return false;
        try {
            String expected = stored.pinHashBase64;
            String actual = hashPinBase64(pin, stored.saltBase64);
            return constantTimeEquals(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    
    public synchronized String peekAvailableUserId() {
        String id;
        do {
            id = generateUserId();
        } while (accounts.containsKey(id));
        return id;
    }

    public String getNameFor(String userId) {
        UserRecord r = accounts.get(userId);
        return r == null ? null : r.name;
    }

    private static String generateSaltBase64() {
        byte[] salt = new byte[SALT_BYTES];
        RNG.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static String hashPinBase64(String pin, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERS, DERIVED_KEY_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGO);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] aa = a == null ? new byte[0] : a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b == null ? new byte[0] : b.getBytes(StandardCharsets.UTF_8);
        if (aa.length != bb.length) return false;
        int result = 0;
        for (int i = 0; i < aa.length; i++) result |= aa[i] ^ bb[i];
        return result == 0;
    }

    private synchronized void saveToFile() throws IOException {
        try {
            Path dir = USERS_FILE.getParent();
            if (dir != null && !Files.exists(dir)) Files.createDirectories(dir);

            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, UserRecord> e : accounts.entrySet()) {
                String uid = e.getKey();
                UserRecord r = e.getValue();
                String obj = String.format("{\"userId\":\"%s\",\"name\":\"%s\",\"salt\":\"%s\",\"pinHash\":\"%s\"}",
                        escape(uid), escape(r.name), escape(r.saltBase64), escape(r.pinHashBase64));
                parts.add(obj);
            }
            String json = "[" + String.join(",", parts) + "]";
            Files.write(USERS_FILE, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw e;
        }
    }

    private synchronized void loadFromFile() throws IOException {
        if (!Files.exists(USERS_FILE)) return;
        String content = new String(Files.readAllBytes(USERS_FILE), StandardCharsets.UTF_8).trim();
        if (content.isEmpty()) return;

        
        accounts.clear();
        int idx = 0;
        while (idx < content.length() && content.charAt(idx) != '[') idx++;
        if (idx >= content.length()) return;
        idx++; // skip '['
        while (idx < content.length()) {
            while (idx < content.length() && Character.isWhitespace(content.charAt(idx))) idx++;
            if (idx < content.length() && content.charAt(idx) == ']') break;
            if (idx < content.length() && content.charAt(idx) == ',') { idx++; continue; }
            if (idx >= content.length() || content.charAt(idx) != '{') break;
            int objStart = idx;
            int brace = 0;
            do {
                char c = content.charAt(idx);
                if (c == '{') brace++;
                else if (c == '}') brace--;
                idx++;
            } while (idx < content.length() && brace > 0);
            String obj = content.substring(objStart, idx);
            String userId = extractJsonValue(obj, "userId");
            String name = extractJsonValue(obj, "name");
            String salt = extractJsonValue(obj, "salt");
            String pinHash = extractJsonValue(obj, "pinHash");
            if (userId != null && salt != null && pinHash != null) {
                accounts.put(userId, new UserRecord(name == null ? "" : name, salt, pinHash));
            }
        }
    }

    private static String extractJsonValue(String obj, String key) {
        String pattern = "\"" + key + "\"\s*:\s*\"";
        int i = obj.indexOf(pattern);
        if (i < 0) return null;
        i += pattern.length();
        StringBuilder sb = new StringBuilder();
        while (i < obj.length()) {
            char c = obj.charAt(i++);
            if (c == '"') break;
            if (c == '\\' && i < obj.length()) {
                char esc = obj.charAt(i++);
                sb.append(esc);
            } else sb.append(c);
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }

    private String generateUserId() {
        int generated = ThreadLocalRandom.current().nextInt(1000, 10000);
        return Integer.toString(generated);
    }
}
