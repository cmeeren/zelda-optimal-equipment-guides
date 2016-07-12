package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds all GameSegment objects in the guide
 */
public class GameSegments implements Iterable<GameSegment> {

    private List<GameSegment> gameSegments = new ArrayList<>();

    public GameSegments(Document doc) {

        NodeList gameSegmentNodes = doc.getElementsByTagName("gameSegment");
        for (int i=0; i<gameSegmentNodes.getLength(); i++) {
            Element gameSegmentElement = (Element) gameSegmentNodes.item(i);
            gameSegments.add(new GameSegment(gameSegmentElement));
        }

    }

    public Iterator<GameSegment> iterator() {
        return gameSegments.iterator();
    }

    /**
     * Returns the entry with the given ID, or null if it doesn't exist
     */
    public Entry getEntryByID(String entryID) {
        for (GameSegment gameSegment : gameSegments) {
            for (Entry entry : gameSegment.entries) {
                if (entry.id != null && entry.id.equals(entryID)) {
                    return entry;
                }
            }
        }
        return null;
    }



}
