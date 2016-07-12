package com.relativisticramblings.zeldaguide;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to check XML document for errors
 */
public class CheckGuide {

    private static NodeList getNodes(String path, Document doc) {
        try {
            return (NodeList) XPathFactory
                    .newInstance()
                    .newXPath()
                    .evaluate(path, doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int numNodes(String path, Document doc) {
        return getNodes(path, doc).getLength();
    }

    public static void checkGuide(Document doc) {

        System.out.println("Checking guide...");

        NodeList entries = doc.getElementsByTagName("entry");

        /* ENTRIES */

        System.out.print("   Checking that all <entry> elements contain at least one <description>:");
        NodeList entryNodesWithoutDescription = getNodes("/guide/gameSegments/gameSegment/entry[not(@type)][not(description)]", doc);
        assert entryNodesWithoutDescription.getLength() == 0 : "\nlacking description on <entry> element with "
                + entryNodesWithoutDescription.item(0).getAttributes().getNamedItem("id");
        System.out.println(" OK");

        System.out.print("   Checking that all descriptions end with punctuation:");
        NodeList allDescriptionNodes = getNodes("//entry/description", doc);
        for (int i=0; i<allDescriptionNodes.getLength(); i++) {
            String description = allDescriptionNodes.item(i).getTextContent();
            Element entry = (Element) allDescriptionNodes.item(i).getParentNode();
            if (!description.endsWith(".") && !description.endsWith(".)") && !description.endsWith("!") && !description.endsWith("!)") && !description.endsWith("?") && !description.endsWith("?)")) {
                System.out.print("\n      Description on entry with id=" + entry.getAttribute("id") + " does not end with punctuation");
            }
        }
        System.out.println(" OK");

        System.out.print("   Checking that all descriptions in a single entry has unique gameVariation values");
        NodeList entryNodes = getNodes("//entry", doc);
        for (int i=0; i<entryNodes.getLength(); i++) {
            List<String> gameVariationsIDsThisEntry = new ArrayList<>();
            Element entryElement = (Element) entryNodes.item(i);
            NodeList descriptionNodes = entryElement.getElementsByTagName("description");
            for (int j=0; j<descriptionNodes.getLength(); j++) {
                Element descriptionNode = (Element) descriptionNodes.item(j);
                if (!descriptionNode.hasAttribute("gameVariations")) break;
                for (String gameVariationID : descriptionNode.getAttribute("gameVariations").split("\\s*,\\s*")) {
                    assert !gameVariationsIDsThisEntry.contains(gameVariationID) :
                            "\n<entry> with id=" + entryElement.getAttribute("id") + " contains two <description> elements referencing gameVariation with id=" + gameVariationID;
                    gameVariationsIDsThisEntry.add(gameVariationID);
                }
            }
        }
        System.out.println(" OK");

        /* ITEMS */

        System.out.print("   Checking that all itemRef attributes on <entry> elements point to an existing item ID:");
        for (int i=0; i<entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            if (entry.hasAttribute("itemRef")) {
                assert 1 == numNodes("/guide/items/item[@id='" + entry.getAttribute("itemRef") + "']", doc) :
                        "\n<entry> with id=" + entry.getAttribute("id") + " references nonexistent item with id=" + entry.getAttribute("itemRef");
            }
        }
        System.out.println(" OK");

        System.out.print("   Checking that all changeNameID attributes on <entry> elements corresponds to a defined name ID:");
        for (int i=0; i<entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            if (entry.hasAttribute("changeNameID")) {
                assert 1 == numNodes("/guide/items/item[@id='" + entry.getAttribute("itemRef") + "']/name[@id='" + entry.getAttribute("changeNameID") + "']", doc) :
                        "\n<entry> with id=" + entry.getAttribute("id") + " references unknown name id=" + entry.getAttribute("changeNameID") + " on item with id=" + entry.getAttribute("itemRef");
            }
        }
        System.out.println(" OK");

        System.out.print("   Checking that all defined name IDs are used...");
        NodeList nameNodesWithID = getNodes("/guide/items/item/name[@id]", doc);
        for (int i=0; i<nameNodesWithID.getLength(); i++) {
            Element nameElementWithID = (Element) nameNodesWithID.item(i);
            Element parentItem = (Element) nameElementWithID.getParentNode();
            if (0 == numNodes("//*[@itemRef='" + parentItem.getAttribute("id") + "'][@changeNameID='" + nameElementWithID.getAttribute("id") + "']", doc)) {
                System.out.print("\n      unused name with id=" + nameElementWithID.getAttribute("id") + " on item with id=" + parentItem.getAttribute("id"));
            }

        }
        System.out.println(" OK");

        System.out.print("   Checking that all defined items are used...");
        NodeList itemNodes = getNodes("/guide/items/item", doc);
        for (int i=0; i<itemNodes.getLength(); i++) {
            Element item = (Element) itemNodes.item(i);
            boolean usedInStartNum = item.hasAttribute("startNum") && Integer.parseInt(item.getAttribute("startNum")) > 0;
            boolean usedInItemRef = 0 < numNodes("//entry[@itemRef='" + item.getAttribute("id") + "']", doc);
            boolean usedInPartsOf = false;
            if (!usedInItemRef) {
                // find out if it's used in a partOf attribute, and if that part item is then used in an itemRef
                NodeList partsOfItems = getNodes("//item[@partsOf='" + item.getAttribute("id") + "']", doc);
                if (partsOfItems.getLength() > 0) {
                    for (int j=0; j<partsOfItems.getLength(); j++) {
                        Element partItem = (Element) partsOfItems.item(j);
                        if (0 < numNodes("//entry[@itemRef='" + partItem.getAttribute("id") + "']", doc)) {
                            usedInPartsOf = true;
                            break;
                        }
                    }
                }
            }
            if (!usedInItemRef && !usedInStartNum && !usedInPartsOf) {
                System.out.print("\n      unused item with id=" + item.getAttribute("id"));
            }
        }
        System.out.println(" OK");

        /* SUMMARY LISTS */

        System.out.print("   Checking that all defined summary lists are used...");
        NodeList summaryListNodes = getNodes("/guide/summaryLists/summaryList", doc);
        for (int i=0; i<summaryListNodes.getLength(); i++) {
            Element summaryList = (Element) summaryListNodes.item(i);
            if (0 == numNodes("//*[@summaryType='" + summaryList.getAttribute("id") + "']", doc)) {
                System.out.print("\n      unused summary list with id=" + summaryList.getAttribute("id"));
            }

        }
        System.out.println(" OK");

        System.out.print("   Checking that all summary list references point to defined summary lists");
        NodeList summaryItems = getNodes("/guide/items/item[@summaryType]", doc);
        for (int i=0; i<summaryItems.getLength(); i++) {
            Element item = (Element) summaryItems.item(i);
            assert 1 == numNodes("/guide/summaryLists/summaryList[@id='" + item.getAttribute("summaryType") + "']", doc) :
                    "\nitem with id=" + item.getAttribute("id") + " points to undefined summary list with id=" + item.getAttribute("summaryType");
        }
        System.out.println(" OK");


        /* GAME VARIATIONS */

        System.out.print("   Checking that all defined gameVariations are used...");
        NodeList gameVariationNodes = getNodes("/guide/gameVariations/gameVariation", doc);
        for (int i=0; i<gameVariationNodes.getLength(); i++) {
            Element gameVariation = (Element) gameVariationNodes.item(i);
            String id = gameVariation.getAttribute("id");
            if (0 == numNodes("//*[contains(@gameVariations, '" + id + "')]", doc)) {
                System.out.print("\n      unused gameVariation with id=" + id);
            }
        }
        System.out.println(" OK");

        System.out.print("   Checking that all gameVariations attribute values correspond to a defined gameVariation...");
        NodeList nodesWithGameVariationAttr = getNodes("//*[@gameVariations]", doc);
        for (int i=0; i<nodesWithGameVariationAttr.getLength(); i++) {
            Element elementWithGameVariationAttr = (Element) nodesWithGameVariationAttr.item(i);
            String[] gameVariationIDs = elementWithGameVariationAttr.getAttribute("gameVariations").split("\\s*,\\s*");
            for (String gameVariationID : gameVariationIDs) {
                assert 1 == numNodes("/guide/gameVariations/gameVariation[@id='" + gameVariationID + "']", doc) :
                        "\nnonexistent gameVariation '{{gameVariationID}}' on <{{tagName}}> with id='{{id}}'"
                                .replace("{{gameVariationID}}", gameVariationID)
                                .replace("{{tagName}}", elementWithGameVariationAttr.getTagName())
                                .replace("{{id}}", elementWithGameVariationAttr.getAttribute("id"));
            }

        }
        System.out.println(" OK");

        System.out.print("   Checking that no gameVariation exists with id='all'...");
        assert 0 == numNodes("/guide/gameVariations/gameVariation[@id='all']", doc) :
                "\ngameVariation with id 'all' exists; this is reserved for internal use";
        System.out.println(" OK");

        System.out.print("   Checking that all gameVariations are present in entries with gameVariation-filtered descriptions...");
        // first, create a list of all game variations
        NodeList gameVariations = getNodes("/guide/gameVariations/gameVariation", doc);
        Set<String> allGameVariationIDs = new HashSet<>();
        for (int i=0; i<gameVariations.getLength(); i++) {
            Element gameVariation = (Element) gameVariations.item(i);
            allGameVariationIDs.add(gameVariation.getAttribute("id"));
        }
        // now go through relevant entries and check if all game variations are present
        NodeList entriesWithGameVariationDescriptions = getNodes("//entry[description[@gameVariations]]", doc);
        for (int i=0; i<entriesWithGameVariationDescriptions.getLength(); i++) {
            Element entry = (Element) entriesWithGameVariationDescriptions.item(i);
            // make a list of all game variations in this entry
            Set<String> gameVariationIDsThisEntry = new HashSet<>();
            NodeList descriptions = entry.getElementsByTagName("description");
            for (int j=0; j<descriptions.getLength(); j++) {
                Element description = (Element) descriptions.item(j);
                String[] gameVariationIDs = description.getAttribute("gameVariations").split("\\s*,\\s*");
                for (String gameVariationID : gameVariationIDs) {
                    gameVariationIDsThisEntry.add(gameVariationID);
                }
            }
            // all gameVariations for this entry are now added, check if they are the same
            Set<String> missing = new HashSet<>(allGameVariationIDs);
            missing.removeAll(gameVariationIDsThisEntry);
            assert missing.isEmpty() :
                    "\n<entry> with id=" + entry.getAttribute("id") + " lacks descriptions for the following gameVariations: " + missing.toString();
        }
        System.out.println(" OK");


        /* FILTERS */

        System.out.print("   Checking that all defined filters are used...");
        NodeList filterNodes = getNodes("/guide/filters/filter", doc);
        for (int i=0; i<filterNodes.getLength(); i++) {
            Element filter = (Element) filterNodes.item(i);
            if (filter.getAttribute("id").equals("oldSummaryItem")) continue;
            if (0 == numNodes("//*[contains(@filters, '" + filter.getAttribute("id") + "')]", doc)) {
                System.out.print("\n      unused filter: " + filter.getAttribute("id"));
            }
        }
        System.out.println(" OK");

        System.out.print("   Checking that all filter attribute values correspond to a defined filter...");
        NodeList filteredElements = getNodes("//*[@filters]", doc);
        for (int i=0; i<filteredElements.getLength(); i++) {
            Element filteredElement = (Element) filteredElements.item(i);
            String[] filters = filteredElement.getAttribute("filters").split("\\s*,\\s*");
            for (String filterID : filters) {
                assert 1 == numNodes("/guide/filters/filter[@id='" + filterID + "']", doc) :
                        "\nnonexistent filter '{{filterID}}' on <{{tagName}}> with id='{{id}}'"
                                .replace("{{filterID}}", filterID)
                                .replace("{{tagName}}", filteredElement.getTagName())
                                .replace("{{id}}", filteredElement.getAttribute("id"));
            }

        }
        System.out.println(" OK");

        System.out.print("   Checking that filter 'oldSummaryItem' is unused...");
        assert 0 == numNodes("//*[contains(@filters, 'oldSummaryItem')]", doc) :
                "\nfilter 'oldSummaryItem' should not be directly used in XML file";
        System.out.println(" OK");

        /* GAME SEGMENTS */

        System.out.print("   Checking that all <gameSegment> elements contains a <heading>:");
        assert 0 == numNodes("/guide/gameSegments/gameSegment[not(heading)]", doc) :
                "\nat least one <gameSegment> does not have a <heading>";
        System.out.println(" OK");


        /* IDs */

        System.out.print("   Checking that all entries have an ID...");
        assert 0 == numNodes("/guide/gameSegments/gameSegment/entry[not(@type='subheading')][not(@id)]", doc) :
                "\nat least one <entry> lacks an id";
        System.out.println(" OK");

        System.out.print("   Checking that all gameSegment elements have an ID...");
        assert 0 == numNodes("/guide/gameSegments/gameSegment[not(@id)]", doc) :
                "\nat least one <gameSegment> lacks an id";
        System.out.println(" OK");

        System.out.print("   Checking that all IDs are unique across entries and game segments...");
        NodeList idElements = getNodes("//*[self::entry or self::gameSegment][not(@type='subheading')]", doc);
        for (int i=0; i<idElements.getLength(); i++) {
            Element idElement = (Element) idElements.item(i);
            String id = idElement.getAttribute("id");
            assert 1 == numNodes("//*[self::entry or self::gameSegment][not(@type='subheading')][@id='" + id + "']", doc) :
                    "\nduplicate id across <entry> and <gameSegment> elements: " + id;
        }

        System.out.println(" OK");

        System.out.print("   Checking that all entries with filter 'mandatory' has checkbox='false'...");
        NodeList mandatoryElements = getNodes("//entry[contains(@filters, 'mandatory')][not(@type)]", doc);
        for (int i=0; i<mandatoryElements.getLength(); i++) {
            Element entry = (Element) mandatoryElements.item(i);
            if (!entry.hasAttribute("checkbox") || Boolean.parseBoolean(entry.getAttribute("checkbox"))) {
                System.out.print("\n      Entry with id=" + entry.getAttribute("id") + " is mandatory, but lacks checkbox='false'");
            }
        }
        System.out.println(" OK");

        System.out.print("   Checking that all entries with filter 'mandatory' has text 'Mandatory.'...");
        NodeList mandatoryElementsWithoutProperDescription = getNodes("//entry[contains(@filters, 'mandatory')]/description[not(contains(text(), 'Mandatory'))]", doc);
        for (int i=0; i<mandatoryElementsWithoutProperDescription.getLength(); i++) {
            Element entry = (Element) mandatoryElementsWithoutProperDescription.item(i).getParentNode();
            System.out.print("\n      Entry with id=" + entry.getAttribute("id") + " is mandatory, but description does not contain 'Mandatory'");
        }
        System.out.println(" OK");

        System.out.print("   Checking that all entries with text 'Mandatory.' has filter 'mandatory'...");
        NodeList mandatoryElementsWithoutProperFilter = getNodes("//entry[not(contains(@filters, 'mandatory'))]/description[text() = 'Mandatory.']", doc);
        for (int i=0; i<mandatoryElementsWithoutProperFilter.getLength(); i++) {
            Element entry = (Element) mandatoryElementsWithoutProperFilter.item(i).getParentNode();
            System.out.print("\n      Entry with id=" + entry.getAttribute("id") + " has text 'Mandatory.', but lacks filter 'mandatory'");
        }
        System.out.println(" OK");

        System.out.print("   Checking that all entries without itemRef has checkbox='false'...");
        NodeList entriesWithoutItemRef = getNodes("//entry[not(@itemRef)][not(@type)]", doc);
        for (int i=0; i<entriesWithoutItemRef.getLength(); i++) {
            Element entry = (Element) entriesWithoutItemRef.item(i);
            if (!entry.hasAttribute("checkbox") || Boolean.parseBoolean(entry.getAttribute("checkbox"))) {
                System.out.print("\n      Entry with id=" + entry.getAttribute("id") + " has no itemRef, but lacks checkbox='false'");
            }
        }
        System.out.println(" OK");

        System.out.print("   Checking that crossreferenced entries in <appendToName> elements contain backreferences...");
        NodeList appendToNameNodes = getNodes("//entry/appendToName", doc);
        Pattern pattern = Pattern.compile("(?<=\\{\\{)([^\\}]+)(?=\\}\\})");
        for (int i=0; i<appendToNameNodes.getLength(); i++) {
            Element entryElement = (Element) appendToNameNodes.item(i).getParentNode();
            String id = entryElement.getAttribute("id");

            String text = appendToNameNodes.item(i).getTextContent();

            Matcher matcher = pattern.matcher(text);
            matcher.find();
            if (matcher.hitEnd()) {
                continue;
            }
            String referencedID = matcher.group(1);

            NodeList referencedNodesAppendToName = getNodes("//entry[@id='" + referencedID + "']/appendToName", doc);

            if (referencedNodesAppendToName.getLength() == 0) {
                System.out.print("\n      Entry with id=" + id + " references " + referencedID + " in <appendToName>, but referenced entry does not provide a backreference in its own <appendToName>");
            } else {
                String referencedText = referencedNodesAppendToName.item(0).getTextContent();
                Matcher matcher2 = pattern.matcher(referencedText);
                matcher2.find();
                String backreferencedID = matcher2.group(1);
                if (!backreferencedID.equals(id)) {
                    System.out.print("\n      Entry with id=" + id + " references " + referencedID + " in <appendToName>, but referenced entry does not provide a backreference in its own <appendToName>");
                }
            }
        }
        System.out.println(" OK");

    }

}
