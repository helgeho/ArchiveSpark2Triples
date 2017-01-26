/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Helge Holzmann
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.l3s.archivespark2triples

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import de.l3s.archivespark.enrich.TypedEnrichable
import de.l3s.archivespark.specific.warc.CdxRecord
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

import scala.reflect.ClassTag

object ArchiveSpark2Triples {
  def dateTimeStr(time: LocalDateTime) = s""""${time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"^^xsd:dateTime"""
  def urlId(url: String) = s"<${url.replace("<", "%3C").replace(">", "%3E")}>"

  var versionUrl: (CdxRecord => String) = record => s"https://web.archive.org/web/${record.timestamp}/${record.originalUrl}"

  def generateDocs[Record <: TypedEnrichable[CdxRecord] : ClassTag](records: RDD[Record]): RDD[TripleDoc] = generateDocs(records.map(_.get))
  def generateDocs(records: RDD[CdxRecord]): RDD[TripleDoc] = {
    records.groupBy(r => r.surtUrl).map{case (url, captures) =>
      val versions = captures.toSeq.sortBy(r => r.time)(Ordering.fromLessThan[LocalDateTime](_.compareTo(_) < 0))
      TripleDoc(
        s"$url##0",
        s"<${versions.head.originalUrl}>",
        "owa:ArchivedDocument",
        Seq(
          "owa:numOfCaptures" -> Seq(s""""${versions.size}"^^xsd:integer"""),
          "owa:firstCapture" -> Seq(dateTimeStr(versions.head.time)),
          "owa:lastCapture" -> Seq(dateTimeStr(versions.reverse.head.time)),
          "dc:hasVersion" -> versions.map(v => urlId(versionUrl(v)))
        )
      )
    }
  }

  def generateSameAsVersions[Record <: TypedEnrichable[CdxRecord] : ClassTag](records: RDD[(Record, Record)]): RDD[TripleDoc] = generateSameAsVersions(records.map{case (r1, r2) => (r1.get, r2.get)})
  def generateSameAsVersions(records: RDD[(CdxRecord, CdxRecord)]): RDD[TripleDoc] = records.map{case (sameAs, record) => TripleDoc(
    s"${sameAs.surtUrl}##${sameAs.timestamp}",
    urlId(versionUrl(sameAs)),
    "owa:VersionedDocument",
    Seq(
      "dc:date" -> Seq(dateTimeStr(sameAs.time)),
      "owl:sameAs" -> Seq(urlId(versionUrl(record)))
    )
  )}

  def generateVersionTriples[Record <: TypedEnrichable[CdxRecord] : ClassTag](records: RDD[Record]): RDD[(Record, TripleDoc)] = records.map(r => (r, versionTriples(r.get)))
  def generateVersionTriples(records: RDD[CdxRecord]): RDD[(CdxRecord, TripleDoc)] = records.map(r => (r, versionTriples(r)))

  def versionTriples(record: CdxRecord) = TripleDoc(
    s"${record.surtUrl}##${record.timestamp}",
    urlId(versionUrl(record)),
    "owa:VersionedDocument",
    Seq(
      "dc:date" -> Seq(dateTimeStr(record.time)),
      "dc:format" -> Seq(s""""${record.mime}"""")
    )
  )

  def generateVersionsMapped[Record <: TypedEnrichable[CdxRecord] : ClassTag](records: RDD[Record])(map: (Record, Long, TripleDoc) => TripleDoc): RDD[TripleDoc] = {
    generateVersionTriples(records).zipWithUniqueId.mapPartitions(_.map{case ((record, triples), uid) => map(record, uid, triples)})
  }
  def generateVersionsMapped(records: RDD[CdxRecord])(map: (CdxRecord, Long, TripleDoc) => TripleDoc): RDD[TripleDoc] = {
    generateVersionTriples(records).zipWithUniqueId.mapPartitions(_.map{case ((record, triples), uid) => map(record, uid, triples)})
  }

  def generateVersions[Record <: TypedEnrichable[CdxRecord] : ClassTag](records: RDD[Record]): RDD[TripleDoc] = {
    generateVersionTriples(records).map{case (record, triples) => triples}
  }
  def generateVersions(records: RDD[CdxRecord]): RDD[TripleDoc] = {
    generateVersionTriples(records).map{case (record, triples) => triples}
  }

  def toStringsSorted(header: TripleHeader, triples1: RDD[TripleDoc], triples2: RDD[TripleDoc]*) = {
    var union = triples1
    for (triples <- triples2) union = union.union(triples)
    val sorted = union.map(doc => (doc.key, doc.toString)).persist(StorageLevel.DISK_ONLY_2).sortByKey().values
    sorted.mapPartitions(strs => Iterator(header.toString) ++ strs)
  }

  def toStrings(header: TripleHeader, triples1: RDD[TripleDoc], triples2: RDD[TripleDoc]*) = {
    var union = triples1
    for (triples <- triples2) union = union.union(triples)
    union.map(_.toString).mapPartitions(strs => Iterator(header.toString) ++ strs)
  }
}
