import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static final String RESULT = "C:\\Users\\Jacob\\Desktop\\results.txt";
    public static final String UGLY_LIST = "C:\\Users\\Jacob\\Desktop\\ugly.txt";
    public static final String USED_PROXIES = "C:\\Users\\Jacob\\Desktop\\used.txt";

    public static void main(String[] args) throws IOException {
        addNewProxies();
        removeUsedProxies();
    }

    public static void addNewProxies() throws FileNotFoundException, UnsupportedEncodingException {
        Pattern p = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b:\\d{2,5}");

        Set<String> results = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(RESULT));
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Current 'results.txt' size: " + results.size());

        try {
            BufferedReader reader = new BufferedReader(new FileReader(UGLY_LIST));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = p.matcher(line.replaceAll(" ", ":").replaceAll("\t", ":"));
                if (m.find()) {
                    results.add(m.group());
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Merge with 'ugly.txt' file. New 'results.txt' size: " + results.size());

        PrintWriter writer = new PrintWriter(RESULT, "UTF-8");
        results.forEach(writer::println);
        writer.close();
    }

    public static void removeUsedProxies() throws FileNotFoundException, UnsupportedEncodingException {
        List<String> oldResults = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(USED_PROXIES));
            String line;
            while ((line = reader.readLine()) != null) {
                oldResults.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("'used.txt' size: " + oldResults.size());

        List<String> newResults = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(RESULT));
            String line;
            while ((line = reader.readLine()) != null) {
                newResults.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        newResults.removeAll(oldResults);

        System.out.println("Remove all used proxies. New 'results.txt' size: " + newResults.size());

        PrintWriter writer = new PrintWriter(RESULT, "UTF-8");
        newResults.forEach(writer::println);
        writer.close();
    }
}