#leave kept lines in original order and add new lines at the end
#we need a temporary file because we want to read and write from
#the same file

APPEND_FILES = $(wildcard *_append)
TABLE_FILES = $(wildcard *_list)
APPEND_DIRS = $(patsubst %_list,%,$(APPEND_FILES))
TABLE_DIRS = $(patsubst %_list,%,$(TABLE_FILES))

$(TABLE_DIRS):
	mkdir $@

%/makefile.inc:
	php gen_makefile.php $* > $*/makefile.inc

clean:
	rm -f $(foreach d,$(TABLE_DIRS),$d/makefile.inc); \
	$(foreach d,$(TABLE_DIRS),[ -d $d ] && rmdir $d;) \
	true
.PRECIOUS: %/makefile.inc
