import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Detect {

    public static String getChecksum(String appSecret, String nonce, String curTime) {
        return encode("sha1", appSecret + nonce + curTime);
    }

    private static String encode(String algorithm, String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(value.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    public static String getNonce() {
        // TO BE REPLACED: use your algorithm to generate a random string
        String digest = UUID.randomUUID().toString();
        return digest.replace("-", "");
    }

    public static String getSubmitBody() {
        JSONObject obj = new JSONObject();

        // TO BE REPLACED: use your parameters instead
        obj.put("monitorUid", 100112);
        obj.put("channelName", "room2233");
        obj.put("detectType", 0);
        obj.put("scFrequency", 10);
        obj.put("callback", "demo callback data");

        return obj.toString();
    }

    public static String getStopBody() {
        JSONObject reqBody = new JSONObject();
        JSONArray chanArray = new JSONArray();
        JSONObject chanObj = new JSONObject();

        // TO BE REPLACED: use your parameters instead
        chanObj.put("channelName", "room2233");
        chanObj.put("status", 100);
        chanArray.add(chanObj);
        reqBody.put("realTimeInfoList", chanArray);

        return reqBody.toString();
    }

    public static void stop() throws Exception {
        URL url = new URL("https://logic-dev.netease.im/livewallsolution/feedback");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("POST");

        // Set headers for authentication
        String curtime = String.valueOf(System.currentTimeMillis() / 1000L);
        String nonce = getNonce();

        // TO BE RELACED: replace with your app key and secret
        String appkey = System.getenv("APPKEY");
        String appsecret = System.getenv("APPSECRET");

        conn.setRequestProperty("CurTime", curtime);
        conn.setRequestProperty("Nonce", nonce);
        conn.setRequestProperty("AppKey", appkey);
        conn.setRequestProperty("CheckSum", getChecksum(appsecret, nonce, curtime));

        String jsonInputString = getStopBody();
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Get response and print out
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer resp = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }
            reader.close();
            System.out.println(resp);
        } finally {
            conn.disconnect();
        }
    }

    public static void submit() throws Exception {
        URL url = new URL("https://logic-dev.netease.im/livewallsolution/submit");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("POST");

        // Set headers for authentication
        String curtime = String.valueOf(System.currentTimeMillis() / 1000L);
        String nonce = getNonce();

        // TO BE RELACED: replace with your app key and secret
        String appkey = System.getenv("APPKEY");
        String appsecret = System.getenv("APPSECRET");

        conn.setRequestProperty("CurTime", curtime);
        conn.setRequestProperty("Nonce", nonce);
        conn.setRequestProperty("AppKey", appkey);
        conn.setRequestProperty("CheckSum", getChecksum(appsecret, nonce, curtime));

        String jsonInputString = getSubmitBody();
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Get response and print out
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer resp = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }
            reader.close();
            System.out.println(resp);
        } finally {
            conn.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
        submit();
        stop();
    }
}
