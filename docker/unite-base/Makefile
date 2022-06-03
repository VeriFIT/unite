image = unite-base
container = unite-base

build:
	docker build -t $(image):latest .

build-clean:
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


compose:
	docker-compose up