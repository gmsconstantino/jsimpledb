
/*
 * Copyright (C) 2014 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.jsimpledb.schema;

import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jsimpledb.core.CollectionField;
import org.jsimpledb.util.Diffs;

/**
 * A collection field in one version of a {@link SchemaObjectType}.
 */
public abstract class CollectionSchemaField extends ComplexSchemaField {

    private SimpleSchemaField elementField;

    public SimpleSchemaField getElementField() {
        return this.elementField;
    }
    public void setElementField(SimpleSchemaField elementField) {
        this.elementField = elementField;
    }

    @Override
    public Map<String, SimpleSchemaField> getSubFields() {
        return Collections.<String, SimpleSchemaField>singletonMap(CollectionField.ELEMENT_FIELD_NAME, this.elementField);
    }

    @Override
    void readSubElements(XMLStreamReader reader, int formatVersion) throws XMLStreamException {
        this.elementField = this.readSubField(reader, formatVersion, CollectionField.ELEMENT_FIELD_NAME);
        this.expectClose(reader);
    }

// DiffGenerating

    protected Diffs differencesFrom(CollectionSchemaField that) {
        final Diffs diffs = new Diffs(super.differencesFrom(that));
        final Diffs elementDiffs = this.elementField.differencesFrom(that.elementField);
        if (!elementDiffs.isEmpty())
            diffs.add("changed element field", elementDiffs);
        return diffs;
    }

// Object

    @Override
    public String toString() {
        return super.toString() + (this.elementField != null ? " with element " + this.elementField : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!super.equals(obj))
            return false;
        final CollectionSchemaField that = (CollectionSchemaField)obj;
        return this.elementField != null ? this.elementField.equals(that.elementField) : that.elementField == null;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ (this.elementField != null ? this.elementField.hashCode() : 0);
    }

// Cloneable

    @Override
    public CollectionSchemaField clone() {
        final CollectionSchemaField clone = (CollectionSchemaField)super.clone();
        clone.elementField = this.elementField.clone();
        return clone;
    }
}

