# TranSMART R client package

This package enables R programmers to access to a tranSMART installation via its [RESTful API](https://github.com/thehyve/transmart-rest-api).
Usually this involves authentication with the tranSMART installation. The package has built-in
functionality for obtaining an authentication token.
# Installation

## Package dependencies

### Protocol Buffers

The `protobuf` and its accompanying c implementation package need to be installed on the machine. 
The Protocol Buffers must be at version 2.2.0 or newer and can be installed either via the distribution
package manager.

Ubuntu:

    # apt-get install libprotobuf-dev protobuf-c-compiler

Redhat:

    # yum install protobuf-compiler protobuf protobuf-devel
    
Or install it from source with one of the packages available at [https://code.google.com/p/protobuf/downloads/list](https://code.google.com/p/protobuf/downloads/list).

### Curl

Windows users might need to install the `curl` package from [http://curl.haxx.se](http://curl.haxx.se).

## Installing transmartRClient

The package can be installed directly from github using the `devtools` package:

    require(devtools)
    install_github('transmart/RInterface')

or by following the instructions in `bin/installCommands.R`

# Demonstration of the package
The `demo/demoCommands.R` file contains a short demo of how to connect to tranSMART and how
to retrieve data from it.

# Contributing

The `bin/devCommands.R` file contains some pointers that might be useful to anyone interested
in developing on this package.

In order to contribute a patch, follow the instructions given in [https://guides.github.com/activities/contributing-to-open-source/#contributing](https://guides.github.com/activities/contributing-to-open-source/#contributing)

And: thanks for considering contributing!
