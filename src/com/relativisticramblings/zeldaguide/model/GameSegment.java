package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Element;

/**
 * Represents a <gameSegment> element and contains a heading and an Entries object
 */
public class GameSegment {

    public String id;
    public String heading;
    public Entries entries;

    public GameSegment(Element gameSegmentElement) {
        id = gameSegmentElement.getAttribute("id");
        heading = gameSegmentElement.getElementsByTagName("heading").item(0).getTextContent();
        entries = new Entries(gameSegmentElement);
    }

}
