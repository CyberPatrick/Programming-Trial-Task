package ee.taltech.food.mapper;

import ee.taltech.food.entities.StationEntity;
import org.mapstruct.Mapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class StationMapper {
    public List<StationEntity> convertXmlDocToEntities(Document xml) {
        List<StationEntity> stationEntities = new ArrayList<>();

        var nodes = xml.getElementsByTagName("station");
        Date timestamp = new Date();
        // Handle timestamp separately
        try {
            Node observationTag = xml.getElementsByTagName("observations").item(0);
            timestamp = new Date(
                    Long.valueOf(observationTag.getAttributes().getNamedItem("timestamp").getNodeValue()) * 1000 // Because timestamp in seconds
            );
        } catch (Exception ex) {
            System.out.println("Error when tried to get xml timestamp" + ex);
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element station = (Element) node;
                String stationName = parseField(
                        station.getElementsByTagName("name").item(0).getTextContent(), String.class);
                if (Objects.isNull(stationName)) continue;
                switch (stationName) {
                    case "Tallinn-Harku", "Tartu-Tõravere", "Pärnu":
                        break;
                    default:
                        continue;
                }

                StationEntity newStation = new StationEntity();
                try {
                    newStation.setName(stationName);
                    newStation.setWmo(parseField(
                            station.getElementsByTagName("wmocode").item(0).getTextContent(), Integer.class));
                    newStation.setAirTemperature(parseField(
                            station.getElementsByTagName("airtemperature").item(0).getTextContent(), Float.class));
                    newStation.setWindSpeed(parseField(
                            station.getElementsByTagName("windspeed").item(0).getTextContent(), Float.class));
                    newStation.setPhenomenon(parseField(
                            station.getElementsByTagName("phenomenon").item(0).getTextContent(), String.class));
                    newStation.setTimestamp(timestamp);

                    stationEntities.add(newStation);
                } catch (NullPointerException ex) {
                    System.out.println("Invalid weather xml " + ex);
                }
            }
        }
        return stationEntities;
    }

    private static <T> T parseField(String field, Class<T> classType) {
        if (field.isEmpty()) return null;
        if (classType.equals(Integer.class)) {
            return (T) Integer.valueOf(field);
        } else if (classType.equals(Float.class)) {
            return (T) Float.valueOf(field);
        } else if (classType.equals(String.class)) {
            return (T) field;
        } else return null;
    }
}
