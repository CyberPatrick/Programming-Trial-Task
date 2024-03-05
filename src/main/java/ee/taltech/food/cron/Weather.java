package ee.taltech.food.cron;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

@Component
public class Weather {
    private static final String API_URL = "https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";
//    @Scheduled(cron = "15 * * * *")
    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 0)
    public void getWeatherData() throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new URL(API_URL).openStream());
        System.out.println(doc.getXmlVersion());
    }
}