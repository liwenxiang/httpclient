package name.codeboy.httpclient.message;

import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
public class GlobalConfig {
    public String host;
    public int port;
    public String urlFile;
    public int fileReuseCount;
    public int threadNum;
    public int localPortNum;

    public String schema = "https";

    private boolean keepAlive = true;

    private boolean reqJsonFormat = true;

    private double qpsLimit = 1000000000;

    private Map<String, String> defaultHeaders = new HashMap<>(0);

    public double getQpsLimit() {
        return qpsLimit;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public boolean isReqJsonFormat() {
        return reqJsonFormat;
    }

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public GlobalConfig(String[] args) throws ParseException {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        options.addOption(Option.builder("h").argName("host").hasArg(true)
                .desc("dst_host").required(true).build());
        options.addOption(Option.builder("p").argName("port").hasArg(true)
                .desc("dst_port").required(true).build());
        options.addOption(Option.builder("f").argName("file").hasArg(true)
                .desc("inputfile").required(true).build());


        options.addOption(Option.builder("q").argName("qpslimit").hasArg(true)
                .desc("qpslimit").required(false).build());
        options.addOption(Option.builder("t").argName("thead_num").hasArg(true)
                .desc("thead_num").required(false).build());
        options.addOption(Option.builder("n").argName("uselocalportnum").hasArg(true)
                .desc("uselocalportnum").required(false).build());

        options.addOption(Option.builder("fc").argName("fileReuseCount").hasArg(true)
                .desc("fileReuseCount").required(false).build());

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            System.out.println(cmd.toString());
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            throw e;
        }

        host = cmd.getOptionValue("h", "127.0.0.1");
        port = Integer.parseInt(cmd.getOptionValue("p", "8081"));
        urlFile = cmd.getOptionValue("f", "/Users/liwenxiang/html/logs/test.url");
        threadNum = Integer.parseInt(cmd.getOptionValue("t", "2"));
        localPortNum = Integer.parseInt(cmd.getOptionValue("n", "300"));
        qpsLimit = Double.parseDouble(cmd.getOptionValue("q", "1000000000"));
        fileReuseCount = Integer.parseInt(cmd.getOptionValue("fc", "1"));
    }
}
