image = unite-its-tools
container = unite-its-tools

build: dependency
	docker build -t $(image):latest .

build-clean: dependency
	docker build --no-cache -t $(image):latest .
	
build-clean-all: dependency-clean
	docker build --no-cache -t $(image):latest .

run-new:
	docker run -p 8080:8080 -p 8081:8081 --name $(container) $(image)

run-existing:
	docker start -a $(container)

clean-image:
	docker image rm $(container)

clean-container:
	docker container rm $(container)

clean: clean-image clean-container

dependency:
	$(MAKE) -C ../unite-base build

dependency-clean:
	$(MAKE) -C ../unite-base build-clean


up:
	docker-compose up -d

down:
	docker-compose down