# openregister-derivation
Home of register derivations

[![Build Status](https://travis-ci.org/openregister/openregister-derivation.svg?branch=master)](https://travis-ci.org/openregister/openregister-derivation)

## Requirements

- Java 1.8+

## Description

Currently a Java command line application which reads RSF files representing the current state of the Local Authority Types Derivation and 
the updates to the original Local Authority Eng register. An RSF representing the whole updated Derivation is written to the 
 Standard Output.

## Usage 

The command line application requires 3 arguments:

- path to RSF *local authority eng* update file
- path to RSF *local authority types* state file [optional]

For example:

	cd [project root]
    gradle derivation-cli:shadowJar
    cd derivation-cli/build/libs
    
Creating an initial RSF for Local Authority Types:

    java -jar derivation-cli-all.jar /tmp/local-authority-eng.rsf > /tmp/local-authority-type.rsf
    
Now we might generate a update RSF by taking some lines from the local-authority-eng.tsv file (note **TSV**), changing 
 the *local-authority-type* field for some *local authorities* and using 
the Serializer to generate an update RSF. Now we could use the first Local Authority Type RSF as the *state*:
  
    java -jar derivation-cli-all.jar /tmp/local-authority-eng-update.rsf /tmp/local-authority-type.rsf > /tmp/local-authority-type-updated.rsf
    
## Loading into Open Register Java

Open Register Java will validate the Items and Entries being added against the definition of the Register in *registers.yaml*
and *fields.yaml*. The 'register' in this case will be *local-authority-type* which is different from the existing 
*local-authority-type* register and contains a new *field* - *local-authorities*. 
There are hand crafted versions of the *registers.yaml* and *fields.yaml* files in the *test-data* directory.

The *config.yaml* for ORJ will have to be altered to refer to the base register as *local-authority-type* and references to any other
registers to be served by ORJ should be removed. 

The *config.yaml* should also point to the altered *registers.yaml* and *fields.yaml* using a URL e.g. *file:///tmp/registers.yaml* etc.



