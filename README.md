## [ArchiveSpark](https://github.com/helgeho/ArchiveSpark)2Triples

This library provides tools to convert [ArchiveSpark](https://github.com/helgeho/ArchiveSpark) records from web archives to [RDF](https://en.wikipedia.org/wiki/Resource_Description_Framework) triples in [*Notation3 (N3)*](https://en.wikipedia.org/wiki/Notation3) format.

To build a JAR file of this project, clone the repository and call `sbt assembly` (this requires [SBT](http://www.scala-sbt.org/)).
The JAR file needs to be added to your classpath together with ArchiveSpark. To run it on a cluster please add it to both [Spark](http://spark.apache.org/) parameters `--driver-class-path` and `--jars`.

An example on how to use it is provided as [Jupyter](http://jupyter.org/) notebook: [**here**](notebooks/Triples.ipynb)  
The example requires [FEL4ArchiveSpark](https://github.com/helgeho/FEL4ArchiveSpark) to be on your classpath for entity recognition.

### License

The MIT License (MIT)

Copyright (c) 2017 Helge Holzmann

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
