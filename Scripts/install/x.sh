#!/bin/bash

echo "\$0 '$0'"
echo "OSTYPE '${OSTYPE}'"
echo "HOSTNAME '${HOSTNAME}'"
echo "HOSTTYPE '${HOSTTYPE}'"
echo "GROUPS '${GROUPS}'"
echo "BASH_VERSION '${BASH_VERSION}'"
echo "BASH_SOURCE '${BASH_SOURCE}'"
INSTALLSCRIPT_BASE="$(dirname -- "$(readlink -f "${BASH_SOURCE}")")"
SCRIPTS_BASE="$(dirname -- "$(dirname -- "$(readlink -f "${INSTALLSCRIPT_BASE}")")")"
echo "SCRIPTS_BASE '${SCRIPTS_BASE}'"
