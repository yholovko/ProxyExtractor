import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static final String RESULT = "results.txt";
    public static final String UGLY_LIST = "ugly.txt";
    public static final String USED_PROXIES = "used.txt";

    //    public static Optional<Boolean> connectToHomeNetwork() {
//        final ExecutorService service = Executors.newSingleThreadExecutor();
//        try {
//            final Future<Boolean> f = service.submit(() -> {
//                while (!service.isShutdown()) {
//                    int i = new Random().nextInt(1000000);
//                    System.out.println(i);
//                    if (i == 23) {
//                        return true;
//                    }
//                }
//                return false;
//            });
//
//            return Optional.of(f.get(1, TimeUnit.SECONDS));
//        } catch (final Exception ignored) {
//        } finally {
//            service.shutdown();
//        }
//        return Optional.of(false);
//    }
//
//    public static void main(final String[] args) {
//        connectToHomeNetwork().ifPresent(aBoolean -> System.out.println(String.format("Trying to connect to home network: %s", aBoolean)));
//    }
    public static void main(String[] args) throws IOException, InterruptedException {
        addNewProxies();
        //checkAllUsedProxies();
    }

    public static void addNewProxies() throws IOException {
        Pattern p = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b:\\d{2,5}");
        Set<String> resultsTxt = new HashSet<>();
        List<String> oldUsedTxt = new ArrayList<>();

        try {
            BufferedReader resultsTxtReader = new BufferedReader(new FileReader(RESULT));
            String line;
            while ((line = resultsTxtReader.readLine()) != null) {
                resultsTxt.add(line);
            }
            resultsTxtReader.close();

            System.out.println("Old 'results.txt' size: " + resultsTxt.size());

            BufferedReader uglyTxtReader = new BufferedReader(new FileReader(UGLY_LIST));
            while ((line = uglyTxtReader.readLine()) != null) {
                Matcher m = p.matcher(line.replaceAll(" ", ":").replaceAll("\t", ":"));
                if (m.find()) {
                    resultsTxt.add(m.group());
                }
            }
            uglyTxtReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(USED_PROXIES));
            String line;
            while ((line = reader.readLine()) != null) {
                oldUsedTxt.add(line);
            }
            reader.close();

            System.out.println("Old 'used.txt' size: " + oldUsedTxt.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        resultsTxt.removeAll(oldUsedTxt);

        System.out.println("New 'results.txt' size: " + resultsTxt.size());

        Set<String> usedProxies = new HashSet<>();
        usedProxies.addAll(oldUsedTxt);

        System.out.println("New 'used.txt' size: " + usedProxies.size());

        PrintWriter writerResultsTxt = new PrintWriter(RESULT, "UTF-8");
        resultsTxt.forEach(writerResultsTxt::println);
        writerResultsTxt.close();

        PrintWriter writerUglyTxt = new PrintWriter(UGLY_LIST, "UTF-8");
        writerUglyTxt.print("");
        writerUglyTxt.close();

        PrintWriter writerUsed = new PrintWriter(USED_PROXIES, "UTF-8");
        usedProxies.forEach(writerUsed::println);
        writerUsed.close();
    }

    public static void checkAllUsedProxies() throws IOException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(300);

        Set<String> usedProxiesOld = new HashSet<>();
        ConcurrentSkipListSet<String> usedProxiesNew = new ConcurrentSkipListSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(USED_PROXIES));
        String line;
        while ((line = reader.readLine()) != null) {
            usedProxiesOld.add(line);
        }
        reader.close();

        int i = 0;
        for (String usedProxy : usedProxiesOld) {
            if (!usedProxy.isEmpty()) {
                i++;
                final int finalI = i;
                es.execute(() -> {
                    InetAddress addr = null;
                    try {
                        addr = InetAddress.getByName(usedProxy.split(":")[0]);
                        if (addr.isReachable(30000)) {
                            usedProxiesNew.add(usedProxy);
                            System.out.println(String.format("Processed: %s out of %s. Iteration: %s", usedProxiesNew.size(), usedProxiesOld.size(), finalI));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        PrintWriter writerUsed = new PrintWriter("usedWithFilter.txt", "UTF-8");
        usedProxiesNew.forEach(writerUsed::println);
        writerUsed.close();
    }
}