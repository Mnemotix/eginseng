package com.mnemotix.semweb;

public enum Format {
	JSON ("json"),
	CSV ("csv"),
	XML ("xml"),
	RDF_XML ("rdf"),
	NTRIPLES ("text/plain"),
	N3_TURTLE ("n3");

    private final String name;       

    private Format(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
       return name;
    }

}
