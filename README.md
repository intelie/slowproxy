## Instalation

Run this in the terminal:

    LOCATION=`curl -i https://github.com/intelie/slowproxy/releases/latest | perl -n -e '/^Location: \r*([^\r]*)\r*$/ && print "$1"'` &&
    curl -L ${LOCATION/\/tag\///download/}/slowproxy > slowproxy &&
    chmod a+x slowproxy
    
## Usage

    $ ./slowproxy @56kbps 1234 somehost:5678

Proxies requests from localhost:1234 to somehost:5678, limited to 56kbps

    $ ./slowproxy @2mbps/56kbps 1234 somehost:5678
    
Proxies requests from localhost:1234 to somehost:5678, limited to 2mbps upload and 56kbps download
