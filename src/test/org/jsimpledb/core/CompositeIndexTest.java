
/*
 * Copyright (C) 2014 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.jsimpledb.core;

import java.io.ByteArrayInputStream;

import org.jsimpledb.TestSupport;
import org.jsimpledb.kv.simple.SimpleKVDatabase;
import org.jsimpledb.schema.SchemaModel;
import org.jsimpledb.tuple.Tuple2;
import org.jsimpledb.tuple.Tuple3;
import org.testng.annotations.Test;

public class CompositeIndexTest extends TestSupport {

    @Test
    public void testCompositeIndex2() throws Exception {

        final SimpleKVDatabase kvstore = new SimpleKVDatabase();
        final Database db = new Database(kvstore);

        final SchemaModel schema1 = SchemaModel.fromXML(new ByteArrayInputStream((
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<Schema formatVersion=\"1\">\n"
          + "  <ObjectType name=\"Foo\" storageId=\"10\">\n"
          + "    <SimpleField name=\"i\" type=\"int\" storageId=\"11\"/>\n"
          + "    <ReferenceField name=\"r\" storageId=\"12\"/>\n"
          + "    <CompositeIndex storageId=\"20\" name=\"ir\">\n"
          + "      <IndexedField storageId=\"11\"/>\n"
          + "      <IndexedField storageId=\"12\"/>\n"
          + "    </CompositeIndex>\n"
          + "  </ObjectType>\n"
          + "</Schema>\n"
          ).getBytes("UTF-8")));

        Transaction tx = db.createTransaction(schema1, 1, true);

        ObjId id1 = new ObjId("0a11111111111111");
        ObjId id2 = new ObjId("0a22222222222222");
        ObjId id3 = new ObjId("0a33333333333333");
        ObjId id4 = new ObjId("0a44444444444444");
        ObjId id5 = new ObjId("0a55555555555555");

        tx.create(id1);
        tx.create(id2);
        tx.create(id3);
        tx.create(id4);
        tx.create(id5);

        tx.writeSimpleField(id1, 11, 555, true);
        tx.writeSimpleField(id1, 12, id3, true);

        tx.writeSimpleField(id2, 11, 555, true);
        tx.writeSimpleField(id2, 12, id4, true);

        tx.writeSimpleField(id3, 11, 666, true);
        tx.writeSimpleField(id3, 12, id3, true);

        tx.writeSimpleField(id4, 11, 666, true);
        tx.writeSimpleField(id4, 12, id4, true);

        // id5 same as id4
        tx.writeSimpleField(id5, 11, 666, true);
        tx.writeSimpleField(id5, 12, id4, true);

        final CoreIndex2<?, ?, ObjId> index = tx.queryCompositeIndex2(20);
        TestSupport.checkSet(index.asSet(), buildSet(
          new Tuple3<Integer, ObjId, ObjId>(555, id3, id1),
          new Tuple3<Integer, ObjId, ObjId>(555, id4, id2),
          new Tuple3<Integer, ObjId, ObjId>(666, id3, id3),
          new Tuple3<Integer, ObjId, ObjId>(666, id4, id4),
          new Tuple3<Integer, ObjId, ObjId>(666, id4, id5)));

        TestSupport.checkMap(index.asMap(), buildMap(
          new Tuple2<Integer, ObjId>(555, id3), buildSet(id1),
          new Tuple2<Integer, ObjId>(555, id4), buildSet(id2),
          new Tuple2<Integer, ObjId>(666, id3), buildSet(id3),
          new Tuple2<Integer, ObjId>(666, id4), buildSet(id4, id5)));

        TestSupport.checkSet(index.asMapOfIndex().get(555).asSet(), buildSet(
          new Tuple2<ObjId, ObjId>(id3, id1),
          new Tuple2<ObjId, ObjId>(id4, id2)));

        TestSupport.checkSet(index.asMapOfIndex().get(666).asSet(), buildSet(
          new Tuple2<ObjId, ObjId>(id3, id3),
          new Tuple2<ObjId, ObjId>(id4, id4),
          new Tuple2<ObjId, ObjId>(id4, id5)));

        TestSupport.checkSet(index.asIndex().asSet(), buildSet(
          new Tuple2<Integer, ObjId>(555, id3),
          new Tuple2<Integer, ObjId>(555, id4),
          new Tuple2<Integer, ObjId>(666, id3),
          new Tuple2<Integer, ObjId>(666, id4)));

        tx.rollback();
    }
}

