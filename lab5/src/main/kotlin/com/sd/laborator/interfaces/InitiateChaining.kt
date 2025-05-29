package com.sd.laborator.interfaces

interface InitiateChaining {
    fun start(A:MutableSet<Int>,B:MutableSet<Int>):Set<Pair<Int,Int>>
}