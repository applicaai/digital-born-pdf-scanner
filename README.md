# Digital-born PDF Scanner

## Genesis

Many of PDF files that we have downloaded are digital-born, that is contain easily accessible text layer that PDF
viewers use to display text. Some are definitely scanned documents, that do not have any text layer at all, some
are searchable OCR-processed scans that contain a lot of hidden text.

Since we want to tell apart all of these categories, we need a tool to detect them. Thus this tool.

## Usage

In order to run use `java -jar`:

`java -jar digital-born-pdf-scanner-0.0.1-SNAPSHOT-jar-with-dependencies.jar`

Invoking the jar without parameters produces the following output:

```
  Options:
    -f, --filename
      Filename to check in single file mode
    -d, --input-dir
      Input directory to look for PDF files
    -o, --output-file-name
      File to write results to. Supported extensions are *.tsv, *.csv
      Default: results.tsv
    -r, --recursive
      Whether to search for PDF files recursively
      Default: false
    --sort
      Whether to sort file name in results.
      Default: false
    -v, --verbose
      Whether to print processed file names.
      Default: false
```

Clearly, there are two modes of operation:
 * single file mode (use `-f path-to-pdf-file`)
 * directory scan mode (use `-d path-to-directory-with-pdf-files`)
 
The latter can be used with recursive directory scan (use `-r`), which searches subdirectories for PDF files.

The output will be stored to TAB-separated file `results.tsv` unless different file name is provided. The output will be
either semicolon or TAB-separated depending on file extension.

### Handling error log

Since there might be a lot of errors coming from failed files printed, it makes sense to redirect logs to a file,
for example:

`java -jar digital-born-pdf-scanner-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d dir-with-pdfs -r --sort -v 2> error.log`

### Tracking progress

Currently the only way to track processing progress is to enable verbose output (`-v`) and couple it with file sorting
(`--sort`). This will produce nice color output showing which file is successfully processed (or whether there was
processing failure). Please see example above.

## Output interpretation

The output file consists of following columns:

| Column Name | Description |
| :--- | :--- |
| File Name | Path to PDF file |
| Has Hidden Text | Is hidden text present in a document |
| Visible Text Len | Length of visible text in a document |
| Hidden Text Len | Length of hidden text in a document |
| Creator | Name of a software that created a document (if any) |
| Producer | Name of a software library used to produce a document (if any) |
| Page Count | Number of pages in a document |
| Max Covered Area Ratio | Maximal ratio of the largest image area to page area (often greater than 1...) |
| Avg Covered Area Ratio | Average ratio of the largest image area to page area (often greater than 1...) |
| Image Count | Number of images in a document |
| Object Count | Number of objects in a document |
| PDF Version | Version of PDF standard |
| Has Outlines | Whether document contains outlines |
| Is Tagged | Does document have tag structure |
| Lang | Content language |
| Conformance Level | Document conformance level |
| Has Page Labels | Whether there are page labels in a document |

At a time of writing, the tool does not tell you if document is scanned, searchable scanned, or is digital born.
However, certain heuristics can be deduced from the output:

* No text in a document (both visible and hidden text len equals 0) - this is a scanned document with 99% probability
* No visible text, a lot of hidden text, max covered area ≈ 1.0 - this must be a searchable scanned document
* No hidden text, a lot of visible text - this is probably digital-born document

The question is what is "a lot of text". Well, we have to check to know.
