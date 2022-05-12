package com.lib.model

class IndexMap<K,V>: Iterator<V> {
    private val map = HashMap<K,V>()
    private val index = ArrayList<K>()
    private var currentIndex = 0
    var size = 0
        get(){ return index.size }
        private set

    operator fun iterator(): Iterator<V> {
        currentIndex = 0
        return this
    }

    fun indexOf(key:K):Int {
        return index.indexOf(key)
    }

    override fun hasNext(): Boolean {
        return currentIndex <= index.size
    }

    override fun next():V {
        val idx = currentIndex
        currentIndex ++
        return get(idx)!!
    }

    fun get(key:K):V? {
        return map[key]
    }

    fun get(idx:Int):V? {
        if(index.isEmpty()) return null
        val key = index[idx]
        return map[key]
    }

    fun clear() {
        map.clear()
        index.clear()
    }

    fun remove(idx:Int) {
        val key = index[idx]
        remove(key)
    }

    fun remove(key:K) {
        map.remove(key)
        index.remove(key)
    }

    fun isEmpty():Boolean {
        return index.isEmpty()
    }

    fun put(key:K,value:V):Int {
        index.add(key)
        map[key] = value
        return index.size-1
    }

    fun poll():V? {
        if(index.isEmpty()) return null
        val key = index.first()
        val v = map[key]
        remove(key)
        return v
    }

    fun pop():V? {
        if(index.isEmpty()) return null
        val key = index.last()
        val v = map[key]
        remove(key)
        return v
    }
}