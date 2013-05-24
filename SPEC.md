Code Documentation Specification
--------------------------------

Garamond uses a streamed JSON protocol to extract documentation information
from a given source. The protocol contains two categories of elements.

* Hierachical elemements, such as packages, name spaces or classes.
* Functions and Fields

Each of these elements always has a name. a doc string and a type.

When generating documentation the project source files are parsed and
converted into a stream of documentation items of this form. A typical 
run of the source parser might produce output like this:

```js
{
  "type":"hierarchy",
  "domain":"package",
  "name":"foo.bar",
  "doc":"my package description"
}
{
  "type":"hierarchy",
  "domain":"class",
  "name":"Fruit",
  "doc":"This thing models fruits"
}
{
  "type":"function",
  "domain":"method",
  "name":"eat",
  "doc":"Eating fruits is healthy",
  "parameters": [
    ...
  ]
}
```

This stream of JSON objects is then fed to a generator process, which
might insert the information into a document template or use it in some 
other way.
