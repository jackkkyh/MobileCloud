package com.urop.common

import java.math.BigInteger


open class Task(@JvmField var id: String = (globalID++).toString()) {

    companion object {
        var globalID = 0
    }
}

open class SortTask(@JvmField val start: Int, @JvmField val end: Int) : Task(encodeID(start, end)) {
    lateinit var arr: IntArray

    @JvmField
    var waitCount = 0

    companion object {
        fun encodeID(start: Int, end: Int): String {
            return "[$start,$end]"
        }
    }
}


class QSortTask(start: Int = 0, end: Int = 0) : SortTask(start, end)
class MSortTask(start: Int = 0, end: Int = 0) : SortTask(start, end)

class NOPTask : Task()

class Message(val msg: String = "") : Task()

typealias Chessboard = IntArray

class NQueenTask(
    val chessboard: Chessboard,
    @JvmField var remaining: Int,
    @JvmField val step: Int
) : Task() {
    lateinit var solution: Array<Chessboard>

    constructor(size: Int = 0, step: Int = 0) : this(IntArray(size), size, step)
}

class FactorizationTask(
    @JvmField val num: BigInteger = BigInteger.ONE
) : Task() {
    //    init {
//        println("fact task $id created")
//    }
    lateinit var f1: BigInteger
    lateinit var f2: BigInteger
}