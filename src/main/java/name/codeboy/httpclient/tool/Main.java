package name.codeboy.httpclient.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Scanner;

import name.codeboy.httpclient.HttpClient;
import name.codeboy.httpclient.message.HttpRequestMessage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	@SuppressWarnings({ "deprecation", "static-access" })
	public static void main(String[] args) throws MalformedURLException, InterruptedException, FileNotFoundException {
		Options options = new Options();
		CommandLineParser parser =  new DefaultParser();
		
		options.addOption(OptionBuilder.withArgName("host").hasArg(true)
				.withDescription("dst_host").isRequired(true).create("d"));
		options.addOption(OptionBuilder.withArgName("port").hasArg(true)
				.withDescription("dst_port").isRequired(true).create("p"));
		options.addOption(OptionBuilder.withArgName("f").hasArg(true)
				.withDescription("urlfile").isRequired(true).create("f"));

		options.addOption(OptionBuilder.withArgName("thead_num").hasArg(true)
				.withDescription("thead_num").isRequired(false).create("t"));
		options.addOption(OptionBuilder.withArgName("uselocalportnum").hasArg(true)
				.withDescription("uselocalportnum").isRequired(false).create("n"));

		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
			System.out.println(cmd.toString());

		} catch (ParseException e) {
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "ant", options );
			System.exit(-1);
		}

		String host = cmd.getOptionValue("d", "127.0.0.1");
		int port = Integer.parseInt(cmd.getOptionValue("p", "8081"));
		String urlFile = cmd.getOptionValue("f", "/Users/liwenxiang/html/logs/test.url");
		int threadNum = Integer.parseInt(cmd.getOptionValue("t", "2"));
		int localPortNum = Integer.parseInt(cmd.getOptionValue("n", "300"));

		HttpClient client = new HttpClient(host, port, localPortNum, threadNum);
		
		Scanner sc = new Scanner(new File(urlFile));
		long totalNum = 0;
		while (sc.hasNextLine()) {
			String url = sc.nextLine();
			client.sendMessage(new HttpRequestMessage(url));
			totalNum++;
		}
		sc.close();
		logger.info("msg add queue end size is {}", client.todoMsg());

		long beginTime = System.currentTimeMillis();
		client.init();
		while (!client.isSendEnd()) {
			Thread.sleep(500);
			long useTime = System.currentTimeMillis() - beginTime; 
			long sendedNum = totalNum - client.todoMsg();
			logger.info("total_num {} toSendMsg {}  hasseneded {} use time {} ratio is {}", 
					totalNum, client.todoMsg(), sendedNum, useTime, sendedNum * 1000f / useTime);
		}

		long useTime = System.currentTimeMillis() - beginTime; 
		long sendedNum = totalNum - client.todoMsg();
		logger.info("total_num {} toSendMsg {}  hasseneded {} use time {} ratio is {}", 
				totalNum, client.todoMsg(), sendedNum, useTime, sendedNum * 1000f / useTime);

		logger.info("close send {}", System.currentTimeMillis() - beginTime);
		logger.info("send end wait end");
		Thread.sleep(2000);
		client.stop();
		logger.info("stop all");
	}

}
