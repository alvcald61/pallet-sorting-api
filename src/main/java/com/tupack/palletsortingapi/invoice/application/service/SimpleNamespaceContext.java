package com.tupack.palletsortingapi.invoice.application.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext {

    private final Map<String, String> prefixToUri;

    public SimpleNamespaceContext(Map<String, String> prefixToUri) {
        this.prefixToUri = Map.copyOf(prefixToUri);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return prefixToUri.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return prefixToUri.entrySet().stream()
            .filter(e -> e.getValue().equals(namespaceURI))
            .map(Map.Entry::getKey)
            .findFirst().orElse(null);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return List.of(getPrefix(namespaceURI)).iterator();
    }
}
