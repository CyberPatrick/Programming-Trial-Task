package ee.taltech.food.cron;

import ee.taltech.food.mapper.StationMapper;
import ee.taltech.food.repositories.StationRepository;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class Weather {
    private static final String API_URL = "https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";
    private final StationRepository repository;
    private final StationMapper mapper;

    /**
     * Get weather date once every hour, 15 minutes after a full hour (HH:15:00)
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Scheduled(cron = "* 15 * * * *")
    public void getWeatherData() throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new URL(API_URL).openStream());
        repository.saveAll(mapper.convertXmlDocToEntities(doc));
    }
}
