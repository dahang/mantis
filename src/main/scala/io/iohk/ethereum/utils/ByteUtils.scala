package io.iohk.ethereum.utils

import java.math.BigInteger
import java.nio.{ByteBuffer, ByteOrder}

import akka.util.ByteString

import scala.util.Random

object ByteUtils {

  def bigIntegerToBytes(b: BigInteger, numBytes: Int): Array[Byte] = {
    val bytes = new Array[Byte](numBytes)
    val biBytes = b.toByteArray
    val start = if (biBytes.length == numBytes + 1) 1 else 0
    val length = Math.min(biBytes.length, numBytes)
    System.arraycopy(biBytes, start, bytes, numBytes - length, length)
    bytes
  }

  def xor(a: Array[Byte], b: Array[Byte]): Array[Byte] = {
    (a zip b) map { case (b1, b2) => (b1 ^ b2).toByte }
  }

  def or(arrays: Array[Byte]*): Array[Byte] = {
    require(arrays.map(_.length).distinct.length <= 1, "All the arrays should have the same length")
    require(arrays.nonEmpty, "There should be one or more arrays")

    val zeroes = Array.fill(arrays.head.length)(0.toByte)
    arrays.foldLeft[Array[Byte]](zeroes){
      case (prevOr, array) => prevOr.zip(array).map{ case (b1, b2) => (b1 | b2).toByte }
    }
  }

  def and(arrays: Array[Byte]*): Array[Byte] = {
    require(arrays.map(_.length).distinct.length <= 1, "All the arrays should have the same length")
    require(arrays.nonEmpty, "There should be one or more arrays")

    val ones = Array.fill(arrays.head.length)(0xFF.toByte)
    arrays.foldLeft[Array[Byte]](ones){
      case (prevOr, array) => prevOr.zip(array).map{ case (b1, b2) => (b1 & b2).toByte }
    }
  }

  def randomBytes(len: Int): Array[Byte] = {
    val arr = new Array[Byte](len)
    new Random().nextBytes(arr)
    arr
  }

  def bigEndianToShort(bs: Array[Byte]): Short = {
    val n = bs(0) << 8
    (n | bs(1) & 0xFF).toShort
  }

  def padLeft(bytes: ByteString, length: Int, byte: Byte = 0): ByteString = {
    val l = math.max(0, length - bytes.length)
    val fill = Seq.fill[Byte](l)(byte)
    fill ++: bytes
  }

  def compactPickledBytes(buffer: ByteBuffer): ByteString = {
    val data = Array.ofDim[Byte](buffer.limit)
    buffer.rewind()
    buffer.get(data)
    ByteString(data)
  }


  def bytesToInts(bytes: Array[Byte]): Array[Int] =
    bytes.grouped(4).map(getIntFromWord).toArray

  def intsToBytes(input: Array[Int]): Array[Byte] = {
    input.flatMap { i =>
      Array(
        (i & 0xFF).toByte,
        ((i >> 8) & 0xFF).toByte,
        ((i >> 16) & 0xFF).toByte,
        ((i >> 24) & 0xFF).toByte)
    }
  }

  def getIntFromWord(arr: Array[Byte]): Int = {
    ByteBuffer.wrap(arr, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt
  }

}
