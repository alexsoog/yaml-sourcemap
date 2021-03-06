# YAMLSourceMap
__Map YAML to Data, and Back. In Java.__

[![Build Status](https://travis-ci.com/abego/yaml-sourcemap.svg?branch=master)](https://travis-ci.com/abego/yaml-sourcemap)
## Overview

The YAMLSourceMap provides a mapping between locations in a YAML document 
(the source) and the data values created from the YAML document.

![Mapping between YAML document text and Data (JSON pointer)
](abego-yaml-sourcemap-core/src/main/javadoc/org/abego/yaml/sourcemap/doc-files/mapping.png)

The mapping works in both directions.

### YAML document text -> Data (JSON pointer)
        
If you have a location in the YAML document, the source map tells you the 
address (JSON pointer) of the data value this location relates to.
        
### Data (JSON pointer) -> YAML document text

If you have a JSON pointer for some data created from the YAML document 
the source map tells you the locations in the YAML document that created 
the data.

## JSON and YAML

As YAML is a superset of JSON the YAMLSourceMap can also be used to create 
source maps for JSON documents.

## Usage

### Creating a YAMLSourceMap

The central type of this module is the YAMLSourceMap. 
You create a YAMLSourceMap for a specific YAML document using the YAMLSourceMapAPI.
Either you specify a Reader to read the YAML text from:

```java
Reader reader = ...;
YAMLSourceMap srcMap = YAMLSourceMapAPI.createYAMLSourceMap(reader);
```

or directly pass in the YAML text:

```java
String yamlText = "foo: 123\nbar: 456\n";
YAMLSourceMap srcMap = YAMLSourceMapAPI.createYAMLSourceMap(yamlText);
``` 

### The Basic Use Cases

#### Find the data for a YAML/JSON document text location (Text location -> Data)

Once you have the YAMLSourceMap you can pass in a location in the YAML 
document text and the source map gives you the address of the data (value) 
the text at the given location in the YAML document created.
 
The data address is given as a JSON Pointer [1], a standard format to identify 
a specific value in a JSON document.

You can either specify the location in the YAML text as an offset to the start
of the text:

```java
YAMLSourceMap srcMap =...;

int offset = 42;
String jsonPointer = srcMap.jsonPointerAtOffset(offset); // return e.g. "/bill-to/address"
``` 

or give the location by line and column. E.g. to get the JSON Pointer for the
text of column 14 of the third line you would write:

```java
YAMLSourceMap srcMap =...;

String jsonPointer = srcMap.jsonPointerAtLocation(3, 14); // return e.g. "/bill-to/address"
```

#### <a name="data-to-text"></a>Find the YAML/JSON document text that created a data value (Data -> Text location)

To get from some data value to the corresponding YAML document text use 
`YAMLSourceMap.sourceRangeOfPointer(java.lang.String)`.
Pass in a JSON Pointer and the method gives you the range in the YAML text 
related to the data value. This may also include surrounding whitespaces 
or comments, or special characters like ":", "[" etc.):

```java
YAMLSourceMap srcMap =...;

String jsonPointer = "/bill-to/address";
YAMLRange range = srcMap.sourceRangeOfPointer(jsonPointer);
``` 

If you interested just in the text range that _defines_ the data value 
you can use the method `YAMLSourceMap.sourceRangeOfValueOfJsonPointer(...)`:

```java
YAMLSourceMap srcMap =...;

String jsonPointer = "/bill-to/address";
YAMLRange range = srcMap.sourceRangeOfValueOfJsonPointer(jsonPointer);
```

The following picture demonstrates the difference between 
`sourceRangeOfJsonPointer` and `sourceRangeOfValueOfJsonPointer` for the example
JSON Pointer `/bill-to/address`. 

![Difference between sourceRangeOfJsonPointer and sourceRangeOfValueOfJsonPointer
](abego-yaml-sourcemap-core/src/main/javadoc/org/abego/yaml/sourcemap/doc-files/source-range.png)


As you can see `sourceRangeOfJsonPointer` also includes white spaces 
and the map item's key "`address:`", but `sourceRangeOfValueOfJsonPointer` 
just the range directly defining the _value_ for JSON Pointer "`/bill-to/address`".

### The Fragments API

The methods `jsonPointerAtOffset`, `jsonPointerAtLocation` 
`sourceRangeOfJsonPointer`, and `sourceRangeOfValueOfJsonPointer` cover 
the basic cases you may want to use a YAMLSourceMap for.

In addition, the YAMLSourceMap also provides the `FragmentsAPI` that
gives you a more detailed mapping between the YAML text and its data. 
As the name suggests the central idea here are "fragments".

Fragments partition the whole YAML text into non-overlapping ranges.
Each fragment covers a range of characters that share the same data (address), 
i.e. the same JSON pointer. Additionally, a fragment is of a certain kind
describes what sort of elements in the YAML document a fragment related to.

Beside the kinds known from the JSON data model (scalar, sequence, map)
other kinds exist that refine these basic kinds, to cover "sub aspects". 
E.g. for a map the kinds `MAP_KEY` and `MAP_VALUE` define subranges within 
the map entry's definition.

This additional information gives you many options for your applications.
E.g. assume you want to use a YAMLSourceMap to implement some content 
assist feature when editing a YAML document. With the fragments it is easy
to display different assists e.g. for the key vs. the value of a map item.
Or you can even show different assists depending on where in a map's key
the user has the text cursor.
 
The following picture shows the available fragment kinds and how they
relate to (a sample) YAML text.

![Fragment Kinds and how they relate to YAML text
](abego-yaml-sourcemap-core/src/main/javadoc/org/abego/yaml/sourcemap/doc-files/fragment-kind-and-legend.png)

Typically, a given JSON pointer relates to multiple fragments, 
of different kinds. E.g. in the picture above the first three
green fragments all map to JSON pointer `/invoice`.

For more details please see the JavaDoc of the FragmentAPI or 
the `FragmentKindColorCodingApp` in the examples.
 
## Examples

Have a look at the module `abego-yaml-sourcemap-examples` for some examples how
the YAMLSourceMap can be used in an application.

E.g. the "Breadcrumbs" application demonstrates how to use the YAMLSourceMap to
implement a "Breadcrumbs bar" (/Navigation bar), e.g. to view YAML/JSON documents.

![Mapping between YAML document text and Data (JSON pointer)
](abego-yaml-sourcemap-core/src/main/javadoc/org/abego/yaml/sourcemap/doc-files/breadcrumbs-demo.png)

That application is also a use case for "bidirectional mapping": 

- After a click in the YAML text (the source) the source map provides the
address of the data created by the text at the click location. The Breadcrumbs 
bar then displays the parts of this address/JSON Pointer as breadcrumbs. 
_(YAML Text -> Data)_
- Clicking a breadcrumb in the Breadcrumbs bar navigates the text cursor in the
YAML text to the location corresponding to that breadcrumb. (Every breadcrumb 
actually is a JSON Pointer). The source map provides the proper location for
every given JSON Pointer/breadcrumb. _(Data -> YAML Text)_

BTW: the "Breadcrumbs" application also highlights the source ranges of the 
YAML entity located at the text cursor position, both the "full" source
range and the "value" source range. For details on the difference see chapter 
[Find the YAML/JSON document text that created a data value](#data-to-text).
 
[1]: https://tools.ietf.org/html/rfc6901

## Development

You may check out the source code from the 
[GitHub repository](https://github.com/abego/yaml-sourcemap).

## Links

- Sources: https://github.com/abego/yaml-sourcemap
- Twitter: @abego (e.g. for announcements of new releases)

## License

YAMLSourceMap is available under a business friendly 
[MIT license](https://www.abego-software.de/legal/mit-license.html).


