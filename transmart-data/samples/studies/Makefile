include ../../lib/makefile.inc

FEED_LISTS:=public-feeds $(wildcard ../../private-feeds)
datasets: $(FEED_LISTS)
	groovy -cp '$(LIB_DIR)' build_datasets_index.groovy \
		$(foreach f,$(FEED_LISTS),-f $f) -o $@

update_datasets:
	@rm -f datasets
	@$(MAKE) --quiet datasets

# we depend on %/%_study.tar.xz, but make doesn't support repeated %

vpath %/annotation.params annotation

# all study data calls a script to check targets exist
# and create an (unused) loadall.params file
# listing the targets found in the datasets file
%/loadall.params:
	./find_study.pl $*

# study needs params file and load.sh script to cascade to all other scripts
%/study.params %/load.sh:
	$(MAKE) $*/$*_study.tar.xz
	tar -C $* -xJf $*/$*_study.tar.xz

# browse program needs params that define the program needed
%/program.params:
	$(MAKE) $*/$*_program.tar.xz
	tar -C $* -xJf $*/$*_program.tar.xz

# browse study needs params that point to the study and the program needed
%/browse.params:
	$(MAKE) $*/$*_browse.tar.xz
	tar -C $* -xJf $*/$*_browse.tar.xz

# samples needs params that point to the study needed
%/samples.params:
	$(MAKE) $*/$*_samples.tar.xz
	tar -C $* -xJf $*/$*_samples.tar.xz

# ref_annotation needs params that point to the annotation needed
%/ref_annotation.params:
	$(MAKE) $*/$*_ref_annotation.tar.xz
	tar -C $* -xJf $*/$*_ref_annotation.tar.xz

# annotation for all types needs params (defines the type) and annotation directory
%/annotation.params %/annotation:
	$(MAKE) $*/$*_annotation.tar.xz
	tar -C $* -xJf $*/$*_annotation.tar.xz

# clinical data and high-dimensional datatypes need .params and directory

vpath %/clinical.params clinical

vpath %/acgh.params acgh
vpath %/expression.params expression
vpath %/mirnaqpcr.params mirnaqpcr
vpath %/mirnaseq.params mirnaseq
vpath %/msproteomics.params msproteomics
vpath %/metabolomics.params metabolomics
vpath %/rbm.params rbm
vpath %/rnaseq.params rnaseq

%/clinical.params %/clinical:
	$(MAKE) $*/$*_clinical.tar.xz
	tar -C $* -xJf $*/$*_clinical.tar.xz

%/expression.params %/expression:
	$(MAKE) $*/$*_expression.tar.xz
	tar -C $* -xJf $*/$*_expression.tar.xz

%/acgh.params %/acgh:
	$(MAKE) $*/$*_acgh.tar.xz
	tar -C $* -xJf $*/$*_acgh.tar.xz

%/mirnaqpcr.params %/mirnaqpcr:
	$(MAKE) $*/$*_mirnaqpcr.tar.xz
	tar -C $* -xJf $*/$*_mirnaqpcr.tar.xz

%/mirnaseq.params %/mirnaseq:
	$(MAKE) $*/$*_mirnaseq.tar.xz
	tar -C $* -xJf $*/$*_mirnaseq.tar.xz

%/msproteomics.params %/msproteomics:
	$(MAKE) $*/$*_msproteomics.tar.xz
	tar -C $* -xJf $*/$*_msproteomics.tar.xz

%/metabolomics.params %/metabolomics:
	$(MAKE) $*/$*_metabolomics.tar.xz
	tar -C $* -xJf $*/$*_metabolomics.tar.xz

%/rbm.params %/rbm:
	$(MAKE) $*/$*_rbm.tar.xz
	tar -C $* -xJf $*/$*_rbm.tar.xz

%/rnaseq.params %/rnaseq:
	$(MAKE) $*/$*_rnaseq.tar.xz
	tar -C $* -xJf $*/$*_rnaseq.tar.xz

# old mirna target needs only params file
%/mirna.params:
	$(MAKE) $*/$*_mirna.tar.xz
	tar -C $* -xJf $*/$*_mirna.tar.xz

# old proteomics (not msproteomics) only needs a params file
%/proteomics.params:
	$(MAKE) $*/$*_proteomics.tar.xz
	tar -C $* -xJf $*/$*_proteomics.tar.xz

# vcf needs a params file to direct the scripts
%/vcf.params:
	$(MAKE) $*/$*_vcf.tar.xz
	tar -C $* -xJf $*/$*_vcf.tar.xz

TARBALLS := $(shell test ! -f datasets || php enumerate.php tarballs)
$(TARBALLS):
	@test -f datasets || $(MAKE) datasets
	mkdir -p "$$(php -r 'preg_match("@^[^/]+@", $$argv[1], $$m); echo $$m[0];' '$@')"
	./download_tarball.sh '$@' $$(php get_file_location.php '$@')

clean:
	rm -f datasets
	find . -path './*' -prune -type d -exec rm -r '{}' \;

.PHONY: update_datasets clean

.DELETE_ON_ERROR:
