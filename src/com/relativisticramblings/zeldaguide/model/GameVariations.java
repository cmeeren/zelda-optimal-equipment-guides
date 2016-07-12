package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds a map of GameVariation objects keyed by their IDs
 */
public class GameVariations implements Iterable<GameVariation> {

    private Map<String, GameVariation> gameVariations = new LinkedHashMap<>();

    public GameVariations(Document doc) {

        NodeList variationElements = doc.getElementsByTagName("gameVariation");

        // initialize SummaryList object for each <summarySection> element and add to map
        for (int i=0; i<variationElements.getLength(); i++) {
            Element variationElement = (Element) variationElements.item(i);
            String id = variationElement.getAttribute("id");
            GameVariation gameVariation = new GameVariation(variationElement);
            gameVariations.put(id, gameVariation);
        }

    }

    public GameVariation getByID(String id) {
        return gameVariations.get(id);
    }

    public Collection<GameVariation> values() {
        return gameVariations.values();
    }

    public boolean isEmpty() {
        return gameVariations.isEmpty();
    }

    public Iterator<GameVariation> iterator() {
        return gameVariations.values().iterator();
    }

    public boolean hasID(String id) {
        return gameVariations.containsKey(id);
    }

}
