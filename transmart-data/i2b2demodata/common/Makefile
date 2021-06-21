#leave kept lines in original order and add new lines at the end
#we need a temporary file because we want to read and write from
#the same file

APPEND_FILES = $(wildcard *_append)
TABLE_FILES = $(wildcard *_list)
APPEND_DIRS = $(patsubst %_list,%,$(APPEND_FILES))
TABLE_DIRS = $(patsubst %_list,%,$(TABLE_FILES))

.PRECIOUS: %/makefile.inc
