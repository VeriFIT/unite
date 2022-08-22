build-all:
	$(MAKE) -C unite-base build
	$(MAKE) -C facebook-infer build
	$(MAKE) -C theta build
	$(MAKE) -C valgrind build
	$(MAKE) -C frama-c build
	$(MAKE) -C grep build
	$(MAKE) -C perun build
	$(MAKE) -C cbmc build
	$(MAKE) -C symbiotic build
# 	$(MAKE) -C anaconda build

build-all-clean:
	$(MAKE) -C unite-base build-clean
	$(MAKE) -C facebook-infer build-clean
	$(MAKE) -C theta build-clean
	$(MAKE) -C valgrind build-clean
	$(MAKE) -C frama-c build-clean
	$(MAKE) -C grep build-clean
	$(MAKE) -C perun build-clean
	$(MAKE) -C cbmc build-clean
	$(MAKE) -C symbiotic build-clean
# 	$(MAKE) -C anaconda build-clean

build-all-as-one:
	$(MAKE) -C unite-base build
	$(MAKE) -C unite-all build

build-all-as-one-clean:
	$(MAKE) -C unite-base build-clean
	$(MAKE) -C unite-all build-clean

up-all:
	docker-compose up -d

down-all:
	docker-compose down

up-all-as-one:
	$(MAKE) -C unite-all up

down-all-as-one:
	$(MAKE) -C unite-all down