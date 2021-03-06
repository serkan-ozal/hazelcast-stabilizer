package com.hazelcast.stabilizer.tests.map.helpers;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class MapOpperationsCount implements DataSerializable {

    public AtomicLong putCount = new AtomicLong(0);
    public AtomicLong putAsyncCount = new AtomicLong(0);

    public AtomicLong putTTLCount = new AtomicLong(0);
    public AtomicLong putAsyncTTLCount = new AtomicLong(0);

    public AtomicLong putTransientCount = new AtomicLong(0);
    public AtomicLong putIfAbsentCount = new AtomicLong(0);

    public AtomicLong replaceCount = new AtomicLong(0);

    public AtomicLong getCount = new AtomicLong(0);
    public AtomicLong getAsyncCount = new AtomicLong(0);

    public AtomicLong removeCount = new AtomicLong(0);
    public AtomicLong removeAsyncCount = new AtomicLong(0);

    public AtomicLong deleteCount = new AtomicLong(0);
    public AtomicLong destroyCount = new AtomicLong(0);

    public MapOpperationsCount(){}

    public void add(MapOpperationsCount c){
        putCount.addAndGet(c.putCount.get());
        putAsyncCount.addAndGet(c.putAsyncCount.get());

        putTTLCount.addAndGet(c.putTTLCount.get());
        putAsyncTTLCount.addAndGet(c.putAsyncTTLCount.get());

        putTransientCount.addAndGet(c.putTransientCount.get());
        putIfAbsentCount.addAndGet(c.putIfAbsentCount.get());

        replaceCount.addAndGet(c.replaceCount.get());

        getCount.addAndGet(c.getCount.get());
        getAsyncCount.addAndGet(c.getAsyncCount.get());

        removeCount.addAndGet(c.removeCount.get());
        removeAsyncCount.addAndGet(c.removeAsyncCount.get());

        deleteCount.addAndGet(c.deleteCount.get());
        destroyCount.addAndGet(c.destroyCount.get());
    }

    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(putCount);
        out.writeObject(putAsyncCount);

        out.writeObject(putTTLCount);
        out.writeObject(putAsyncTTLCount);

        out.writeObject(putTransientCount);
        out.writeObject(putIfAbsentCount);

        out.writeObject(replaceCount);

        out.writeObject(getCount);
        out.writeObject(getAsyncCount);

        out.writeObject(removeCount);
        out.writeObject(removeAsyncCount);

        out.writeObject(deleteCount);
        out.writeObject(destroyCount);
    }

    public void readData(ObjectDataInput in) throws IOException {

        putCount = in.readObject();
        putAsyncCount = in.readObject();

        putTTLCount = in.readObject();
        putAsyncTTLCount = in.readObject();

        putTransientCount = in.readObject();
        putIfAbsentCount = in.readObject();

        replaceCount = in.readObject();

        getCount = in.readObject();
        getAsyncCount = in.readObject();

        removeCount = in.readObject();
        removeAsyncCount = in.readObject();

        deleteCount = in.readObject();
        destroyCount = in.readObject();
    }

    @Override
    public String toString() {
        return "MapOpperationsCount{" +
                "putCount=" + putCount +
                ", putAsyncCount=" + putAsyncCount +
                ", putTTLCount=" + putTTLCount +
                ", putAsyncTTLCount=" + putAsyncTTLCount +
                ", putTransientCount=" + putTransientCount +
                ", putIfAbsentCount=" + putIfAbsentCount +
                ", replaceCount=" + replaceCount +
                ", getCount=" + getCount +
                ", getAsyncCount=" + getAsyncCount +
                ", removeCount=" + removeCount +
                ", removeAsyncCount=" + removeAsyncCount +
                ", deleteCount=" + deleteCount +
                ", destroyCount=" + destroyCount +
                '}';
    }
}