# openregister-derivation
Home of register derivations

[![Build Status](https://travis-ci.org/openregister/openregister-derivation.svg?branch=master)](https://travis-ci.org/openregister/openregister-derivation)

# Requirements

- Java 1.8+

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

