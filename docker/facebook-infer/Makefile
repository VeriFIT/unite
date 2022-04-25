image = unite-infer
container = unite-infer

build:
	docker build --no-cache -t $(image):latest .

run-new:
	docker run -p 8080:8080 -p 8081:8081 --name $(container) $(image)

run-existing:
	docker start -a $(container)

clean-image:
	docker image prune

clean-container:
	docker container prune

clean:
	docker image prune
	docker container prune