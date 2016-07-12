package com.relativisticramblings.zeldaguide.view;

import com.relativisticramblings.zeldaguide.CheckGuide;
import com.relativisticramblings.zeldaguide.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Subclasses of this class are used for actually writing the guide, in some output form or another.
 * Subclasses must implement the getGuide method which should return the complete output string for the whole guide.
 */
public abstract class GuideWriter {

    Items items;
    GameSegments gameSegments;
    SummaryLists summaryLists;
    GameVariations gameVariations;
    Filters filters;
    String guideDescription;
    String guideID;
    Map<String, String> crossReferences = new HashMap<>();  // entry ID -> text to replace cross-reference with

    GuideWriter(String fileIn) {

        Document doc = getDocument(fileIn);

        CheckGuide.checkGuide(doc);

        items = new Items(doc);
        gameSegments = new GameSegments(doc);
        summaryLists = new SummaryLists(doc);
        gameVariations = new GameVariations(doc);
        filters = new Filters(doc);

        // description to show in the intro section
        NodeList guideDescriptionNodes = doc.getElementsByTagName("guideDescription");
        if (guideDescriptionNodes.getLength() != 0) {
            Element guideDescriptionElement = (Element) guideDescriptionNodes.item(0);
            guideDescription = guideDescriptionElement.getTextContent();
        }

        // guide ID
        NodeList guideIDNodes = doc.getElementsByTagName("guideID");
        if (guideIDNodes.getLength() != 0) {
            Element guideIDElement = (Element) guideIDNodes.item(0);
            guideID = guideIDElement.getTextContent();
        }

        // add all items to their associated summary list
        for (Item item : items) {
            if (item.summaryType != null && !item.summaryType.trim().equals("")) {
                summaryLists.getByID(item.summaryType).addItem(item.id);
            }
        }

    }

    /**
     * Create the guide.
     *
     * @return the complete guide to be written to a file
     */
    public abstract String getGuide();

    /**
     * Write the guide to a file.
     * @param fileOut path to file
     */
    public void writeGuideToFile(String fileOut) {
        try {
            PrintWriter writer = new PrintWriter(fileOut, "UTF-8");
            System.out.println("Creating guide...");
            writer.print(getGuide());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse an XML file.
     * @param fileIn XML file to read
     * @return XML document
     */
    private static Document getDocument(String fileIn) {
        File inputFile = new File(fileIn);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the number of the first item added by the entry (which must have an itemRef)
     */
    int getItemCountStart(Entry entry) {
        return items.get(entry.itemRef).count + 1;
    }

    /**
     * Returns the number of the last item added by the entry (which must have an itemRef)
     */
    int getItemCountEnd(Entry entry) {
        return items.get(entry.itemRef).count + entry.add;
    }

    /**
     * Returns a map of strings to replace keyed by the entry ID it's referring to
     */
    Map<String, String> getCrossReferences(String description) {

        // The referenced entries keyed by the strings that should be replaced
        Map<String, String> crossReferences = new HashMap<>();

        String pattern = "\\{\\{([^}]+)}}";  // characters surrounded by two braces, e.g. {{some-id-here}}
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(description);
        while (m.find()) {
            String toReplace = m.group(0);  // the whole matched string
            String referencedEntryID = m.group(1);
            crossReferences.put(referencedEntryID, toReplace);
        }
        return crossReferences;

    }


    /**
     * Returns the correct singular/plural name of the item added by the entry (which must have an itemRef)
     */
    String getItemName(Entry entry) {
        Item item = items.get(entry.itemRef);
        if (entry.hasNameOverride()) {
            // the entry has its own <name> tag, use this
            return entry.nameOverride;
        } else {
            if (!item.unique && (entry.add > 1 || (entry.add == 0 && item.count > 1))) {
                // if item is unique, always use singular name. Otherwise, use plural if:
                //  - the entry is adding more than one item OR
                //  - the entry is adding nothing but we have more than one of that item
                //    (such as when changing names, e.g. upgrading bomb bags in Twilight Princess)
                return item.name.plural;
            } else {
                // the entry is only adding one item, use singular
                return item.name.singular;
            }
        }
    }

    /**
     * Returns the correct item name to use in summaries
     */
    String getSummaryItemName(Item item) {

        // default: singular name, no count or parts (e.g. "Boomerang" instead of "1 Boomerang")
        String count = "";
        String parts = "";
        String name = item.name.singular;

        // for non-unique items...
        if (!item.unique) {
            // ...display current item count (e.g. "1 Heart")
            count = item.count + " ";
            // ...use plural name if we have more than one item or any parts (e.g. "5 Hearts")
            if (item.count != 1 || item.currentParts > 0) name = item.name.plural;
            // ...display parts if present (e.g. "5 2/5 hearts")
            if (item.currentParts > 0) parts = item.currentParts + "/" + item.numberOfParts + " ";
        }

        return count + parts + name;
    }

    /**
     * Increments the count of the item added by the entry
     */
    void incrementItemCount(Entry entry) {
        Item item = items.get(entry.itemRef);
        item.count += entry.add;
        // if part of another item, increment that too
        if (item.isPart()) items.get(item.partsOf).addParts(entry.add);
    }

    /**
     * Adds the item to its corresponding summary list
     */
    void addToSummaryList(Item item) {
        // if item is a part of another item, we use "mother" item instead
        if (item.isPart()) {
            item = items.get(item.partsOf);
        }
        if (item.isInSummaryList()) {
            summaryLists.getByID(item.summaryType).addItem(item.id);
        }
    }

}
