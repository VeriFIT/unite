image = unite-all
container = unite-all
unite_version = v3.0.0

# docker commands ##############################################################

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


# podman commands ##############################################################

podman-build: podman-dependency
	podman build --squash-all --build-arg UNITE_VERSION=$(unite_version) -t $(image):latest .

podman-build-clean: podman-dependency
	podman build --squash-all --build-arg UNITE_VERSION=$(unite_version) --no-cache -t $(image):latest .
	
podman-build-clean-all: dependency-clean
	podman build --squash-all --build-arg UNITE_VERSION=$(unite_version) --no-cache -t $(image):latest .

podman-clean-image:
	podman image rm $(container)

podman-clean-container:
	podman container rm $(container)

podman-clean: podman-clean-image podman-clean-container

podman-dependency:
	$(MAKE) -C ../unite-base podman-build unite_version=$(unite_version)

podman-dependency-clean:
	$(MAKE) -C ../unite-base podman-build-clean unite_version=$(unite_version)

podman-up:
	podman-compose up -d

podman-down:
	podman-compose down