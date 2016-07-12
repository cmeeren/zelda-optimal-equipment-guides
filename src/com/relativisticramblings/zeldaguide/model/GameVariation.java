package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Element;

/**
 * Class to hold a variation of the game (e.g. Wii U, GameCube, Hero Mode, etc.)
 */
public class GameVariation {

    public String id;
    public String title;

    public GameVariation(Element gameVariationElement) {
        id = gameVariationElement.getAttribute("id");
        title = gameVariationElement.getTextContent();
    }

}
