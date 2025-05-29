package com.sd.laborator.services
import com.sd.laborator.interfaces.CartesianProductOperation
import com.sd.laborator.interfaces.InitiateChaining
import com.sd.laborator.interfaces.PrimeNumberGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service





@Service
class InitiateChainingService(): InitiateChaining {

    @Autowired
    private lateinit var cartesianProductOperation: CartesianProductOperation

    private val primeNumbersIn1To100: Set<Int> = setOf(2, 3, 5, 7, 11,
        13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83,
        89, 97)
    override fun start(A:MutableSet<Int>,B:MutableSet<Int>):Set<Pair<Int,Int>>{

        val result=cartesianProductOperation.executeOperation(A,B)
        return result




    }
}