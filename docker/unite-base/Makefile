image = unite-base
container = unite-base
unite_version = v3.0.0

# docker commands ##############################################################

build:
	docker build --build-arg UNITE_VERSION=$(unite_version) -t $(image):$(unite_version) .

build-clean:
	docker build --build-arg UNITE_VERSION=$(unite_version) --no-cache -t $(image):$(unite_version) .

clean-image:
	docker image rm $(container)

clean-container:
	docker container rm $(container)

clean: clean-image clean-container


up:
	docker-compose up -d

down:
	docker-compose down


# podman commands ##############################################################

podman-build:
	podman build --squash-all --build-arg UNITE_VERSION=$(unite_version) -t $(image):$(unite_version) .

podman-build-clean:
	podman build --squash-all --build-arg UNITE_VERSION=$(unite_version) --no-cache -t $(image):$(unite_version) .

podman-clean-image:
	podman image rm $(container)

podman-clean-container:
	podman container rm $(container)

podman-clean: podman-clean-image podman-clean-container


podman-up:
	podman-compose up -d

podman-down:
	podman-compose down