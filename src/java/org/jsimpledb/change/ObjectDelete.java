
/*
 * Copyright (C) 2014 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.jsimpledb.change;

import org.jsimpledb.JObject;
import org.jsimpledb.JTransaction;
import org.jsimpledb.core.ObjId;

/**
 * Change notification that indicates an object has been deleted.
 *
 * @param <T> the type of the object that was deleted
 */
public class ObjectDelete<T> extends Change<T> {

    /**
     * Constructor.
     *
     * @param jobj Java model object that was deleted
     * @throws IllegalArgumentException if {@code jobj} is null
     */
    public ObjectDelete(T jobj) {
        super(jobj);
    }

    @Override
    public <R> R visit(ChangeSwitch<R> target) {
        return target.caseObjectDelete(this);
    }

    @Override
    public void apply(JTransaction tx, ObjId id) {
        tx.delete(id);
    }

// Object

    @Override
    public String toString() {
        return "ObjectDelete[objId=" + ((JObject)this.getObject()).getObjId() + "]";
    }
}
