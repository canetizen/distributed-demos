##
 # Author: canetizen
 # Created on Sat Jun 14 2025
 # Description: Makefile for simulation.
##

# Default target
up:
	docker-compose up --build

# Tear down containers and volumes, clean logs.
clean:
	docker-compose down -v
	rm -f logs/*

# Show logs for a specific node (use: make logs NODE=node1)
logs:
ifndef NODE
	$(error NODE is not set. Use: make logs NODE=node1)
endif
	docker-compose logs $(NODE)
