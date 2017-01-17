package com.github.astonbitecode.zoocache.api.dtos

import java.util.List
import java.lang.String

class JCacheResult(path: String, data: Array[Byte], children: List[String]) {
  def getPath(): String = path

  def getData(): Array[Byte] = data

  def getChildren(): List[String] = children
}