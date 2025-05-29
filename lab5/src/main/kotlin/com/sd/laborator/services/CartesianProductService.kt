package com.sd.laborator.services
import com.sd.laborator.interfaces.CartesianProductOperation
import com.sd.laborator.interfaces.UnionOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service




@Service
class CartesianProductService: CartesianProductOperation {

    @Autowired
    private lateinit var unionOperation: UnionOperation

    override fun executeOperation(A: Set<Int>, B: Set<Int>):Set<Pair<Int,Int>>
    {
        var result1: MutableSet<Pair<Int, Int>> = mutableSetOf()
        A.forEach { a -> B.forEach { b -> result1.add(Pair(a, b)) } }

        var result2: MutableSet<Pair<Int, Int>> = mutableSetOf()
        B.forEach { a -> B.forEach { b -> result2.add(Pair(a, b)) } }

        val result=unionOperation.executeOperation(result1,result2)
        return result
    }
}