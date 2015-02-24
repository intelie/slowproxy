## Installation

Run this in the terminal (downloads the latest binary to the current directory):

    LOCATION=`curl -i https://github.com/intelie/slowproxy/releases/latest | perl -n -e '/^Location: \r*([^\r]*)\r*$/ && print "$1"'` &&
    curl -L ${LOCATION/\/tag\///download/}/slowproxy > slowproxy &&
    chmod a+x slowproxy

Or this, if you want to install it system-wide:

    LOCATION=`curl -i https://github.com/intelie/slowproxy/releases/latest | perl -n -e '/^Location: \r*([^\r]*)\r*$/ && print "$1"'` &&
    curl -L ${LOCATION/\/tag\///download/}/slowproxy > slowproxy &&
    chmod a+x slowproxy &&
    sudo mv slowproxy /usr/local/bin

## Usage

    $ ./slowproxy @56kbps 1234 somehost:5678

Proxies requests from localhost:1234 to somehost:5678, limited to 56kbps

    $ ./slowproxy @2mbps/56kbps 1234 somehost:5678
    
Proxies requests from localhost:1234 to somehost:5678, limited to 2mbps download and 56kbps upload

## TODO

* Better shell
* Simulate network stall
* Simulate latency
