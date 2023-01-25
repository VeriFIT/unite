image = unite-symbiotic
container = unite-symbiotic
unite_version = v3.0.0

build: dependency
	docker build --build-arg UNITE_VERSION=$(unite_version) -t $(image):latest .

build-clean: dependency
	docker build --build-arg UNITE_VERSION=$(unite_version) --no-cache -t $(image):latest .
	
build-clean-all: dependency-clean
	docker build --build-arg UNITE_VERSION=$(unite_version) --no-cache -t $(image):latest .

clean-image:
	docker image rm $(container)

clean-container:
	docker container rm $(container)

clean: clean-image clean-container

dependency:
	$(MAKE) -C ../unite-base build unite_version=$(unite_version)

dependency-clean:
	$(MAKE) -C ../unite-base build-clean unite_version=$(unite_version)

up:
	docker-compose up -d

down:
	docker-compose down