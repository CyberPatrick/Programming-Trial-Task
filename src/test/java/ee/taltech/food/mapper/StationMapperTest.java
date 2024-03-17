package ee.taltech.food.mapper;

import ee.taltech.food.entities.StationEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class StationMapperTest {
    private static final String API_URL = "https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";
    private static StationMapper stationMapper = Mappers.getMapper(StationMapper.class);
    @Test
    void convertXmlDocToEntities_RegularXml_OnlyPhenomenonFieldCanBeNull() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new URL(API_URL).openStream());

        var result = stationMapper.convertXmlDocToEntities(doc);

        assertEquals(3, result.size());
        for (StationEntity station : result) {
            assertNotNull(station.getWmo(), station.toString());
            assertNotNull(station.getAirTemperature(), station.toString());
            assertNotNull(station.getWindSpeed(), station.toString());
            assertNotNull(station.getTimestamp(), station.toString());
        }
    }
}