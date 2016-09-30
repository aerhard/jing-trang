package com.thaiopensource.suggest.relaxng.sax;

// copy of com.thaiopensource.relaxng.sax.Context with additional getNsUriMap() method

import com.thaiopensource.relaxng.match.MatchContext;
import com.thaiopensource.relaxng.parse.sax.DtdContext;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

public class Context extends DtdContext implements MatchContext {
  protected PrefixMapping prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);

  public Context() {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    prefixMapping = new PrefixMapping(prefix, "".equals(uri) ? null : uri, prefixMapping);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    prefixMapping = prefixMapping.getPrevious();
  }

  public String getBaseUri() {
    return null;
  }

  protected static final class PrefixMapping {
    private final String prefix;
    // null for undeclaring
    private final String namespaceURI;
    private final PrefixMapping previous;

    PrefixMapping(String prefix, String namespaceURI, PrefixMapping prev) {
      this.prefix = prefix;
      this.namespaceURI = namespaceURI;
      this.previous = prev;
    }

    PrefixMapping getPrevious() {
      return previous;
    }
  }

  public String resolveNamespacePrefix(String prefix) {
    PrefixMapping tem = prefixMapping;
    do {
      if (tem.prefix.equals(prefix))
        return tem.namespaceURI;
      tem = tem.previous;
    } while (tem != null);
    return null;
  }

  public void reset() {
    prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);
    clearDtdContext();
  }

  public String getPrefix(String namespaceURI) {
    PrefixMapping tem = prefixMapping;
    do {
      if (namespaceURI.equals(tem.namespaceURI) &&
          tem.namespaceURI == resolveNamespacePrefix(tem.prefix))
        return tem.prefix;
      tem = tem.previous;
    } while (tem != null);
    return null;
  }

  public Map<String, String> getElementNsPrefixMap() {
    Map<String, String> nsPrefixMap = new HashMap<String, String>();
    PrefixMapping tem = prefixMapping;
    do {
      if (!WellKnownNamespaces.XML.equals(tem.namespaceURI) &&
          !nsPrefixMap.containsValue(tem.namespaceURI) &&
          !nsPrefixMap.containsKey(tem.prefix)) {
        nsPrefixMap.put(tem.namespaceURI, tem.prefix);
      }
      tem = tem.previous;
    } while (tem != null);

    if (!nsPrefixMap.containsValue("")) {
      nsPrefixMap.put(null, "");
    }

    return nsPrefixMap;
  }

  public Map<String, String> getAttributeNsPrefixMap() {
    Map<String, String> nsPrefixMap = new HashMap<String, String>();
    PrefixMapping tem = prefixMapping;
    do {
      if (!"".equals(tem.prefix) &&
          !nsPrefixMap.containsValue(tem.namespaceURI) &&
          !nsPrefixMap.containsKey(tem.prefix)) {
        nsPrefixMap.put(tem.namespaceURI, tem.prefix);
      }
      tem = tem.previous;
    } while (tem != null);

    nsPrefixMap.put(null, "");

    return nsPrefixMap;
  }
}
