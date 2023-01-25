image = unite-base
container = unite-base
unite_version = v3.0.0

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