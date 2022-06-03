build-all:
	$(MAKE) -C unite-base build
	$(MAKE) -C facebook-infer build
	$(MAKE) -C theta build
	$(MAKE) -C valgrind build
	$(MAKE) -C frama-c build
	$(MAKE) -C grep build
# 	$(MAKE) -C anaconda build

build-all-clean:
	$(MAKE) -C unite-base build-clean
	$(MAKE) -C facebook-infer build-clean
	$(MAKE) -C theta build-clean
	$(MAKE) -C valgrind build-clean
	$(MAKE) -C frama-c build-clean
	$(MAKE) -C grep build-clean
# 	$(MAKE) -C anaconda build-clean

build-all-as-one:
	$(MAKE) -C unite-base build
	$(MAKE) -C unite-all build

build-all-as-one-clean:
	$(MAKE) -C unite-base build-clean
	$(MAKE) -C unite-all build-clean

up-all:
	$(MAKE) -C facebook-infer up
	$(MAKE) -C theta up
	$(MAKE) -C valgrind up
	$(MAKE) -C frama-c up
	$(MAKE) -C grep up
#	$(MAKE) -C anaconda up

down-all:
	$(MAKE) -C facebook-infer down
	$(MAKE) -C theta down
	$(MAKE) -C valgrind down
	$(MAKE) -C frama-c down
	$(MAKE) -C grep down
# 	$(MAKE) -C anaconda down

up-one:
	$(MAKE) -C unite-all up

down-one:
	$(MAKE) -C unite-all down