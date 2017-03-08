# openregister-derivation
Home of register derivations

[![Build Status](https://travis-ci.org/openregister/openregister-derivation.svg?branch=master)](https://travis-ci.org/openregister/openregister-derivation)

# Requirements

- Java 1.8+

# Command Line Application

# Description

Currently a Java command line application which reads an RSF file and stores a JSON representation of the Registers
 described by the RSF in S3.

# Usage 

The command line application requires 3 arguments:

- path to RSF file
- s3 bucket
- s3 key

For example:

	cd [project root]
    gradle derivation-cli:shadowJar
    cd derivation-cli/build/libs
    java -jar derivation-cli-all.jar /tmp/countries.rsf openregister.derivation derivation.json

# Web Application

This is a simplified version of the Register application with derivation endpoints added.

### Building

	cd [project root]
    gradle derivation-framework:shadowJar
    
### Running

    cd derivation-framework
    java -jar build/libs/derivation-framework-all.jar server orj-lite.yaml
    
The application has no persistent data store. You need to load some register data into memory using:

	cd [project root]/orj-lite
    ./load-local-authority-rsf.sh 
    
   
   
