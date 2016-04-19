package com.stonecraft.datastore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class
 * <p/>
 * Author: michaeldelaney
 * Created: 19/04/16
 */
public class ResultSet <T> implements Iterable<T>{
    private List<Object> myList;

    public ResultSet(List<Object> list) {
        myList = list;
    }

    public int size(){
        return myList.size();
    }

    public T get(int location){
        return (T)myList.get(location);
    }

    public boolean isEmpty(){
        return myList.isEmpty();
    }

    public boolean contains(Object object){
        return myList.contains(object);
    }

    @Override
    public boolean equals(Object o) {
        return myList.equals(o);
    }

    public List<T> getMutableList() {
        ArrayList<T> mutableList = new ArrayList<>();
        for(T object : this){
            mutableList.add(object);
        }
        return mutableList;
    }

    @Override
    public Iterator<T> iterator() {
        return new ResultsetIterator();
    }

    private class ResultsetIterator implements Iterator<T> {

        private int myCount = 0;

        @Override
        public boolean hasNext() {
            return myList.size() < myCount;
        }

        @Override
        public T next() {
            int count = myCount;
            if(count >= myList.size()){
                throw new NoSuchElementException();
            }

            myCount++;
            return (T)myList.get(myCount);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
