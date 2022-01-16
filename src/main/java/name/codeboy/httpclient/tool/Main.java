package name.codeboy.httpclient.tool;

import name.codeboy.httpclient.HttpClient;
import name.codeboy.httpclient.message.GlobalConfig;
import name.codeboy.httpclient.message.HttpRequestMessage;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Scanner;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    @SuppressWarnings({"deprecation", "static-access"})
    public static void main(String[] args) throws MalformedURLException, InterruptedException, FileNotFoundException {

        GlobalConfig globalConfig = null;
        try {
            globalConfig = new GlobalConfig(args);
        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            System.exit(-1);
        }

        HttpClient client = new HttpClient(globalConfig);
        long totalNum = 0;
        for (int i = 0; i < globalConfig.fileReuseCount; i++) {
            Scanner sc = new Scanner(new File(globalConfig.urlFile));
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                client.sendMessage(new HttpRequestMessage(line, globalConfig));
                totalNum++;
            }
            sc.close();
            logger.info("msg add queue loop {} size is {}", i, client.todoMsg());
        }
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
