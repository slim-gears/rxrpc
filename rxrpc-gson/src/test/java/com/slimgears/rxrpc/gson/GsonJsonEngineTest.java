package com.slimgears.rxrpc.gson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GsonJsonEngineTest {
    private GsonJsonEngine engine;

    public static class DummyItem {
        public String stringVal;
        public boolean boolVal;
        public Object nullVal = null;

        public DummyItem() {}
        public DummyItem(String stringVal, boolean boolVal) {
            this.stringVal = stringVal;
            this.boolVal = boolVal;
        }
    }

    public static class DummyClass {
        public int intVal;
        public String stringVal;
        public DummyItem[] arrayVal;

        public DummyClass() {

        }

        public DummyClass(int intVal, String stringVal, DummyItem... array) {
            this.intVal = intVal;
            this.stringVal = stringVal;
            this.arrayVal = array;
        }
    }

    @Before
    public void setUp() {
        this.engine = new GsonJsonEngine();
    }

    @Test
    public void testEncodeDecode() {
        DummyClass source = new DummyClass(2, "test", new DummyItem("dummyTest", true));
        String str = this.engine.encodeString(source);
        DummyClass dest = this.engine.decodeString(str, DummyClass.class);

        Assert.assertEquals(source.intVal, dest.intVal);
        Assert.assertEquals(source.stringVal, dest.stringVal);
        Assert.assertEquals(source.arrayVal.length, dest.arrayVal.length);
        Assert.assertEquals(source.arrayVal[0].stringVal, dest.arrayVal[0].stringVal);
        Assert.assertEquals(source.arrayVal[0].boolVal, dest.arrayVal[0].boolVal);
        Assert.assertEquals(source.arrayVal[0].nullVal, dest.arrayVal[0].nullVal);
    }
}