package com.falcon.technologies

import java.util.ArrayList
import kotlin.collections.ArrayList


/*
*     var array = [[1,2,3],[5,6,7,8]]
// Write a code to flatten an array,
// Output: 1, 2, 3, 4, 5, 6, 7, 8

* */
fun main() {

    val first: ArrayList<Int> = arrayListOf<Int>(1, 2, 3)
    val second = arrayListOf<Int>(5, 6, 7, 8)
    val result = mutableListOf<ArrayList<Int>>(first, second)
    println(result)

    result.flatten()

    val evenNumber: ArrayList<Int> = arrayListOf<Int>(2, 4, 6, 8, 10)
    val randomNumber = arrayListOf<Int>(1, 4, 7, 9, 10, 12)

    //we will check overlap item using contain method...

    val resultsItem = mutableListOf<Int>()
    evenNumber.forEach { item ->
        randomNumber.forEach { newItem ->
            if (item == newItem) {
                resultsItem.add(item)
                println(resultsItem)
            }
        }
    }

}

/*
var evenNumbers = [2, 4, 6, 8, 10]
var oddNumbers = [3, 5, 7, 9, 11]
var randomNumbers = [1, 4, 7, 9, 10, 12]

// find lverlap items between evenNumbers and randomNumbers
print(randomNumbers)
// Output: 4,10
*/
