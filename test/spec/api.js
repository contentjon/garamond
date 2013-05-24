
var fs          = require("fs");
var expect      = require("chai").expect;
var validator   = require("JSV").JSV;
var env         = validator.createEnvironment();

describe("API documentation", function() {

    var schema = null;

    var validation = function(json) {
        return (env.validate(json, schema).errors.length == 0);
    }

    it("has a schema", function(done) {
        
        fs.readFile("schemas/spec.json", function(err, data) {
            schema = JSON.parse(data.toString());
            done(err);
        });
    });

    it("must have a type", function() {
        
        expect({"name":"foo", "doc":"bar"}).not.to.satisfy(validation);
    });

    it("must have a name", function() {
        
        expect({"type":"field", "doc":"bar"}).not.to.satisfy(validation);
    });

    it("must provide a doc string", function() {
        
        expect({"type":"field", "name":"baz"}).not.to.satisfy(validation);
    });

    it("is valid with a type, name and doc string", function() {
        
        expect({"type":"field", "name":"baz", "doc":"bar"}).to.satisfy(validation);
    });

    it("can optionally include a source", function() {
        
        expect({"type":"field",
                "name":"baz",
                "doc":"bar",
                "source":{"file":"foo/bar/baz"}}).to.satisfy(validation);

        expect({"type":"field",
                "name":"baz",
                "doc":"bar",
                "source":{"file":"foo/bar/baz", "line":12}}).to.satisfy(validation);

        expect({"type":"field",
                "name":"baz",
                "doc":"bar",
                "source":{"line":12}}).not.to.satisfy(validation);
    });

    it("can have a parameter description when documenting a function", function() {
        
        expect({"type":"function", 
                "name":"baz", 
                "doc":"bar",
                "parameters": [
                    {"name":"arg0",
                     "doc":"a doc string"}
                ]}).to.satisfy(validation);
        
        expect({"type":"field",
                "name":"baz",
                "doc":"bar",
                "parameters": [
                    {"name":"arg0",
                     "doc":"a doc string"}
                ]}).not.to.satisfy(validation);
    });
});
